package entities.requests;

import entities.Subscriber;

/**
 * A request that handles a new registration of a user
 */
public class RegisterRequest extends Request {

	private static final long serialVersionUID = -6407442402477950147L;
	private Subscriber user;

	/**
	 * Constructor for RegisterRequest
	 * 
	 * @param user The user to be registered
	 */
	public RegisterRequest(Subscriber user) {
		super(RequestType.REGISTER_REQUEST, "INSERT INTO `user` (full_name, subscriber_id, username, phone_number, email, status)\n"
				+ "VALUES (?, ?, ?, ?, ?, ?);");
		this.user = user;
	}
	public Subscriber getUser() {
		return user;
	}
	

}
