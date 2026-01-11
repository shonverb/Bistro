package entities.requests;

/**
 * A concrete implementation of Request for joining the waitlist.
 */
public class JoinWaitlistRequest extends Request {
    private static final long serialVersionUID = -8741550332679773490L;
	private String orderDateTime;
    private String numberOfGuests;
    private String subscriberId;
    private String contact;
    private boolean isWaitlistEntry = false;

	/**
	 * Constructs a JoinWaitlistRequest with the specified parameters.
	 *
	 * @param orderDateTime  The date and time for the order.
	 * @param numberOfGuests The number of guests.
	 * @param subscriberId   The subscriber ID.
	 * @param contact        The contact information.
	 */
    public JoinWaitlistRequest(String orderDateTime, String numberOfGuests, String subscriberId, String contact) {
        // You can leave the query string empty if the server handles logic internally
        super(RequestType.JOIN_WAITLIST, ""); 
        this.orderDateTime = orderDateTime;
        this.numberOfGuests = numberOfGuests;
        this.subscriberId = subscriberId;
        this.contact = contact;
    }
    

    /**
     * Constructs a JoinWaitlistRequest with the specified parameters including waitlist entry status.
     * @param orderDateTime
     * @param numberOfGuests
     * @param subscriberId
     * @param contact
     * @param isWaitlistEntry
     */
 // New constructor for the second attempt (waitlist confirmation)
    public JoinWaitlistRequest(String orderDateTime, String numberOfGuests, String subscriberId, String contact, boolean isWaitlistEntry) {
        this(orderDateTime, numberOfGuests, subscriberId, contact); // Calls existing constructor
        setWaitlistEntry(isWaitlistEntry);
    }

	/**
	 * Getter and Setter for isWaitlistEntry
	 */
    public boolean isWaitlistEntry() { return isWaitlistEntry; }
    public void setWaitlistEntry(boolean isWaitlistEntry) { this.isWaitlistEntry = isWaitlistEntry; }
    public String getOrderDateTime() { return orderDateTime; }
    public String getNumberOfGuests() { return numberOfGuests; }
    public String getSubscriberId() { return subscriberId; }
    public String getContact() { return contact; }

}