package entities.requests;

/**
 * CancelRequest class represents a request to cancel an order in the system. It
 * extends the Request class and includes the confirmation code of the order to
 * be cancelled.
 */
public class CancelRequest extends Request {
	private static final long serialVersionUID = -474821641310298369L;
	private String code;
	
	/**
	 * Constructs a CancelRequest with the specified confirmation code.
	 * 
	 * @param code the confirmation code of the order to be cancelled
	 */
	public CancelRequest(String code) {
		super(RequestType.CANCEL_REQUEST, "SELECT * FROM `order` WHERE confirmation_code = ?");
		this.code = code;
	}

	/**
	 * Returns the confirmation code of the order to be cancelled.
	 * 
	 * @return the confirmation code
	 */
	public String getCode() {
		return code;
	}
}
