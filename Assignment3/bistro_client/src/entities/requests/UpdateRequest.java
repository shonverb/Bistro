package entities.requests;
/** a derived class of request, which allows for updating orders*/
public class UpdateRequest extends Request {
	
	private static final long serialVersionUID = -479178551918491397L;
	private String orderNum;
	private int guestsNum;
	private String date;

	/**
	 * @param orderNum the primary key of the order in the table
	 * @param guestsNum the number of guests to update to for the order
	 * */
	public UpdateRequest(String orderNum, int guestsNum) {
		super(RequestType.UPDATE_GUESTS,"UPDATE `order` SET number_of_guests = ? WHERE order_number = ?");
		this.orderNum = orderNum;
		this.guestsNum = guestsNum;
	}
	/**
	 * @param orderNum the primary key of the order in the table
	 * @param date the date to update to for the order
	 * */
	public UpdateRequest(String orderNum, String date) {
		super(RequestType.UPDATE_DATE,"UPDATE `order` SET order_date = ? WHERE order_number = ?");
		this.orderNum = orderNum;
		this.date = date;
	}
	/**
	 * @return the order number
	 * */
	public String getOrderNum() {
		return orderNum;
	}
	/**
	 * @return the number of guests to update to
	 * */
	public int getNumberOfGuests() {
		return guestsNum;
	}
	/**
	 * @return the date to update to
	 */
	public String getDate() {
		return date;
	}
}
