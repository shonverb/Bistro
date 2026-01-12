package entities.requests;

/**
 * This class represents a request to get hours date information from the
 * database. It extends the Request class and specifies the request type and SQL
 * query.
 */
public class GetHoursDateRequest extends Request {


	private static final long serialVersionUID = -1352813963901315413L;

	/**
	 * Constructs a GetHoursDateRequest with the appropriate request type and SQL
	 * query.
	 */
	public GetHoursDateRequest() {
		super(RequestType.GET_HOURS_DATE, "SELECT * FROM `date`;");
	}
}
