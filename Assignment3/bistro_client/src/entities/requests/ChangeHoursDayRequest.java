package entities.requests;

/**
 * ChangeHoursDayRequest is a subclass of Request that represents a request to
 * change the opening and closing hours for a specific day of the week.
 */
public class ChangeHoursDayRequest extends Request{
	private static final long serialVersionUID = -3135678074310303798L;
	private String day;
	private String open;
	private String close;

	/**
	 * Constructs a ChangeHoursDayRequest with the specified day, opening hour, and
	 * closing hour.
	 * 
	 * @param day   The day of the week to change hours for.
	 * @param open  The new opening hour.
	 * @param close The new closing hour.
	 */
	public ChangeHoursDayRequest(String day, String open, String close) {
		super(RequestType.CHANGE_HOURS_DAY, "UPDATE `day` SET open_hour = ? , close_hour = ? WHERE day_of_week = ?");
		this.day = day;
		this.open = open;
		this.close = close;
	}
	
	public String getDay() {
		return day;
	}
	
	public String getOpen() {
		return open;
	}
	
	public String getClose() {
		return close;
	}
}
