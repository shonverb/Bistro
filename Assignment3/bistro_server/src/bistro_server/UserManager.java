package bistro_server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import entities.Subscriber;
import entities.User;
import entities.requests.GetUserActiveOrdersRequest;
import entities.requests.LogOutUserRequest;
import entities.requests.LoginRequest;
import entities.requests.RegisterRequest;
import entities.requests.Request;

public class UserManager {

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
	
	public Boolean logoutUser(Request r) {
		int subscriberId = ((LogOutUserRequest) r).getSubscriberId();
		BistroServer.loggedInUsers.remove(subscriberId);
		return true;
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
		synchronized (BistroServer.loggedInUsers) {
		    if (BistroServer.loggedInUsers.contains(subcriberId)) {
		        return "ALREADY CONNECTED";
		    }
		    BistroServer.loggedInUsers.add(subcriberId);
		}

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
	 * the method gets all confirmation codes of active orders for a specific user
	 * from the database
	 * 
	 * @param r A GetUserActiveOrdersRequest
	 * @return A list of confirmation codes
	 */
	public List<String> getUserConfCodes(GetUserActiveOrdersRequest r) {
		Connection conn = ConnectionPool.getInstance().getConnection();
		String forWaiting = " In waiting list.";
		String forReady = " Ready for sitting.";
		List<String> result = new ArrayList<>();
		try {
			PreparedStatement stmt = conn.prepareStatement(r.getQuery());
			stmt.setInt(1, r.getSubId());
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				result.add(rs.getString("confirmation_code")+ (rs.getString("status").equals("WAITING") ? forWaiting : forReady) );
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionPool.getInstance().returnConnection(conn);
		}
		System.out.println(result);
		
		return result;
	}

}
