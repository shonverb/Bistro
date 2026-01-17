package bistro_server;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import entities.Day;
import entities.SpecificDate;
import entities.requests.ChangeHoursDayRequest;
import entities.requests.CloseDateRequest;
import entities.requests.CloseDayRequest;
import entities.requests.Request;
import entities.requests.WriteHoursDateRequest;

public class ScheduleManager {
	
	private OrderManager orderManager;
	
	public ScheduleManager(OrderManager orderManager) {
		this.orderManager = orderManager;
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
				orderManager.cancelOrdersOutsideHours(conn, req.getDay(), openTime, closeTime);
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
				orderManager.cancelOrdersOutsideHours(conn, req.getDate(), openTime, closeTime);
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

		return new ArrayList<Day>();
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

		return  new ArrayList<SpecificDate>();
	}
	
	/**
	 * the method checks if a specific date is closed in the database
	 * 
	 * @param dateTimeToCheck The specific date
	 * @return true if the date is closed, false otherwise
	 */
	public Boolean isDateClosed(LocalDateTime dateTimeToCheck) {
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
		return null;
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
	        	orderManager.cancelAllOrdersForDay(conn, Integer.parseInt(dayOfWeek));
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
	        	orderManager.cancelAllOrdersForDate(conn, date);
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

	
}
