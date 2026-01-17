package bistro_server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import entities.requests.GetReportsRequest;
import entities.requests.Request;

public class ReportManager {

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

}
