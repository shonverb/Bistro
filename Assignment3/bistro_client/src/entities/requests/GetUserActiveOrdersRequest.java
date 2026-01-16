package entities.requests;

public class GetUserActiveOrdersRequest extends Request {
	private static final long serialVersionUID = 5296365641316545075L;
	private int subId;
	public GetUserActiveOrdersRequest(int subId) {
		super(RequestType.GET_USER_ACTIVE_ORDERS, "SELECT confirmation_code, status FROM `order` WHERE subscriber_id = ? AND (status = 'OPEN' OR status = 'WAITING');");
		this.subId = subId;
	}
	public int getSubId() {
		return subId;
	}

}
