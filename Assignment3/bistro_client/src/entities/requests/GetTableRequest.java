package entities.requests;

/**
 * GetTableRequest is a request to get the table associated with a given
 * confirmation code.
 */
public class GetTableRequest extends Request {
	private static final long serialVersionUID = 492767235657524221L;
	private String confcode;

	/**
	 * Constructs a GetTableRequest with the given confirmation code.
	 * 
	 * @param confcode the confirmation code
	 */
	public GetTableRequest(String confcode) {
		super(RequestType.GET_TABLE,"SELECT * FROM `order` WHERE confirmation_code = ? AND status = 'OPEN';");
		this.confcode=confcode;
	}
	public String getConfcode() {
		return confcode;
	}
}
