package bistro_server;

import java.util.TreeMap;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import entities.SpecificDate;
import entities.Order;
import entities.Subscriber;
import entities.Table;
import entities.User;
import entities.requests.CancelRequest;
import entities.requests.ChangeHoursDayRequest;
import entities.requests.CheckConfCodeRequest;
import entities.requests.CloseDateRequest;
import entities.requests.CloseDayRequest;
import entities.requests.GetReportsRequest;
import entities.requests.GetUserActiveOrdersRequest;
import entities.requests.LeaveTableRequest;
import entities.requests.LoginRequest;
import entities.requests.ReadRequest;
import entities.requests.RegisterRequest;
import entities.requests.Request;
import entities.requests.ShowTakenSlotsRequest;
import entities.requests.WriteHoursDateRequest;
import entities.Day;

/**
 * A class that handles all operations on the database, receiving requests and
 * handling them
 */
public class DBconnector {

	DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public DBconnector() {
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
	 * the method reads the email of a subscriber from the database
	 * 
	 * @param subId The subscriber ID
	 * @return The resulting string, the email or empty string if not found
	 */
	public String readEmail(String subId) {
		Connection conn = ConnectionPool.getInstance().getConnection();

		try {
			PreparedStatement stmt = conn.prepareStatement("SELECT email FROM `user` WHERE subscriber_id = ?");
			stmt.setInt(1, Integer.parseInt(subId));
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				String email = rs.getString(1);
				return email;
			}
			return "";

		} catch (SQLException e) {
			e.printStackTrace();
			return "";
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}
	}

