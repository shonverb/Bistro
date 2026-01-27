package entities.requests;

public class LogOutUserRequest extends Request {
	private static final long serialVersionUID = -3238122087124868232L;
	private int subscriberId;

	public LogOutUserRequest(int subscriberId) {
		super(RequestType.LOGOUT_REQUEST, "");
		this.subscriberId = subscriberId;
		
	}
	public int getSubscriberId() {
		return subscriberId;
	}

}
