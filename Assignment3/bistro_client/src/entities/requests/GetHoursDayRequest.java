package entities.requests;

/**
 * This class represents a request to get the hours of the day from the
 * database.
 */
public class GetHoursDayRequest extends Request{

	private static final long serialVersionUID = 6731244524687517956L;

	/**
	 * Constructs a new GetHoursDayRequest.
	 */
	public GetHoursDayRequest() {
		super(RequestType.GET_HOURS_DAY, "SELECT * FROM `day`;");
	}
}