	/**
	 * 
	 * @param r A LoginRequest
	 * @return The resulting string, a message or the subscriber if found
	 */
	public String checkLogin(Request r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = r.getQuery();
		int subcriberId = ((LoginRequest) r).getId();
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setInt(1, subcriberId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String res = "";
				for (int i = 1; i <= 5; i++) {
					res += rs.getString(i) + ",";
				}
				res += rs.getString(6);
				return res;
			} else {
				return "Not found";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return "";
	}

	/**
	 * 
	 * @param r a RegisterRequest to handle
	 * @return The resulting string, message to the user
	 */
	public String addNewUser(Request r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = r.getQuery();
		Subscriber user = ((RegisterRequest) r).getUser();
		System.out.println("In add new user");
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, user.getFirstName() + " " + user.getLastName());
			stmt.setInt(2, user.getSubscriberID());
			stmt.setString(3, user.getUserName());
			stmt.setString(4, user.getPhone());
			stmt.setString(5, user.getEmail());
			stmt.setString(6, user.getStatus());
			if (stmt.executeUpdate() == 0) {
				return "ERROR: Couldn't add the user, please try again";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "ERROR: Couldn't add the user, please try again";
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return "New user added successfully, please keep your ID handy for further login attempts\nUser is:\n" + user;

	}

	/**
	 * the method cancels an order in the database
	 * 
	 * @param r A CancelRequest
	 * @return The resulting string, a message to the user
	 */
	public String cancelOrder(Request r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		// IMPORTANT: The query in 'r' MUST be a SELECT statement now!
		// Example: "SELECT * FROM `order` WHERE confirmation_code = ?"
		String code = ((CancelRequest) r).getCode();
		String query = r.getQuery();

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
	 * the method gets all relevant tables from the database
	 * 
	 * @return A list of relevant tables
	 */
	public HashMap<Table, Order> getCurrentBistroState() {
		Connection conn = ConnectionPool.getInstance().getConnection();
		HashMap<Table, Order> currentBistro = new HashMap<>();
		try {
			PreparedStatement stmt = conn.prepareStatement(
					"SELECT * FROM `table` WHERE pending_removal = FALSE;");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("table_number");
				int capacity = rs.getInt("number_of_seats");
				int currentOrder = rs.getInt("current_order");
				if (currentOrder != 0) {
					try {
						PreparedStatement stmt2 = conn
								.prepareStatement("SELECT * FROM `order` WHERE order_number = ?;");
						stmt2.setInt(1, currentOrder);
						ResultSet rs2 = stmt2.executeQuery();
						ArrayList<String> order = new ArrayList<>();
						LocalDateTime orderSittingTime = null;
						if (rs2.next()) {
							order.add(rs2.getString("order_number"));
							order.add(rs2.getString("order_datetime"));
							order.add(rs2.getString("number_of_guests"));
							order.add(rs2.getString("confirmation_code"));
							order.add(rs2.getString("subscriber_id"));
							order.add(rs2.getString("date_of_placing_order"));
							order.add(rs2.getString("contact"));
							orderSittingTime = rs2.getObject("seated_time", LocalDateTime.class);

						}
						Order o = new Order(order, 0);
						o.setSittingtime(orderSittingTime);
						System.out.println(order);
						currentBistro.put(new Table(id, capacity, true), o);
					} catch (SQLException e) {
						//e.printStackTrace();
						ConnectionPool.getInstance().returnConnection(conn);
					}

				} else {
					currentBistro.put(new Table(id, capacity, false), null);
				}

			}
			return currentBistro;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return null;
	}

	/**
	 * the method updates details in the database
	 * 
	 * @param r A Request containing the update query
	 * @return The resulting string, a message to the user
	 */
	public String updateDetails(Request r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = r.getQuery();
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			int rowsUpdated = stmt.executeUpdate();
			if (rowsUpdated > 0)
				return "Details updated successfully.";
			else
				return "No details were updated.";
		} catch (SQLException e) {
			e.printStackTrace();
			return "Error updating details: " + e.getMessage();
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
	 * the method gets all subscribers from the database
	 * 
	 * @param r A Request containing the select query
	 * @return The resulting string, a message to the user
	 */
	public List<User> getAllSubscribers(Request r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = r.getQuery();
		List<User> result = new ArrayList<>();
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();


			while (rs.next()) {
				User cur;
				cur = new Subscriber(rs.getInt("subscriber_id"), rs.getString("username"),
							rs.getString("full_name").split(" ")[0], rs.getString("full_name").split(" ")[1],
							rs.getString("phone_number"), rs.getString("email"), rs.getString("status"), null);
				
				result.add(cur);
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
	 * the method changes the opening and closing hours for a specific day in the
	 * database
	 * 
	 * @param r A ChangeHoursDayRequest
	 * @return The resulting string, a message to the user
	 */
	public String changeHoursDay(Request r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		ChangeHoursDayRequest req = (ChangeHoursDayRequest) r;
		String openTime = String.format("%02d:00:00", Integer.parseInt(req.getOpen()));
		String closeTime = String.format("%02d:00:00", Integer.parseInt(req.getClose()));
		String query = req.getQuery();
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setTime(1, Time.valueOf(openTime));
			stmt.setTime(2, Time.valueOf(closeTime));
			stmt.setString(3, req.getStatus());
			stmt.setString(4, req.getDay());

			int rowsUpdated = stmt.executeUpdate();
			if (rowsUpdated > 0) {
				cancelOrdersOutsideHours(conn, req.getDay(), openTime, closeTime);
				return "Details updated successfully.";
			}
			else
				return "No details were updated.";
		} catch (SQLException e) {
			e.printStackTrace();
			return "Error updating details: " + e.getMessage();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return "Error: Invalid hour format.";
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

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
	 * the method writes opening and closing hours for a specific date in the
	 * database
	 * 
	 * @param r A WriteHoursDateRequest
	 * @return The resulting string, a message to the user
	 */
	public String writeHoursDate(Request r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		WriteHoursDateRequest req = (WriteHoursDateRequest) r;
		String openTime;
		String closeTime;
		try {
			openTime = String.format("%02d:00:00", Integer.parseInt(req.getOpen()));
			closeTime = String.format("%02d:00:00", Integer.parseInt(req.getClose()));
		} catch (NumberFormatException e) {
			return "Error: Invalid hour format.";
		}

		String query = req.getQuery();

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setDate(1, java.sql.Date.valueOf(req.getDate()));
			stmt.setTime(2, java.sql.Time.valueOf(openTime));
			stmt.setTime(3, java.sql.Time.valueOf(closeTime));
			stmt.setString(4, req.getStatus());

			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0) {
				cancelOrdersOutsideHours(conn, req.getDate(), openTime, closeTime);
				return "Hours for date inserted successfully.";
			}
			else
				return "No rows were inserted.";
		} catch (SQLException e) {
			e.printStackTrace();
			return "Error inserting hours for date: " + e.getMessage();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

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
	 * the method gets all tables from the database
	 * 
	 * @return A list of all tables
	 */
	public List<Table> getAllTables() {
		Connection conn = ConnectionPool.getInstance().getConnection();
		ArrayList<Table> tables = new ArrayList<>();
		try {
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `table` WHERE pending_removal = FALSE;");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("table_number");
				int capacity = rs.getInt("number_of_seats");
				tables.add(new Table(id, capacity, false));
			}
			return tables;
		} catch (SQLException e) {
			//e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return null;
	}

	/**
	 * the method gets all days' hours from the database
	 * 
	 * @param r A Request
	 * @return A list of all days with their hours
	 */
	public List<Day> getAllDaysHours(Request r) {
		System.out.println("DEBUG: In getAllDaysHours");
		Connection conn = ConnectionPool.getInstance().getConnection();
		ArrayList<Day> days = new ArrayList<>();
		try {
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `day`;");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int day = rs.getInt("day_of_week");
				Time open = rs.getTime("open_hour");
				Time close = rs.getTime("close_hour");
				Day dayObj = new Day(day, open, close);
				System.out.println("DEBUG: Retrieved day entry: " + day + ", open: " + open + ", close: " + close + ", closed: "
						+ rs.getString("status").equals("CLOSE"));
				dayObj.setClosed(rs.getString("status").equals("CLOSE"));
				days.add(dayObj);
			}
			return days;
		} catch (SQLException e) {
			System.out.println("ERROR in getAllDaysHours:");
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return null;
	}

	/**
	 * the method gets all specific dates' hours from the database
	 * 
	 * @param r A Request
	 * @return A list of all specific dates with their hours
	 */
	public List<SpecificDate> getAllDatesHours(Request r) {
		System.out.println("DEBUG: In getAllDatesHours");
		Connection conn = ConnectionPool.getInstance().getConnection();
		ArrayList<SpecificDate> dates = new ArrayList<>();
		try {
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `date`;");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				LocalDate date = rs.getDate("specific_date").toLocalDate();
				Time open = rs.getTime("open_hour");
				Time close = rs.getTime("close_hour");
				boolean status = rs.getString("status").equals("CLOSE");
				System.out.println("DEBUG: Retrieved date entry: " + date + ", open: " + open + ", close: " + close + ", closed: "
						+ status);
				SpecificDate datesObj = new SpecificDate(date, open, close);
				datesObj.setClosed(status);
				dates.add(datesObj);
				
			}
			return dates;
		} catch (SQLException e) {
			System.out.println("ERROR in getAllDatesHours:");
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return null;
	}
	
	/**
	 * the method checks if a specific date is closed in the database
	 * 
	 * @param dateTimeToCheck The specific date
	 * @return true if the date is closed, false otherwise
	 */
	public boolean isDateClosed(LocalDateTime dateTimeToCheck) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = "SELECT * FROM `date` WHERE specific_date = ?;";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setDate(1, Date.valueOf(dateTimeToCheck.toLocalDate()));
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					boolean result = rs.getString("status").equals("CLOSE") || rs.getTime("open_hour").toLocalTime().isAfter(dateTimeToCheck.toLocalTime())
							|| rs.getTime("close_hour").toLocalTime().isBefore(dateTimeToCheck.toLocalTime());
					return result;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}
		return false;
	}
	
	/**
	 * the method checks if a day of the week is closed in the database
	 * 
	 * @param dayOfWeek The day of the week (1=Sunday, 7=Saturday)
	 * @param localTime 
	 * @return true if the day is closed, false otherwise
	 */
	public boolean isDayClosed(int dayOfWeek, LocalTime localTime) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query = "SELECT * FROM `day` WHERE day_of_week = ?;";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, dayOfWeek);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("status").equals("CLOSE") || rs.getTime("open_hour").toLocalTime().isAfter(localTime)
							|| rs.getTime("close_hour").toLocalTime().isBefore(localTime);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}
		return false;
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
	 * the method gets reports data from the database
	 * 
	 * @param r A GetReportsRequest
	 * @return A map containing the reports data
	 */
	public Map<String, Map<Integer, Double>> getReportsData(Request r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		GetReportsRequest req = (GetReportsRequest) r;

		// Determine which month/year to query
		int targetMonth, targetYear;

		if (req.getMonth() == -1) {
			// Default: Previous Month
			LocalDate lastMonth = LocalDate.now().minusMonths(1);
			targetMonth = lastMonth.getMonthValue();
			targetYear = lastMonth.getYear();
		} else {
			// User Selection
			targetMonth = req.getMonth();
			targetYear = req.getYear();
		}

		Map<String, Map<Integer, Double>> allData = new HashMap<>();
		String[] keys = { "Arrivals", "Departures", "AvgCustomerLate", "AvgRestaurantDelay", "InAdvance", "OnTheSpot",
				"AvgWaiting" };
		for (String k : keys) {
			allData.put(k, new TreeMap<>());
			for (int i = 0; i < 24; i++)
				allData.get(k).put(i, 0.0);
		}

		try {
			// --- QUERY 1: ACTIVITY ---
			String queryActivity = "SELECT HOUR(actual_arrival) as h, 'ARR' as type, COUNT(*) as val FROM `order` "
					+ "WHERE actual_arrival IS NOT NULL AND MONTH(actual_arrival) = ? AND YEAR(actual_arrival) = ? GROUP BY h "
					+ "UNION " + "SELECT HOUR(leave_time) as h, 'DEP' as type, COUNT(*) as val FROM `order` "
					+ "WHERE leave_time IS NOT NULL AND MONTH(leave_time) = ? AND YEAR(leave_time) = ? GROUP BY h";

			try (PreparedStatement stmt = conn.prepareStatement(queryActivity)) {
				stmt.setInt(1, targetMonth);
				stmt.setInt(2, targetYear);
				stmt.setInt(3, targetMonth);
				stmt.setInt(4, targetYear);

				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						int h = rs.getInt("h");
						String type = rs.getString("type");
						double val = rs.getDouble("val");
						if (type.equals("ARR"))
							allData.get("Arrivals").put(h, val);
						else
							allData.get("Departures").put(h, val);
					}
				}
			}

			// --- QUERY 2: LATENESS vs DELAY ---
			String queryLateness = "SELECT HOUR(actual_arrival) as h, "
					+ "AVG(GREATEST(0, TIMESTAMPDIFF(MINUTE, order_datetime, actual_arrival))) as customer_late, "
					+ "AVG(GREATEST(0, TIMESTAMPDIFF(MINUTE, actual_arrival, seated_time))) as restaurant_delay "
					+ "FROM `order` " + "WHERE actual_arrival IS NOT NULL AND seated_time IS NOT NULL "
					+ "AND MONTH(actual_arrival) = ? AND YEAR(actual_arrival) = ? " + "GROUP BY h";

			try (PreparedStatement stmt = conn.prepareStatement(queryLateness)) {
				stmt.setInt(1, targetMonth);
				stmt.setInt(2, targetYear);

				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						int h = rs.getInt("h");
						allData.get("AvgCustomerLate").put(h, rs.getDouble("customer_late"));
						allData.get("AvgRestaurantDelay").put(h, rs.getDouble("restaurant_delay"));
					}
				}
			}
			// --- QUERY 3: IN_ADVANCE vs ON_THE_SPOT (Daily) ---

