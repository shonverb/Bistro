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
        Set<SimOrder> conflicts = new HashSet<>();

        // Pass 0 to simulate full removal
        if (!isSafeToReconfigure(tableId, 0, conflicts)) {
            sb.append("Removing Table " + tableId + " conflicts with orders to be cancelled:");
            for (SimOrder o : conflicts) {
            	sb.append(" " + o.id);
            	server.dbcon.cancelOrder(new CancelRequest(o.confirmationCode));
            	if(o.contact.contains("@")) {
        			EmailService.getInstance().sendEmail(o.contact, "Your Order (#"+(o.id)+")"
        					,"Dear customer,\n\n unfortunately, due to changes in our tables capacity and/or quantity, we cannot accomodate you and your guests today,\nplease come again at a future time");
            	}
            	else {
            		ServerUI.updateInScreen("Order number "+o.id+" cancelled because of table change");
            	}
            	
            }
            
        	
        }
        
        if (isTableOccupied(tableId)) {
            markForPendingRemoval(tableId);
            return "Table " + tableId + " marked for removal after current order.";
        } else {
            hardDeleteTable(tableId);
            return "Table " + tableId + " Removed.";
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
                Set<SimOrder> conflicts = new HashSet<>();
                if (!isSafeToReconfigure(tableId, newCapacity, conflicts)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("ABORT: Shrinking Table ").append(tableId).append(" conflicts with orders:");
                    for (SimOrder o : conflicts) {
                    	sb.append(" ").append(o.id);
                    	server.dbcon.cancelOrder(new CancelRequest(o.confirmationCode));
                    	if(o.contact.contains("@")) {
                			EmailService.getInstance().sendEmail(o.contact, "Your Order (#"+(o.id)+")"
                					,"Dear customer,\n\n unfortunately, due to changes in our tables capacity and/or quantity, we cannot accomodate you and your guests today,\nplease come again at a future time");
                    	}else {
                    		ServerUI.updateInScreen("Order number "+o.id+" cancelled because of table change");
                    	}
                    }
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
        int currentTableId; // 0 if not seated
        String confirmationCode;
        String contact;
        public SimOrder(int id, int guests, int currentTableId, String confirmationCode, String contact) {
            this.id = id; this.guests = guests; this.currentTableId = currentTableId; this.confirmationCode=confirmationCode;
            this.contact = contact;
        }
    }

    /**
     * Simulates the restaurant state for all future time slots to check if a configuration change is safe.
     * * @param targetTableId The table being modified/removed.
     * @param proposedCapacity The new capacity (pass 0 if removing).
     * @param conflicts A set to collect IDs of orders that would be displaced.
     * @return true if the change is safe, false if it causes conflicts.
     */
    private boolean isSafeToReconfigure(int targetTableId, int proposedCapacity, Set<SimOrder> conflicts) {
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
            int maxTableSize = 0;
            for(SimTable t : virtualTableList) {
            	if(t.capacity>maxTableSize) maxTableSize = t.capacity;
            }

            // If Update: Add the target table back with NEW capacity
            if (proposedCapacity > 0) {
                virtualTableList.add(new SimTable(targetTableId, proposedCapacity));
                maxTableSize = Math.max(proposedCapacity, maxTableSize);
            }
            String sqlWaiting = "SELECT confirmation_code, contact FROM `order` WHERE status = `WAITING` AND number_of_guests > "+maxTableSize;
            try {
            	Statement stmt = conn.createStatement();
            	ResultSet rs = stmt.executeQuery(sqlWaiting);
            	while(rs.next()) {
            		server.dbcon.cancelOrder(new CancelRequest(rs.getString("confirmation_code")));
            		String contact = rs.getString("contact");
            		if(contact.contains("@")) {
            			EmailService.getInstance().sendEmail(contact, "Your Order (#"+(Integer.parseInt(rs.getString("confirmation_code"))-1000)+")"
            					,"Dear customer,\n\n unfortunately, due to changes in our tables capacity and/or quantity, we cannot accomodate you and your guests today,\nplease come again at a future time");
            		}
            	}
            } catch (SQLException e) {
            	e.printStackTrace();
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

    /**
     * Runs a two-phase algorithm to match orders to tables for a specific time slot.
     * Phase 1: Secures tables for customers currently eating (Incumbents).
     * Phase 2: Matches UPCOMING open orders to remaining tables.
     */
    private boolean canFitReservationsInSlot(Connection conn, Timestamp startTime, List<SimTable> allTables, Set<SimOrder> conflicts) throws SQLException {
        // 1. Fetch Orders AND their current table (if they are eating)
        String sqlRes = "SELECT o.order_number, o.number_of_guests t.table_number, o.confirmation_code, o.contact " +
                        "FROM `order` o " +
                        "LEFT JOIN `table` t ON t.current_order = o.order_number " +
                        "WHERE o.order_datetime <= ? AND DATE_ADD(o.order_datetime, INTERVAL 2 HOUR) > ? " +
                        "AND o.status ='OPEN'";

        List<SimOrder> incumbents = new ArrayList<>(); // People eating NOW
        List<SimOrder> upcoming = new ArrayList<>();   // Reservations coming later

        try (PreparedStatement stmt = conn.prepareStatement(sqlRes)) {
            stmt.setTimestamp(1, startTime);
            stmt.setTimestamp(2, startTime);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                SimOrder order = new SimOrder(
                    rs.getInt("order_number"),
                    rs.getInt("number_of_guests"),
                    rs.getInt("table_number"), 
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

        // Create a mutable copy of tables for this slot
        List<SimTable> availableTables = new ArrayList<>(allTables);
        boolean slotSuccess = true;

        // --- PHASE 1: Secure Incumbents (People Eating) ---
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
                // If table is missing, it's the one being removed. 
                continue;
            }
        }

        // --- PHASE 2: Fit Upcoming Reservations ---
        // We must check if the remaining tables can hold the reservations
        upcoming.sort((o1, o2) -> Integer.compare(o2.guests, o1.guests)); // Largest groups first
        availableTables.sort(Comparator.comparingInt(t -> t.capacity));   // Smallest tables first

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
                conflicts.add(res); // Conflict: Reservation has no table!
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