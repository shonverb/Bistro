package entities.requests;

/**A request for handling a the login of a user*/
public class LoginRequest extends Request {
	private static final long serialVersionUID = -4077547286007567120L;
	private int subscriberId;
	
	/**
	 * Creates a new LoginRequest
	 * 
	 * @param subscriberId The subscriber id of the user trying to login
	 */
	public LoginRequest(int subscriberId) {
		super(RequestType.LOGIN_REQUEST, "SELECT * FROM `user` WHERE subscriber_id = ?;");
		this.subscriberId = subscriberId;
	}
	public int getId() {
		return subscriberId;
	}
}
