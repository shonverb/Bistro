package bistro_server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import entities.Order;
import entities.requests.CancelRequest;
import entities.requests.CheckConfCodeRequest;
import entities.requests.LeaveTableRequest;
import entities.requests.ReadRequest;
import entities.requests.Request;
import entities.requests.ShowTakenSlotsRequest;

public class OrderManager {
	BistroServer server;
	public OrderManager(BistroServer bistroServer) {
		this.server = bistroServer;
	}


	/**
	 * the method adds an order to the database
	 * 
	 * @param o     The order to add
	 * @param query The insert query
	 * @return The resulting string, a message to the user
	 */
	public String addOrder(Order o, String query) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setInt(1, Integer.parseInt(o.getOrderNumber()));
			stmt.setTimestamp(2, Timestamp.valueOf(o.getOrderDateTime()));
			stmt.setInt(3, Integer.parseInt(o.getNumberOfGuests()));
			stmt.setInt(4, Integer.parseInt(o.getConfirmationCode()));

			int subId = 0;
			try {
				subId = Integer.parseInt(o.getSubscriberId());
			} catch (Exception ignored) {
			}
			if (subId == 0)
				stmt.setNull(5, Types.INTEGER);
			else
				stmt.setInt(5, subId);

			stmt.setDate(6, Date.valueOf(o.getDateOfPlacingOrder()));
			stmt.setString(7, o.getContact());
			stmt.setString(8, "OPEN");

