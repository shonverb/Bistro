package entities.requests;

/**
 * Request to get the next available order number.
 */
public class OrderNumberRequest extends Request {
	private static final long serialVersionUID = -6668175151255611481L;
	/** SQL query to retrieve the next order number. */
    private static final String Q =
            "SELECT IFNULL(MAX(order_number), 0) + 1 AS next_num FROM `order`";

	/**
	 * Constructs an OrderNumberRequest with the predefined SQL query.
	 */
    public OrderNumberRequest() {
        super(RequestType.ORDER_NUMBER, Q);
    }
}