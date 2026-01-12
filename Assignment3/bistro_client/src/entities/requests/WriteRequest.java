package entities.requests;
/**
 * a request that adds a new order
 */
public class WriteRequest extends Request {

    private static final long serialVersionUID = -6769511017937007225L;
	private String orderDateTime;       // yyyy-MM-dd HH:mm:ss
    private String numberOfGuests;
    private String subscriberId;        // "0" means guest
    private String contact;             // phone/email or subscriber email
    
    /**
     * Constructor
     * @param orderDateTime
     * @param numberOfGuests
     * @param subscriberId
     * @param contact
     */
	public WriteRequest(String orderDateTime, String numberOfGuests, String subscriberId, String contact) {
	    super(
	        RequestType.WRITE_ORDER,
	        "INSERT INTO `order` " +
	        "(order_number, order_datetime, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order, contact, status) " +
	        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
	    );
	    this.orderDateTime = orderDateTime;
	    this.numberOfGuests = numberOfGuests;
	    this.subscriberId = subscriberId;
	    this.contact = contact;
	}
	
	public String getOrderDateTime() {
		return orderDateTime;
	}

	public String getNumberOfGuests() {
		return numberOfGuests;
	}

	public String getSubscriberId() {
		return subscriberId;
	}


	public String getContact() {
		return contact;
	}
	

}
