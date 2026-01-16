package entities.requests;

/**
 * Request to close a specific day of the week.
 */
public class CloseDayRequest extends Request {
	private static final long serialVersionUID = 1L;
    private String day;

    public CloseDayRequest(String day) {
        super(RequestType.CLOSE_DAY,
              "UPDATE `day` SET status = 'CLOSE' WHERE day_of_week = ?");
        this.day = day;
    }
    
        public String getDay() {
        	return day;
        }
}
