package entities.requests;

/**
 * A request to check the confirmation code associated with a contact within a
 * specific time frame. This class extends the Request class and is used to
 * retrieve the confirmation code for an order placed by a contact within 30
 * minutes before or after the current time.
 * 
 * @see Request
 */
public class CheckConfCodeRequest extends Request {
	private static final long serialVersionUID = 8362836490349079455L;
	private String contact;

	/**
	 * Constructs a CheckConfCodeRequest with the specified contact.
	 * 
	 * @param contact the contact associated with the order
	 */
	public CheckConfCodeRequest(String contact) {  
		super(RequestType.CHECK_CONFCODE, "SELECT confirmation_code FROM `order` WHERE contact = ? AND order_datetime BETWEEN "
				+ "DATE_SUB(?, INTERVAL 30 MINUTE) AND DATE_ADD(?, INTERVAL 30 MINUTE)");
				
		this.contact=contact;
	}
	/**
	 * 
	 * @return the order number for that request
	 */
	public String getcontact() {
		return contact;
	}
	


}
