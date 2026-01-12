package entities.requests;

/**
 * Represents a reserve table request in the restaurant reservation system.
 */
public class ReserveRequest extends WriteRequest {


	private static final long serialVersionUID = 1618413611196120100L;

	/**
	 * Constructs a new ReserveRequest with the specified details.
	 *
	 * @param orderDateTime  the date and time of the reservation
	 * @param numberOfGuests the number of guests for the reservation
	 * @param subscriberId   the ID of the subscriber making the reservation
	 * @param contact        the contact information for the reservation
	 */
	public ReserveRequest(String orderDateTime, String numberOfGuests, String subscriberId, String contact) {
		super(orderDateTime, numberOfGuests, subscriberId, contact);
		this.setType(RequestType.RESERVE_TABLE);
	}
}
