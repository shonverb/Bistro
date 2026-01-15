package bistro_server;

import java.sql.*;
import java.util.*;
import entities.requests.AddTableRequest;
import entities.requests.CancelRequest;
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
     * Attempts to remove a table. Checks for conflicts and cancels them if necessary.
     */
    public String removeTable(RemoveTableRequest req) {
        int tableId = req.getId();
        Set<SimOrder> conflicts = new HashSet<>();

        // 1. Simulation Check
        if (!isSafeToReconfigure(tableId, 0, conflicts)) {
            // 2. Handle Conflicts (Cancel & Email)
            processCancellations(conflicts, "Removing Table " + tableId);
            
            StringBuilder sb = new StringBuilder();
            sb.append("Removing Table " + tableId + " conflicts with orders to be cancelled:");
            for (SimOrder o : conflicts) sb.append(" " + o.id);
            return sb.toString();
        }
        
        // 3. Execution (Graceful vs Immediate)
        if (isTableOccupied(tableId)) {
            markForPendingRemoval(tableId);
            return "Table " + tableId + " marked for removal after current order.";
        } else {
            hardDeleteTable(tableId);
            return "Table " + tableId + " Removed.";
        }
    }

    /**
     * Updates table capacity. Checks for conflicts and cancels them if necessary.
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
                Set<SimOrder> conflicts = new HashSet<>();
                if (!isSafeToReconfigure(tableId, newCapacity, conflicts)) {
                    // Handle Conflicts (Cancel & Email) using the helper method
                    processCancellations(conflicts, "Shrinking Table " + tableId);

                    StringBuilder sb = new StringBuilder();
                    sb.append("ABORT: Shrinking Table ").append(tableId).append(" conflicts with orders:");
                    for (SimOrder o : conflicts) sb.append(" ").append(o.id);
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
    
    // --- PRIVATE HELPERS ---

    /**
     * Helper to handle the cancellation logic (DB update + Email/UI).
     */
    private void processCancellations(Set<SimOrder> conflicts, String reason) {
        for (SimOrder o : conflicts) {
            server.dbcon.cancelOrder(new CancelRequest(o.confirmationCode));
            if(o.contact != null && o.contact.contains("@")) {
                EmailService.getInstance().sendEmail(o.contact, "Your Order (#"+(o.id)+")",
                    "Dear customer,\n\nunfortunately, due to changes in our tables capacity ("+reason+"), " +
                    "we cannot accomodate you and your guests today.\nPlease come again at a future time.");
            } else {
                ServerUI.updateInScreen("Order number "+o.id+" cancelled because of table change");
            }
        }
    }

    // --- SIMULATION LOGIC ---

    private static class SimTable {
        int id;
        int capacity;
        public SimTable(int id, int capacity) { this.id = id; this.capacity = capacity; }
    }

    private static class SimOrder {
        int id;
        int guests;
        int currentTableId; // 0 if not seated
        String confirmationCode;
        String contact;
        public SimOrder(int id, int guests, int currentTableId, String confirmationCode, String contact) {
            this.id = id; this.guests = guests; this.currentTableId = currentTableId; 
            this.confirmationCode=confirmationCode; this.contact = contact;
        }
        // Needed for HashSet uniqueness
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimOrder simOrder = (SimOrder) o;
            return id == simOrder.id;
        }
        @Override
        public int hashCode() { return Objects.hash(id); }
    }

    private boolean isSafeToReconfigure(int targetTableId, int proposedCapacity, Set<SimOrder> conflicts) {
        Connection conn = ConnectionPool.getInstance().getConnection();
        boolean isSafe = true;

        try {
            // A. Get all OTHER tables
            List<SimTable> virtualTableList = new ArrayList<>();
            String sqlTables = "SELECT table_number, number_of_seats FROM `table` WHERE table_number != ? AND pending_removal = FALSE";
            try (PreparedStatement stmt = conn.prepareStatement(sqlTables)) {
                stmt.setInt(1, targetTableId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    virtualTableList.add(new SimTable(rs.getInt("table_number"), rs.getInt("number_of_seats")));
                }
            }
            
            // Calculate Max Table Size (Current State)
            int maxTableSize = 0;
            for(SimTable t : virtualTableList) {
                if(t.capacity > maxTableSize) maxTableSize = t.capacity;
            }

            // If Update: Add the target table back with NEW capacity
            if (proposedCapacity > 0) {
                virtualTableList.add(new SimTable(targetTableId, proposedCapacity));
                maxTableSize = Math.max(proposedCapacity, maxTableSize);
            }
            
            // --- LOGIC: WAITING ORDERS CHECK ---
            // Cancel any WAITING order that is now strictly larger than our biggest table
            String sqlWaiting = "SELECT confirmation_code, contact, order_number, number_of_guests FROM `order` WHERE status = 'WAITING' AND number_of_guests > " + maxTableSize;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlWaiting)) {
                while(rs.next()) {
                    // We treat these as conflicts too, effectively canceling them
                    conflicts.add(new SimOrder(
                        rs.getInt("order_number"), 
                        rs.getInt("number_of_guests"), 
                        0, 
                        rs.getString("confirmation_code"), 
                        rs.getString("contact")
                    ));
                    isSafe = false; // We found conflicts
                }
            }

            // B. Get Time Slots
            List<Timestamp> timeSlots = new ArrayList<>();
            String sqlTimes = "SELECT DISTINCT order_datetime FROM `order` WHERE status = 'OPEN'";
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

    private boolean canFitReservationsInSlot(Connection conn, Timestamp startTime, List<SimTable> allTables, Set<SimOrder> conflicts) throws SQLException {
        String sqlRes = "SELECT o.order_number, o.number_of_guests, t.table_number, o.confirmation_code, o.contact " +
                        "FROM `order` o " +
                        "LEFT JOIN `table` t ON t.current_order = o.order_number " +
                        "WHERE o.order_datetime <= ? AND DATE_ADD(o.order_datetime, INTERVAL 2 HOUR) > ? " +
                        "AND o.status ='OPEN'";

        List<SimOrder> incumbents = new ArrayList<>(); 
        List<SimOrder> upcoming = new ArrayList<>();   

        try (PreparedStatement stmt = conn.prepareStatement(sqlRes)) {
            stmt.setTimestamp(1, startTime);
            stmt.setTimestamp(2, startTime);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                SimOrder order = new SimOrder(
                    rs.getInt("order_number"),
                    rs.getInt("number_of_guests"),
                    rs.getInt("table_number"), // 0 if null 
                    rs.getString("confirmation_code"),
                    rs.getString("contact")
                );
                
                // SEPARATION LOGIC:
                if (order.currentTableId > 0) {
                    incumbents.add(order); // Sitting
                } else {
                    upcoming.add(order);   // Not sitting (Future Reservation)
                }
            }
        }

        List<SimTable> availableTables = new ArrayList<>(allTables);
        boolean slotSuccess = true;

        // --- PHASE 1: Secure Incumbents ---
        for (SimOrder seated : incumbents) {
            SimTable myTable = null;
            for (SimTable t : availableTables) {
                if (t.id == seated.currentTableId) {
                    myTable = t;
                    break;
                }
            }

            if (myTable != null) {
                if (myTable.capacity >= seated.guests) {
                    availableTables.remove(myTable); 
                } else {
                    conflicts.add(seated); 
                    slotSuccess = false;
                }
            } else {
                // Graceful Removal: If table is missing, the incumbent is safe.
                continue;
            }
        }

        // --- PHASE 2: Fit Upcoming Reservations ---
        upcoming.sort((o1, o2) -> Integer.compare(o2.guests, o1.guests)); 
        availableTables.sort(Comparator.comparingInt(t -> t.capacity));   

        for (SimOrder res : upcoming) {
            boolean matched = false;
            for (int i = 0; i < availableTables.size(); i++) {
                if (availableTables.get(i).capacity >= res.guests) {
                    availableTables.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                conflicts.add(res); 
                slotSuccess = false;
            }
        }

        return slotSuccess;
    }    
    
    // --- UTILITIES ---
    
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