package entities.requests;

/**
 * This class represents a request to get the live state of the Bistro system.
 * It extends the Request class and specifies the request type as
 * GET_LIVE_BISTRO_STATE.
 */
public class GetLiveStateRequest extends Request {

	private static final long serialVersionUID = 2414606050636627627L;

	/**
	 * Constructor for GetLiveStateRequest. Initializes the request with the
	 * appropriate type.
	 */
    public GetLiveStateRequest() {
        // We pass empty string because we don't need a SQL query, 
        // we are accessing Server Memory objects.
        super(RequestType.GET_LIVE_BISTRO_STATE, "");
    }
}
