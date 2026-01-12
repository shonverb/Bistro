package entities.requests;

/**
 * OrderHistoryRequest class represents a request to retrieve order history.
 */
public class OrderHistoryRequest extends Request {
	
	private static final long serialVersionUID = -3105051314545762356L;

	/**
	 * Constructs an OrderHistoryRequest for the specified user ID.
	 * 
	 * @param userId The ID of the user whose order history is being requested.
	 */
	public OrderHistoryRequest(String userId) {
		super(RequestType.ORDER_HISTORY, "SELECT * "
				+ "FROM `order` "
				+ "WHERE subscriber_id = '" + userId + "' "
				+ "ORDER BY date_of_placing_order DESC;"
				);
	}

}
