package entities.requests;

/**
 * This class represents a request to write operating hours for a specific date
 * into the database.
 */
public class WriteHoursDateRequest extends Request{
	private static final long serialVersionUID = -4418439440692756028L;
	private String date;
	private String open;
	private String close;
	
	/**
	 * Constructs a WriteHoursDateRequest with the specified date, opening hour, and
	 * closing hour.
	 * 
	 * @param date  The specific date for which the hours are being set (format:
	 *              YYYY-MM-DD).
	 * @param open  The opening hour for the specified date (format: HH:MM).
	 * @param close The closing hour for the specified date (format: HH:MM).
	 */
	public WriteHoursDateRequest(String date, String open, String close) {
		super(RequestType.WRITE_HOURS_DATE,
				"INSERT INTO `date`" + 
				"(specific_date, open_hour, close_hour)" +
				"VALUES (?, ?, ?)"
		);
		this.date = date;
		this.open = open;
		this.close = close;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getOpen() {
		return open;
	}
	
	public String getClose() {
		return close;
	}
}