			int rows = stmt.executeUpdate();
			if (rows == 1) {
				System.out.println("Order added to DB: " + o.getOrderNumber());
				return "✅ Order saved successfully!\nOrder Number: " + o.getOrderNumber() + "\nConfirmation Code: "
						+ o.getConfirmationCode();
			}
			return "❌ Order was not saved.";

		} catch (SQLException e) {
			e.printStackTrace();
			return "❌ Database error: " + e.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			// return "❌ Error saving order.";
			return "❌ ERROR: " + e.getClass().getName() + " | " + e.getMessage();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}
	}

	
	/**
	 * the method gets the next order number
	 * 
	 * @return The resulting string, the next order number
	 */
	public synchronized String OrderNumber() {
		Connection conn = ConnectionPool.getInstance().getConnection();
		try (PreparedStatement stmt = conn
				.prepareStatement("SELECT IFNULL(MAX(order_number), 0) + 1 AS next_num FROM `order`");
				ResultSet rs = stmt.executeQuery()) {

			if (rs.next())
				return rs.getString(1);

		} catch (SQLException e) {
			e.printStackTrace();
			return "ERROR:" + e.getMessage();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}
		return "";
	}

	/**
	 * the method gets an order from the database
	 * 
	 * @param r A ReadRequest
	 * @return The resulting string, a message or the order details
	 */
	public String getOrder(Request r) {
		String query = r.getQuery();
		String orderNum = ((ReadRequest) r).getOrderNum();
		String result = "Results:\n";
		Connection conn = ConnectionPool.getInstance().getConnection();

		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, orderNum);
			ResultSet rs = stmt.executeQuery();

			if (!rs.next())
				return "No order found.";

			do {
				result += "Order Number: " + rs.getString(1) + "\n";
				result += "Order DateTime: " + rs.getString(2) + "\n";
				result += "Guests: " + rs.getString(3) + "\n";
				result += "Confirmation Code: " + rs.getString(4) + "\n";
				result += "Subscriber ID: " + rs.getString(5) + "\n";
				result += "Placed On: " + rs.getString(6) + "\n";
			} while (rs.next());

		} catch (SQLException e) {
			e.printStackTrace();
			return "❌ Error reading order.";
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return result;
	}
	/**
	 * the method cancels an order in the database
	 * 
	 * @param r A CancelRequest
	 * @return The resulting string, a message to the user
	 */
	public String cancelOrder(Request r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String code = ((CancelRequest) r).getCode();
		String query = r.getQuery();
		for(Order o : server.getCurrentBistro().values()) {
			if(o!=null && o.getConfirmationCode().equals(code))
			return "Cannot cancel order: Customer is already in the bistro.";
		}

		try (PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_UPDATABLE)) {

			stmt.setString(1, code);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					String status = rs.getString("status");

					if ("CANCELLED".equals(status)) {
						return "Order is already cancelled";
					}

					rs.updateString("status", "CANCELLED");

					rs.updateRow();

					return "Order Cancelled";
				} else {
					return "Order not found";
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return "Error: " + e.getMessage();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

	}
	
	/**
	 * the method gets the order history from the database
	 * 
	 * @param r A Request containing the select query
	 * @return The resulting ArrayList, a message to the user
	 */
	public List<Order> getOrderHistory(Request r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = r.getQuery();
		List<Order> result = new ArrayList<>();
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				ArrayList<String> args = new ArrayList<>();
				args.add(rs.getString("order_number"));
				args.add(rs.getString("order_datetime"));
				args.add(rs.getString("number_of_guests"));
				args.add(rs.getString("confirmation_code"));
				args.add(rs.getString("subscriber_id"));
				args.add(rs.getString("date_of_placing_order"));
				args.add(rs.getString("contact"));
				Order o = new Order(args,1);
				o.setStatus(rs.getString("status"));
				result.add(o);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return result;
	}

	/**
	 * the method gets all active orders from the database
	 * 
	 * @param r A Request containing the select query
	 * @return The resulting Order List, a message to the user
	 */
	public List<Order> getAllActiveOrders(Request r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = r.getQuery();
		List<Order> result = new ArrayList<>();
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				ArrayList<String> args = new ArrayList<>();
				args.add(rs.getString("order_number"));
				args.add(rs.getString("order_datetime"));
				args.add(rs.getString("number_of_guests"));
				args.add(rs.getString("confirmation_code"));
				args.add(rs.getString("subscriber_id"));
				args.add(rs.getString("date_of_placing_order"));
				args.add(rs.getString("contact"));
				Order o = new Order(args,1);
				o.setStatus(rs.getString("status"));
				result.add(o);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return result;
	}

	/**
	 * the method gets an order from the database using confirmation code
	 * 
	 * @param query    The select query
	 * @param confCode The confirmation code
	 * @return The resulting string, a message or the order details
	 */
	public String getOrderFromConfCode(String query, String confCode) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, confCode);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getString("order_number") + "," + rs.getString("order_datetime") + ","
						+ rs.getString("number_of_guests") + "," + rs.getString("confirmation_code") + ","
						+ rs.getString("subscriber_id") + "," + rs.getString("date_of_placing_order") + ","
						+ rs.getString("contact");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return "Not found";
	}

	/**
	 * the method closes an order in the database
	 * 
	 * @param r A LeaveTableRequest
	 * @return The resulting string, a message to the user
	 */
	public String closeOrder(LeaveTableRequest r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = r.getQuery();
		String confcode = r.getConfCode();
		try {
			PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			stmt.setInt(1, Integer.parseInt(confcode));
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				rs.updateString("status", "CLOSED");
				rs.updateTimestamp("leave_time", Timestamp.valueOf(LocalDateTime.now()));
				rs.updateRow();
				return rs.getString("subscriber_id");
			} else {
				return "Not found";
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return "Error";
	}

	/**
	 * the method expires pending orders in the database
	 * 
	 * @param OrdersInBistro A set of order numbers currently in the bistro
	 * @return A map of expired order numbers and their associated contacts
	 */
	protected Map<String, String> ExpirePendingOrders(Set<String> OrdersInBistro) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		Map<String, String> expiredOrders = new HashMap<>();
		String query = "SELECT order_number, contact, status FROM `order` WHERE status = 'OPEN' AND order_datetime <= ?;";
		LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);
		try {
			PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			stmt.setTimestamp(1, Timestamp.valueOf(expirationTime));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				if (!OrdersInBistro.contains(rs.getString("order_number"))) {
					rs.updateString("status", "CANCELLED");
					rs.updateRow();
					String orderNumber = rs.getString("order_number");
					String contact = rs.getString("contact");
					expiredOrders.put(orderNumber, contact);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return expiredOrders;
	}

	/**
	 * the method gets orders that need notification in the database
	 * 
	 * @return A map of order numbers and their associated contacts for notification
	 */
	protected Map<String, String> OrdersToNotify() {
		Connection conn = ConnectionPool.getInstance().getConnection();
		Map<String, String> contacts = new HashMap<>();
		String query = "SELECT order_number, contact FROM `order` WHERE status = 'OPEN' AND order_datetime = ?;";
		LocalDateTime notificationTime = LocalDateTime.now().plusHours(2);
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setTimestamp(1, Timestamp.valueOf(notificationTime));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				String orderNumber = rs.getString("order_number");
				String contact = rs.getString("contact");
				contacts.put(orderNumber, contact);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return contacts;
	}

	/**
	 * call this when user arrives at the terminal (entrance)
	 * 
	 * @param orderNumber The order number
	 */
	public void markArrivalAtTerminal(String orderNumber) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = "UPDATE `order` SET actual_arrival = ? WHERE order_number = ? AND actual_arrival IS NULL";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			stmt.setInt(2, Integer.parseInt(orderNumber));
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

	}

	/**
	 * call this when user is seated at the table
	 * 
	 * @param orderNumber The order number
	 */
	public void markOrderAsSeated(String orderNumber) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = "UPDATE `order` SET seated_time = ? WHERE order_number = ? AND seated_time IS NULL";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			stmt.setInt(2, Integer.parseInt(orderNumber));
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

	}

	/**
	 * the method changes the status of an order in the database
	 * 
	 * @param status      The new status
	 * @param orderNumber The order number
	 */
	public void changeStatus(String status, String orderNumber) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = "UPDATE `order` SET status = ? WHERE order_number = ?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, status);
			stmt.setInt(2, Integer.parseInt(orderNumber));
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

	}

	/**
	 * Sets an order type to the type given
	 * 
	 * @param orderNum the order number to set the type to
	 * @param type     the type to set the order to
	 */
	public void setOrderType(String orderNum, String type) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = "UPDATE `order` SET type_of_order = ? WHERE order_number = ? AND type_of_order IS NULL;";
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, type);
			stmt.setInt(2, Integer.parseInt(orderNum));
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

	}

	/**
	 * the method gets all waiting orders of a specific type from the database
	 * @param OrderType
	 * @return A string containing all waiting orders of the specified type
	 */
	public String getWaitingOrders(String OrderType) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = "SELECT * FROM `order` WHERE status = 'WAITING' AND type_of_order = ?";
		StringBuilder sb = new StringBuilder();
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, OrderType);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				sb.append(rs.getString("order_number")).append(",").append(rs.getString("order_datetime")).append(",")
						.append(rs.getString("number_of_guests")).append(",").append(rs.getString("confirmation_code"))
						.append(",").append(rs.getString("subscriber_id")).append(",")
						.append(rs.getString("date_of_placing_order")).append(",").append(rs.getString("contact"))
						.append("\n");
			}
		} catch (SQLException e) {
			//e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return sb.isEmpty() ? "" : sb.toString();
	}

	/**
	 * the method cancels orders outside of opening hours in the database
	 * @param conn
	 * @param date
	 * @param openTime
	 * @param closeTime
	 * @throws SQLException
	 */
	public void cancelOrdersOutsideHours(Connection conn, String date, String openTime, String closeTime) throws SQLException {
	    System.out.println("DEBUG: Cancelling orders outside hours. Input: " + date + " | Open: " + openTime + " | Close: " + closeTime);
	    List<String> cnl = new ArrayList<>();

	    // 1. Determine if input is a specific date (YYYY-MM-DD) or a Day of Week
	    // Regex checks for format ####-##-##
	    boolean isSpecificDate = date.matches("\\d{4}-\\d{2}-\\d{2}");

	    // 2. Build the WHERE clause dynamically based on input type
	    String dateFilter;
	    if (isSpecificDate) {
	        // Look for exact date match
	        dateFilter = "DATE(order_datetime) = ?";
	    } else {
	        // Look for Day of Week match (e.g., all Fridays)
	        dateFilter = "DAYOFWEEK(order_datetime) = ?"; 
	    }

	    String baseWhere = dateFilter 
	                     + " AND (TIME(order_datetime) < ? OR TIME(order_datetime) >= ?)"
	                     + " AND status IN ('OPEN', 'WAITING')";

	    String selectSql = "SELECT contact FROM `order` WHERE " + baseWhere;

	    try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
	        // 3. Set the first parameter based on type
	        if (isSpecificDate) {
	            stmt.setDate(1, Date.valueOf(date));
	        } else {
	            // If date is "6", parse it as int.
	            stmt.setInt(1, Integer.parseInt(date));
	        }
	        
	        stmt.setTime(2, Time.valueOf(openTime));
	        stmt.setTime(3, Time.valueOf(closeTime));
	        
	        System.out.println("DEBUG: Executing select query: " + stmt.toString());

	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            cnl.add(rs.getString("contact"));
	        }
	    }


	    EmailService emailService = EmailService.getInstance();
	    for (String email : cnl) {
	        if (email.contains("@")) {
	            String subject = "Order Cancellation Notice";
	            String body = "Dear Customer,\n\n" 
	                        + "We regret to inform you that your order has been cancelled due to a change in our operating hours (" 
	                        + (isSpecificDate ? date : "Weekly Schedule Change") + ").\n"
	                        + "Best regards,\nBistro Team";
	            emailService.sendEmail(email, subject, body);
	        } else {
	            ServerUI.updateInScreen("Order cancelled notice sent to: " + email);
	        }
	    }

	    String updateSql = "UPDATE `order` SET status = 'CANCELLED' WHERE " + baseWhere;
	    
	    try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
	        if (isSpecificDate) {
	            stmt.setDate(1, Date.valueOf(date));
	        } else {
	            stmt.setInt(1, Integer.parseInt(date));
	        }
	        stmt.setTime(2, Time.valueOf(openTime));
	        stmt.setTime(3, Time.valueOf(closeTime));
	        stmt.executeUpdate();
	    }
	    System.out.println("DEBUG: Orders updated to CANCELLED.");
	}
	/**
	 * the method cancels all orders for a specific date in the database
	 * 
	 * @param conn A database connection
	 * @param date The specific date
	 * @throws SQLException
	 */
	public void cancelAllOrdersForDate(Connection conn, String date) throws SQLException {
	    List<String> cnl = new ArrayList<>();

	    String selectSql = """
	        SELECT contact
	        FROM `order`
	        WHERE DATE(order_datetime) = ?
	          AND status IN ('OPEN', 'WAITING')
	    """;

	    try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
	        stmt.setDate(1, Date.valueOf(date));

	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            cnl.add(rs.getString("contact"));
	        }
	    }

	    EmailService emailService = EmailService.getInstance();
	    for (String contact : cnl) {
	        if (contact.contains("@")) {
	            String subject = "Order Cancellation Notice";
	            String body =
	                    "Dear Customer,\n\n" +
	                    "We regret to inform you that your order scheduled for " + date +
	                    " has been cancelled due to the restaurant being closed on this date.\n\n" +
	                    "Best regards,\n" +
	                    "Bistro Team";
	            emailService.sendEmail(contact, subject, body);
	        } else {
	            ServerUI.updateInScreen(
	                "Order cancelled notice sent to phone number " + contact
	            );
	        }
	    }

	    String updateSql = """
	        UPDATE `order`
	        SET status = 'CANCELLED'
	        WHERE DATE(order_datetime) = ?
	          AND status IN ('OPEN', 'WAITING')
	    """;

	    try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
	        stmt.setDate(1, Date.valueOf(date));
	        stmt.executeUpdate();
	    }
	}
	
	/**
	 * the method cancels all orders for a specific day of the week in the database
	 * 
	 * @param conn      A database connection
	 * @param dayOfWeek The specific day of the week
	 * @throws SQLException
	 */
	public void cancelAllOrdersForDay(Connection conn, int dayOfWeek) throws SQLException {
	    List<String> cnl = new ArrayList<>();

	    String selectSql = """
	        SELECT contact
	        FROM `order`
	        WHERE DAYOFWEEK(order_datetime) = ?
	          AND order_datetime >= NOW()
	          AND status IN ('OPEN', 'WAITING')
	    """;

	    try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
	        stmt.setInt(1, dayOfWeek);

	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            cnl.add(rs.getString("contact"));
	        }
	    }

	    EmailService emailService = EmailService.getInstance();
	    for (String contact : cnl) {
	        if (contact.contains("@")) {
	            String subject = "Order Cancellation Notice";
	            String body =
	                    "Dear Customer,\n\n" +
	                    "We regret to inform you that your order has been cancelled " +
	                    "due to the restaurant being closed on this day of the week.\n\n" +
	                    "Best regards,\n" +
	                    "Bistro Team";
	            emailService.sendEmail(contact, subject, body);
	        }
	    }

	    String updateSql = """
	        UPDATE `order`
	        SET status = 'CANCELLED'
	        WHERE DAYOFWEEK(order_datetime) = ?
	          AND order_datetime >= NOW()
	          AND status IN ('OPEN', 'WAITING')
	    """;

	    try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
	        stmt.setInt(1, dayOfWeek);
	        stmt.executeUpdate();
	    }
	}

	/**
	 * the method checks for confirmation codes related to a contact in a given time
	 * frame
	 * 
	 * @param r A CheckConfCodeRequest
	 * @return The resulting string, a message to the user
	 */
	public String checkConfCode(Request r) {
		CheckConfCodeRequest req = (CheckConfCodeRequest) r;
		String res = "";
		Connection conn = ConnectionPool.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(r.getQuery())) {
			stmt.setString(1, req.getcontact());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				res += rs.getString(1) + "\n";
			}
			return res;

		} catch (SQLException e) {
			e.printStackTrace();
			return "ERROR:" + e.getMessage();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

	}

	/**
	 * the method gets all taken slots in a given time frame
	 * 
	 * @param r A ShowTakenSlotsRequest
	 * @return The resulting string, a message to the user
	 */
	public String getTakenSlots(Request r) {
		ShowTakenSlotsRequest req = (ShowTakenSlotsRequest) r;
		LocalDateTime from = req.getFrom();
		LocalDateTime to = req.getTo();
		Connection conn = ConnectionPool.getInstance().getConnection();
		System.out.println("DEBUG: Querying DB for status='OPEN' between: " + from + " AND " + to);
		try (PreparedStatement stmt = conn.prepareStatement(r.getQuery())) {
			stmt.setTimestamp(1, Timestamp.valueOf(from));
			stmt.setTimestamp(2, Timestamp.valueOf(to));

			try (ResultSet rs = stmt.executeQuery()) {
				StringBuilder sb = new StringBuilder();
				while (rs.next()) {
					sb.append(rs.getString(1)).append(":").append(rs.getString(2)).append(",");
					System.out.println("DBCONNECTOR: in getTakenSlots, added an entry to result");
				}
				return sb.toString();
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return "ERROR:" + e.getMessage();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

	}

}
