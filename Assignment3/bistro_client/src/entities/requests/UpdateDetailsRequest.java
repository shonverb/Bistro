package entities.requests;

/**
 * Represents a request to update details in the system.
 */
public class UpdateDetailsRequest extends Request {


	private static final long serialVersionUID = 6412164591689763483L;

	/**
	 * Constructs an UpdateDetailsRequest with the specified query.
	 *
	 * @param query the query string containing the details to be updated
	 */
	public UpdateDetailsRequest(String query) {
		super(RequestType.UPDATE_DETAILS, query);
	}

}
