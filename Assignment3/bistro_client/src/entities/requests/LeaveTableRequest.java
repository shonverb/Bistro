package entities.requests;

/**
 * LeaveTableRequest class represents a request to leave a table in a
 * restaurant. It extends the Request class and includes the confirmation code
 * of the order.
 */
public class LeaveTableRequest extends Request {

	private static final long serialVersionUID = -4430403792652017325L;
	private String confCode;

	/**
	 * Constructs a LeaveTableRequest with the specified confirmation code.
	 * 
	 * @param confCode the confirmation code of the order
	 */
	public LeaveTableRequest(String confCode) {
		super(RequestType.LEAVE_TABLE, "SELECT subscriber_id, status, order_number, leave_time FROM `order` WHERE confirmation_code = ?");
		this.confCode = confCode;
	}
	
	public String getConfCode() {
		return confCode;
	}

}
