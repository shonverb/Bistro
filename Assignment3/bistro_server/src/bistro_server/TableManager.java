package bistro_server;

import java.sql.*;
import java.util.*;
import entities.requests.AddTableRequest;
import entities.requests.RemoveTableRequest;
import entities.Order;
import entities.Table;

/**
 * Manages the lifecycle of restaurant tables.
 * Handles adding, removing, and updating tables while ensuring that existing and future orders
 * are not negatively impacted by these changes.
 */
public class TableManager {

    private BistroServer server;

    public TableManager(BistroServer server) {
        this.server = server;
    }

    /**
     * Adds a new table to the restaurant immediately.
     * @param req The request containing the capacity for the new table.
     * @return true if the table was added successfully, false otherwise.
     */
    public synchronized boolean addNewTable(AddTableRequest req) {
        Connection conn = ConnectionPool.getInstance().getConnection();
        String sql = "INSERT INTO `table` (table_number, number_of_seats, current_order, pending_removal) VALUES (?, ?, 0, FALSE)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, getNextTableId(conn));
            stmt.setInt(2, req.getCap());
            stmt.executeUpdate();
            server.refreshCurrentState();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            ConnectionPool.getInstance().returnConnection(conn);
        }
    }

    /**
     * Attempts to remove a table from the restaurant.
     * @param req The request containing the ID of the table to remove.
     * @return A status message describing the outcome (Immediate removal, Deferred removal, or Error).
     */
    public String removeTable(RemoveTableRequest req) {
        int tableId = req.getId();
        StringBuilder sb = new StringBuilder();
        Set<Integer> conflicts = new HashSet<>();

        // Pass 0 to simulate full removal
        if (!isSafeToReconfigure(tableId, 0, conflicts)) {
            sb.append("ABORT: Removing Table " + tableId + " conflicts with orders:");
            for (int o : conflicts) sb.append(" " + o);
            return sb.toString();
        }
        
        if (isTableOccupied(tableId)) {
            markForPendingRemoval(tableId);
            return "Table " + tableId + " marked for removal after current order.";
        } else {
            hardDeleteTable(tableId);
            return "Table " + tableId + " removed immediately.";
        }
    }

    /**
     * Updates the seating capacity of an existing table.
     * If the capacity is being reduced, an an algorithm is run to ensure that future bookings assigned
     * to this table can still be accommodated.
     * * @param tableId The ID of the table to update.
     * @param newCapacity The new number of seats.
     * @return A string indicating success or failure.
     */
    public String updateTableCapacity(int tableId, int newCapacity) {
        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            int oldCapacity = -1;
            String getCapSql = "SELECT number_of_seats FROM `table` WHERE table_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getCapSql)) {
                stmt.setInt(1, tableId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) oldCapacity = rs.getInt("number_of_seats");
            }
            if (oldCapacity == -1) return "Error: Table not found.";

            // If shrinking, check safety
            if (newCapacity < oldCapacity) {
                Set<Integer> conflicts = new HashSet<>();
                if (!isSafeToReconfigure(tableId, newCapacity, conflicts)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("ABORT: Shrinking Table ").append(tableId).append(" conflicts with orders:");
                    for (int o : conflicts) sb.append(" ").append(o);
                    return sb.toString();
                }
            }

            String updateSql = "UPDATE `table` SET number_of_seats = ? WHERE table_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, newCapacity);
                stmt.setInt(2, tableId);
                stmt.executeUpdate();
            }
            server.refreshCurrentState();
            return "Table updated.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database Error.";
        } finally {
            ConnectionPool.getInstance().returnConnection(conn);
        }
    }
    
    // --- HELPER CLASSES & METHODS ---

    /**
     * Internal helper class to represent a table snapshot during simulation.
     */
    private static class SimTable {
        int id;
        int capacity;
        public SimTable(int id, int capacity) { this.id = id; this.capacity = capacity; }
    }

    /**
     * Internal helper class to represent an order snapshot during simulation.
     */
    private static class SimOrder {
        int id;
        int guests;
        String status;
        int currentTableId; // 0 if not seated
        public SimOrder(int id, int guests, String status, int currentTableId) {
            this.id = id; this.guests = guests; this.status = status; this.currentTableId = currentTableId;
        }
    }

    /**
     * Simulates the restaurant state for all future time slots to check if a configuration change is safe.
     * * @param targetTableId The table being modified/removed.
     * @param proposedCapacity The new capacity (pass 0 if removing).
     * @param conflicts A set to collect IDs of orders that would be displaced.
     * @return true if the change is safe, false if it causes conflicts.
     */
    private boolean isSafeToReconfigure(int targetTableId, int proposedCapacity, Set<Integer> conflicts) {
        Connection conn = ConnectionPool.getInstance().getConnection();
        boolean isSafe = true;

        try {
            // A. Get all OTHER tables (We now need ID + Capacity)
            List<SimTable> virtualTableList = new ArrayList<>();
            String sqlTables = "SELECT table_number, number_of_seats FROM `table` WHERE table_number != ? AND pending_removal = FALSE";
            try (PreparedStatement stmt = conn.prepareStatement(sqlTables)) {
                stmt.setInt(1, targetTableId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    virtualTableList.add(new SimTable(rs.getInt("table_number"), rs.getInt("number_of_seats")));
                }
            }

            // If Update: Add the target table back with NEW capacity
            if (proposedCapacity > 0) {
                virtualTableList.add(new SimTable(targetTableId, proposedCapacity));
            }

            // B. Get Time Slots
            List<Timestamp> timeSlots = new ArrayList<>();
            String sqlTimes = "SELECT DISTINCT order_datetime FROM `order` WHERE status = 'OPEN' OR status = 'WAITING'";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlTimes)) {
                while (rs.next()) timeSlots.add(rs.getTimestamp("order_datetime"));
            }

            // C. Simulate
            for (Timestamp slot : timeSlots) {
                if (!canFitReservationsInSlot(conn, slot, virtualTableList, conflicts)) {
                    isSafe = false;
                }
            }
            return isSafe;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            ConnectionPool.getInstance().returnConnection(conn);
        }
    }

    /**
     * Runs a two-phase algorithm to match orders to tables for a specific time slot.
     * Phase 1: Secures tables for customers currently eating.
     * Phase 2: Matches waiting customers to remaining tables.
     */
    private boolean canFitReservationsInSlot(Connection conn, Timestamp startTime, List<SimTable> allTables, Set<Integer> conflicts) throws SQLException {
        // 1. Fetch Orders AND their current table (if they are eating)
        String sqlRes = "SELECT o.order_number, o.number_of_guests, o.status, t.table_number " +
                        "FROM `order` o " +
                        "LEFT JOIN `table` t ON t.current_order = o.order_number " +
                        "WHERE o.order_datetime <= ? AND DATE_ADD(o.order_datetime, INTERVAL 2 HOUR) > ? " +
                        "AND (o.status ='OPEN' OR o.status = 'WAITING')";

        List<SimOrder> openOrders = new ArrayList<>();
        List<SimOrder> waitingOrders = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sqlRes)) {
            stmt.setTimestamp(1, startTime);
            stmt.setTimestamp(2, startTime);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                SimOrder order = new SimOrder(
                    rs.getInt("order_number"),
                    rs.getInt("number_of_guests"),
                    rs.getString("status"),
                    rs.getInt("table_number")
                );
                
                if ("OPEN".equals(order.status) && order.currentTableId > 0) {
                	openOrders.add(order);
                } else {
                    waitingOrders.add(order);
                }
            }
        }

        // Create a mutable copy of tables for this slot
        List<SimTable> availableTables = new ArrayList<>(allTables);
        boolean slotSuccess = true;

        // --- PHASE 1: Secure Incumbents ---
        // People already eating MUST stay at their table (unless we deleted it).
        for (SimOrder seated : openOrders) {
            SimTable myTable = null;
            // Find the specific table this person is sitting at
            for (SimTable t : availableTables) {
                if (t.id == seated.currentTableId) {
                    myTable = t;
                    break;
                }
            }

            if (myTable != null) {
                // Table exists in our virtual pool. Check if it's still big enough (in case of shrink update)
                if (myTable.capacity >= seated.guests) {
                    availableTables.remove(myTable); // Table is claimed!
                } else {
                    conflicts.add(seated.id); // Table shrank and they don't fit!
                    slotSuccess = false;
                }
            } else {
                // If table is missing, it implies it is the one being removed.
            	continue;
            }
        }

        // --- PHASE 2: Fit Challengers ---
        // Now use Greedy Algorithm for the waiting list with whatever is left
        waitingOrders.sort((o1, o2) -> Integer.compare(o2.guests, o1.guests)); // Descending size
        availableTables.sort(Comparator.comparingInt(t -> t.capacity)); // Smallest fit first

        for (SimOrder waiter : waitingOrders) {
            boolean matched = false;
            for (int i = 0; i < availableTables.size(); i++) {
                if (availableTables.get(i).capacity >= waiter.guests) {
                    availableTables.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                conflicts.add(waiter.id);
                slotSuccess = false;
            }
        }

        return slotSuccess;
    }

    // --- Utilities ---
    
    private boolean isTableOccupied(int tableId) {
        Map<Table, Order> currentBistro = server.getCurrentBistro();
        for (Table t : currentBistro.keySet()) {
            if (t.getId() == tableId) return t.isTaken();
        }
        return false;
    }

    private void markForPendingRemoval(int tableId) {
        Connection conn = ConnectionPool.getInstance().getConnection();
        String sql = "UPDATE `table` SET pending_removal = TRUE WHERE table_number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tableId);
            stmt.executeUpdate();
            for(Table t : server.getCurrentBistro().keySet()) {
                if(t.getId() == tableId) t.setPendingRemoval(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.getInstance().returnConnection(conn);
        }
    }

    public void hardDeleteTable(int tableId) {
        Connection conn = ConnectionPool.getInstance().getConnection();
        String sql = "DELETE FROM `table` WHERE table_number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tableId);
            stmt.executeUpdate();
            server.refreshCurrentState();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.getInstance().returnConnection(conn);
        }
    }

    /**
     * Checks if a table is currently marked for pending removal.
     */
    public boolean isPendingRemoval(int tableId) {
        Connection conn = ConnectionPool.getInstance().getConnection();
        String sql = "SELECT pending_removal FROM `table` WHERE table_number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tableId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getBoolean("pending_removal");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.getInstance().returnConnection(conn);
        }
        return false;
    }

    private synchronized int getNextTableId(Connection conn) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT IFNULL(MAX(table_number), 0) + 1 FROM `table`");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}