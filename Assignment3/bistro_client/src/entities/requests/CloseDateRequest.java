package entities.requests;

/**
 * Request to close a specific date.
 */
public class CloseDateRequest extends Request {
	private static final long serialVersionUID = 1L;
    private String date;

    public CloseDateRequest(String date) {
        super(RequestType.CLOSE_DATE,
              "UPDATE `date` SET status = 'CLOSE' WHERE specific_date = ?");
        this.date = date;
    }
    
	public String getDate() {
		return date;
	}

}
