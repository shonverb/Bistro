package entities;

import java.io.Serializable;
import java.util.List;

/**
 * Worker class representing a bistro representative in the system. Inherits
 * from Subscriber and implements Serializable for object serialization.
 */
public class Worker extends Subscriber implements Serializable {
	private static final long serialVersionUID = 558341385970885494L;
	private int subscriberID;
	private String userName;
	private String firstName;
	private String lastName;
	private List<Order> orderHistory;
	
	/**
	 * Constructor to initialize a Worker object with the provided details.
	 * 
	 * @param subscriberID Unique identifier for the subscriber.
	 * @param userName     Username of the worker.
	 * @param firstName    First name of the worker.
	 * @param lastName     Last name of the worker.
	 * @param phoneNumber  Contact phone number of the worker.
	 * @param email        Email address of the worker.
	 * @param status       Current status of the worker.
	 * @param orderHistory List of orders associated with the worker.
	 */
	public Worker(int subscriberID, String userName, String firstName, String lastName, String phoneNumber,
			String email,String status, List<Order> orderHistory) {
		super(subscriberID,userName,firstName,lastName,phoneNumber,email,status,orderHistory);
		setType(UserType.BISTRO_REP);
	}
}
