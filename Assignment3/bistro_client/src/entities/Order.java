package entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order class representing a restaurant order.
 */
public class Order implements Serializable{

    private static final long serialVersionUID = -799336047605632156L;
	private String orderNumber;         // from GET_NEXT_ORDER_NUMBER
    private String confirmationCode;    // computed from orderNumber

    private String orderDateTime;       // yyyy-MM-dd HH:mm:ss
    private String numberOfGuests;
    private String subscriberId;        // "0" means guest
    private String dateOfPlacingOrder;  // yyyy-MM-dd
    private String contact;             // phone/email or subscriber email
    
    /** Sitting time for the order */
    private LocalDateTime sittingtime;
    
    /**
	 * Constructor for Order object
	 * @param args ArrayList of Strings containing order details in the following order:
	 *             orderNumber, dateTime, numberOfGuests, confirmationCode,
	 *             subscriberId, contact
	 */
    public Order(ArrayList<String> args) {
    	this.orderNumber = args.get(0);
        this.orderDateTime =args.get(1); 
        this.numberOfGuests = args.get(2);
        this.confirmationCode = args.get(3);
        this.subscriberId = args.get(4);
        this.dateOfPlacingOrder = LocalDate.now().toString();
        this.contact = args.get(5);
    }
    
    /**
     * Constructor for Order object
     * @param args
     * @param flag
     */
    public Order(List<String> args, int flag) {
		this.orderNumber = args.get(0);
		this.orderDateTime =args.get(1); 
		this.numberOfGuests = args.get(2);
		this.confirmationCode = args.get(3);
		this.subscriberId = args.get(4);
		this.dateOfPlacingOrder = args.get(5);
		this.contact = args.get(6);
	}

    public String getOrderNumber() { return orderNumber; }
    public String getConfirmationCode() { return confirmationCode; }

    public String getOrderDateTime() { return orderDateTime; }
    public String getNumberOfGuests() { return numberOfGuests; }
    public String getSubscriberId() { return subscriberId; }
    public String getDateOfPlacingOrder() { return dateOfPlacingOrder; }
    public String getContact() { return contact; }
    
    public LocalDateTime getSittingtime() {
    	return sittingtime;
    }
    public void setSittingtime(LocalDateTime sittingtime) {
    	if (this.sittingtime == null) {
    		this.sittingtime = sittingtime;
    	}
	}
    @Override
    public boolean equals(Object ord) {
		return this.orderNumber.equals(((Order)ord).getOrderNumber());
	}
    
    @Override
    public String toString() {
    			return
						"Order Number= " + orderNumber + '\n' +
						"Confirmation Code= " + confirmationCode + '\n' +
						"Number Of Guests= " + numberOfGuests + '\n' +
						"Subscriber Id= " + subscriberId + '\n' +
						"Contact= " + contact + '\n';
    }
}
