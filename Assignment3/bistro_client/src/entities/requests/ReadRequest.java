package entities.requests;

/**
 * A request that handles searching for an order with a given id
 */
public class ReadRequest extends Request {

	private static final long serialVersionUID = -6823587599986808062L;
	/** an id for recognizing the order*/
	private String orderNum;
	public ReadRequest(String orderNum) { //only primary key (order number) needed for reading details about existing order 
		super(RequestType.READ_ORDER, "SELECT * FROM `order` WHERE order_number = ?");
		this.orderNum = orderNum;
	}
	/**
	 * 
	 * @return the order number for that request
	 */
	public String getOrderNum() {
		return orderNum;
	}
	

}
