package entities.requests;

/**
 * Request to close a specific date.
 */
public class CloseDateRequest extends Request {
	private static final long serialVersionUID = 1L;
    private String date;

    public CloseDateRequest(String date) {
        super(RequestType.CLOSE_DATE,
				"INSERT INTO `date` (specific_date, open_hour, close_hour, status) " +
					      "VALUES (?, ?, ?, ?) " +
					      "ON DUPLICATE KEY UPDATE status = VALUES(status);");
        this.date = date;
    }
    
	public String getDate() {
		return date;
	}

}
