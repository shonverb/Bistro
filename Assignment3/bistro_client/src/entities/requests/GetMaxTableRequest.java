package entities.requests;

/**
 * Request to get the maximum table number.
 */
public class GetMaxTableRequest extends Request {


	private static final long serialVersionUID = 7248863229358859283L;

	/**
	 * Constructs a GetMaxTableRequest.
	 */
	public GetMaxTableRequest() {
		super(RequestType.GET_MAX_TABLE, "");
	}

}
