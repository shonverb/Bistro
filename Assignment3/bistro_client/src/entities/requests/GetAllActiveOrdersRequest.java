package entities.requests;

/**
 * Represents a request to retrieve all active orders from the database.
 */
public class GetAllActiveOrdersRequest extends Request {


	private static final long serialVersionUID = -1370405609311910593L;

	/**
	 * Constructs a new GetAllActiveOrdersRequest.
	 */
	public GetAllActiveOrdersRequest() {
		super(RequestType.GET_ALL_ACTIVE_ORDERS, "SELECT * FROM `order` WHERE status = 'OPEN'");
	}

}
