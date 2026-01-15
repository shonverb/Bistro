package entities.requests;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * a class that represents a request to show taken slots for a given number of guests and date/time
 * */
public class ShowTakenSlotsRequest extends Request {
	private static final long serialVersionUID = 7195377181018591704L;
	/** date time formatter */
	private static DateTimeFormatter f=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private int numberOfGuests;
    private String orderDateTime;
    private LocalDateTime from;
    private LocalDateTime to;
    
	/**
	 * constructor
	 * 
	 * @param numberOfGuests the number of guests
	 * @param orderDateTime  the date and time of the order
	 */
    public ShowTakenSlotsRequest(int numberOfGuests, String orderDateTime) {
        super(RequestType.GET_TAKEN_SLOTS, "SELECT confirmation_code, number_of_guests FROM `order` WHERE status = 'OPEN' AND order_datetime BETWEEN ? AND ?;");
        this.numberOfGuests = numberOfGuests;
        this.orderDateTime = orderDateTime;
    	LocalDateTime parsed = LocalDateTime.parse(orderDateTime, f);
        this.from = LocalDateTime.parse(parsed.toString()).minusMinutes(30); 
        this.to = LocalDateTime.parse(parsed.toString()).plusHours(1).plusMinutes(30);
    }
    
    /**
     * constructor
     * @param numberOfGuests
     * @param orderDateTime
     * @param from
     * @param to
     */
    public ShowTakenSlotsRequest(int numberOfGuests, String orderDateTime,LocalDateTime from,LocalDateTime to) {
        super(RequestType.GET_TAKEN_SLOTS, "SELECT confirmation_code, number_of_guests FROM `order` WHERE status = 'OPEN' AND order_datetime BETWEEN ? AND ?;");
        this.numberOfGuests = numberOfGuests;
        this.orderDateTime = orderDateTime;
    	this.from=from;
    	this.to=to;
    }

    public int getNumberOfGuests() {
		return numberOfGuests;
	}
    
    public String getOrderDateTime() {
		return orderDateTime;
	}
    
    public LocalDateTime getFrom() {
 		return from;
    }
    
    public LocalDateTime getTo() {
   		return to;
    }
}


