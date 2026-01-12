package entities;

import java.io.Serializable;
import java.util.List;

/**
 * Manager class representing a manager user in the system.
 */
public class Manager extends Worker implements Serializable{
	private static final long serialVersionUID = -4707355705231161183L;
	private int subscriberID;
	private String userName;
	private String firstName;
	private String lastName;
	private List<Order> orderHistory;
	
	/**
	 * Constructor for Manager class.
	 * 
	 * @param subscriberID Unique identifier for the manager.
	 * @param userName     Username of the manager.
	 * @param firstName    First name of the manager.
	 * @param lastName     Last name of the manager.
	 * @param phoneNumber  Phone number of the manager.
	 * @param email        Email address of the manager.
	 * @param status       Current status of the manager.
	 * @param orderHistory List of orders associated with the manager.
	 */
	public Manager(int subscriberID, String userName, String firstName, String lastName, String phoneNumber,
			String email,String status, List<Order> orderHistory) {
		super(subscriberID,userName,firstName,lastName,phoneNumber,email,status,orderHistory);
		setType(UserType.MANAGER);
	}
}
