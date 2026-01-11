package entities.requests;

/**
 * Represents a request to read the email of a user based on their subscriber ID.
 */
public class ReadEmailRequest extends Request {

    private static final long serialVersionUID = -3424593526604118732L;
	private int subscriberId;

	/**
	 * Constructs a ReadEmailRequest with the specified subscriber ID.
	 *
	 * @param subscriberId the subscriber ID of the user whose email is to be read
	 */
    public ReadEmailRequest(int subscriberId) {
        super(RequestType.READ_EMAIL, "SELECT email FROM `user` WHERE subscriber_id = ?");
        this.subscriberId = subscriberId;
    }

    public int getSubscriberId() {
        return subscriberId;
    }
}