			String queryOrderTypes = "SELECT DAY(order_datetime) as d, 'ADVANCE' as type, COUNT(*) as val "
					+ "FROM `order` " + "WHERE type_of_order = 'IN_ADVANCE' "
					+ "AND MONTH(order_datetime) = ? AND YEAR(order_datetime) = ? " + "GROUP BY d " +

					"UNION " +

					"SELECT DAY(order_datetime) as d, 'SPOT' as type, COUNT(*) as val " + "FROM `order` "
					+ "WHERE type_of_order = 'ON_THE_SPOT' "
					+ "AND MONTH(order_datetime) = ? AND YEAR(order_datetime) = ? " + "GROUP BY d";

			try (PreparedStatement stmt = conn.prepareStatement(queryOrderTypes)) {

				// 1st part (ADVANCE)
				stmt.setInt(1, req.getMonth());
				stmt.setInt(2, req.getYear());

				// 2nd part (SPOT)
				stmt.setInt(3, req.getMonth());
				stmt.setInt(4, req.getYear());

				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						int day = rs.getInt("d");
						String type = rs.getString("type");
						double count = rs.getDouble("val");

						if (type.equals("ADVANCE")) {
							allData.get("InAdvance").put(day, count);
						} else if (type.equals("SPOT")) {
							allData.get("OnTheSpot").put(day, count);
						}
					}
				}
			}
			String queryWaitingTimes = "SELECT HOUR(actual_arrival) as start_h, HOUR(seated_time) as end_h "
					+ "FROM `order` " + "WHERE actual_arrival IS NOT NULL AND seated_time IS NOT NULL "
					+ "AND MONTH(actual_arrival) = ? AND YEAR(actual_arrival) = ? "
					+ "AND TIMESTAMPDIFF(MINUTE, actual_arrival, seated_time) > 0";

			int[] hourlyCounts = new int[24];
			int month = req.getMonth();
			int year = req.getYear();

			try (PreparedStatement stmt = conn.prepareStatement(queryWaitingTimes)) {
				stmt.setInt(1, month);
				stmt.setInt(2, year);

				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						int startH = rs.getInt("start_h");
						int endH = rs.getInt("end_h");
						if (startH == endH) {
							hourlyCounts[startH]++;
						} else {
							for (int h = startH; h <= endH; h++) {
								if (h >= 0 && h < 24) {
									hourlyCounts[h]++;
								}
							}
						}
					}
				}
			}
			int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

			for (int h = 0; h < 24; h++) {
				// Total Waiting Count / Days in Month = Average Daily Queue for that hour
				double avg = (double) hourlyCounts[h] / daysInMonth;
				allData.get("AvgWaiting").put(h, avg);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}

		return allData;
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
	 * the method assigns or removes an order to/from a table in the database
	 * @param orderNum
	 * @param tableNum
	 * @param wantToSit
	 */
	public void putOrderToTable(String orderNum, int tableNum, boolean wantToSit) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String query;
		if (wantToSit) {
			query = "UPDATE `table` SET current_order = ? WHERE table_number = ?;";
		} else {
			query = "UPDATE `table` SET current_order = '0' WHERE table_number = ?;";
		}

		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			if (wantToSit) {
				stmt.setInt(1, Integer.parseInt(orderNum));
				stmt.setInt(2, tableNum);
			} else {
				stmt.setInt(1, tableNum);
			}

			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}
	}
	
	/**
	 * the method cancels orders outside of opening hours in the database
	 * @param conn
	 * @param date
	 * @param openTime
	 * @param closeTime
	 * @throws SQLException
	 */
	private void cancelOrdersOutsideHours(Connection conn, String date, String openTime, String closeTime) throws SQLException{
		List<String> cnl = new ArrayList<>();

		String selectSql = """
		    SELECT contact
		    FROM `order`
		    WHERE DATE(order_datetime) = ?
		      AND (TIME(order_datetime) < ? OR TIME(order_datetime) >= ?)
		      AND status IN ('OPEN', 'WAITING')
		""";

		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
		    stmt.setDate(1, Date.valueOf(date));
		    stmt.setTime(2, Time.valueOf(openTime));
		    stmt.setTime(3, Time.valueOf(closeTime));

		    ResultSet rs = stmt.executeQuery();
		    while (rs.next()) {
		        cnl.add(rs.getString("contact"));
		    }
		}

		EmailService emailService = EmailService.getInstance();
		for(String email : cnl) {
			if(email.contains("@")) {
				String subject = "Order Cancellation Notice";
				String body = "Dear Customer,\n\n" + "We regret to inform you that your order scheduled for " + date
						+ " has been cancelled due to it being outside of our operating hours.\n"
						+ "We apologize for any inconvenience this may cause and appreciate your understanding.\n\n"
						+ "Best regards,\n" + "Bistro Team";
				emailService.sendEmail(email, subject, body);
			}
			else {
				ServerUI.updateInScreen("order cancelled notice sent to phone number " + email);
			}
		}
		
	    String sql = """
	        UPDATE `order`
	        SET status = 'CANCELLED'
	        WHERE DATE(order_datetime) = ?
	        AND (
	            TIME(order_datetime) < ?
	            OR TIME(order_datetime) >= ?
	        )
	        AND status IN ('OPEN', 'WAITING')
	    """;

	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setDate(1, Date.valueOf(date));
	        stmt.setTime(2, Time.valueOf(openTime));
	        stmt.setTime(3, Time.valueOf(closeTime));
	        stmt.executeUpdate();
	    }
	}

	/**
	 * the method gets all confirmation codes of active orders for a specific user
	 * from the database
	 * 
	 * @param r A GetUserActiveOrdersRequest
	 * @return A list of confirmation codes
	 */
	public List<String> getUserConfCodes(GetUserActiveOrdersRequest r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		List<String> result = new ArrayList<>();
		try {
			PreparedStatement stmt = conn.prepareStatement(r.getQuery());
			stmt.setInt(1, r.getSubId());
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				result.add(rs.getString("confirmation_code"));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}
		
		return result;
	}

	/**
	 * the method closes the restaurant for a specific day of the week in the
	 * database
	 * 
	 * @param r A CloseDayRequest
	 * @return The resulting string, a message to the user
	 */
	public String closeRestaurantByDay(Request r) {
	    Connection conn = ConnectionPool.getInstance().getConnection();
	    CloseDayRequest req = (CloseDayRequest) r;
	    String dayOfWeek = req.getDay();
	    String query = req.getQuery();

	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setString(1, dayOfWeek);

	        int rows = stmt.executeUpdate();
	        if (rows > 0) {
	        	cancelAllOrdersForDay(conn, Integer.parseInt(dayOfWeek));
	            return "Restaurant closed for day " + dayOfWeek;
	        }
	        else
	            return "No matching day found.";
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return "Error closing restaurant by day: " + e.getMessage();
	    } finally {
	        ConnectionPool.getInstance().returnConnection(conn);
	    }
	}

	/**
	 * the method closes the restaurant for a specific date in the database
	 * 
	 * @param r A CloseDateRequest
	 * @return The resulting string, a message to the user
	 */
	public String closeRestaurantByDate(Request r) {
	    Connection conn = ConnectionPool.getInstance().getConnection();
	    CloseDateRequest req = (CloseDateRequest) r;
	    String date = req.getDate();
	    String query = req.getQuery();

	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setDate(1, Date.valueOf(date));
	        stmt.setTime(2, Time.valueOf("00:00:00"));
	        stmt.setTime(3, Time.valueOf("00:00:00"));
	        stmt.setString(4, "CLOSE");

	        int rows = stmt.executeUpdate();
	        if (rows > 0) {
	        	cancelAllOrdersForDate(conn, date);
	            return "Restaurant closed for date " + date;
	        }
	        else
	            return "No matching date found.";
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return "Error closing restaurant by date: " + e.getMessage();
	    } finally {
	        ConnectionPool.getInstance().returnConnection(conn);
	    }
	}
	
	/**
	 * the method cancels all orders for a specific date in the database
	 * 
	 * @param conn A database connection
	 * @param date The specific date
	 * @throws SQLException
	 */
	private void cancelAllOrdersForDate(Connection conn, String date) throws SQLException {
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
	private void cancelAllOrdersForDay(Connection conn, int dayOfWeek) throws SQLException {
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
	public boolean bistroSchemaExists(String dbUser, String dbPassword) {
	    // Connect to the MySQL server directly, not the specific 'bistro' DB
	    String serverUrl = "jdbc:mysql://localhost:3306/"; 
	    String query = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";

	    try (Connection conn = DriverManager.getConnection(serverUrl, dbUser, dbPassword);
	         PreparedStatement stmt = conn.prepareStatement(query)) {
	        
	        stmt.setString(1, "bistro");
	        
	        try (ResultSet rs = stmt.executeQuery()) {
	            // If we get a row back, the schema exists
	            return rs.next(); 
	        }

	    } catch (SQLException e) {
	        System.err.println("Error checking for schema: " + e.getMessage());
	        return false;
	    }
	}

	public boolean AllTablesExist() {
		Connection conn = ConnectionPool.getInstance().getConnection();
	    String [] requiredTables = {
	        "`order`",
	        "`table`",
	        "day",
	        "date",
	        "`user`"
	    };
	    // Build a query like: AND TABLE_NAME IN (?, ?, ?)
	    StringBuilder sql = new StringBuilder(
	        "SELECT COUNT(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'bistro' AND TABLE_NAME IN ("
	    );
	    
	    for (int i = 0; i < requiredTables.length; i++) {
	        sql.append(i == 0 ? "?" : ", ?");
	    }
	    sql.append(")");

	    try(PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

	        // Fill the parameters
	        for (int i = 0; i < requiredTables.length; i++) {
	            stmt.setString(i + 1, requiredTables[i]);
	        }

	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                int foundCount = rs.getInt(1);
	                // Return true only if we found exactly as many tables as we asked for
	                return foundCount == requiredTables.length;
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Error checking tables: " + e.getMessage());
	    } finally {
	    	ConnectionPool.getInstance().returnConnection(conn);
	    }
	    return false;
	
	}

}
