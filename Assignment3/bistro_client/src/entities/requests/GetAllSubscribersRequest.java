package entities.requests;

/**
 * This class represents a request to get all subscribers (clients) from the
 * database.
 */
public class GetAllSubscribersRequest extends Request {


	private static final long serialVersionUID = 2706508013278830159L;

	/**
	 * Constructs a new GetAllSubscribersRequest with the appropriate SQL query.
	 */
	public GetAllSubscribersRequest() {
		super(RequestType.GET_ALL_SUBSCRIBERS, "SELECT * FROM `user` WHERE status = 'CLIENT';");
	}

}
