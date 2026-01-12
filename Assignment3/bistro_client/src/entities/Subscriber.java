package entities;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Subscriber class represents a subscriber user in the system.
 */
public class Subscriber extends User implements Serializable{
	private static final long serialVersionUID = -1659025603535708879L;
	private int subscriberID;
	private String userName;
	private String firstName;
	private String lastName;
	private List<Order> orderHistory;

	/**
	 * Constructs a Subscriber object with the specified details.
	 *
	 * @param subscriberID the unique ID of the subscriber
	 * @param userName     the username of the subscriber
	 * @param firstName    the first name of the subscriber
	 * @param lastName     the last name of the subscriber
	 * @param phoneNumber  the phone number of the subscriber
	 * @param email        the email address of the subscriber
	 * @param status       the status of the subscriber
	 * @param orderHistory the order history of the subscriber
	 */
	public Subscriber(int subscriberID, String userName, String firstName, String lastName, String phoneNumber,
			String email,String status, List<Order> orderHistory) {
		super(UserType.SUBSCRIBER,email,phoneNumber,status);
		this.subscriberID = subscriberID;
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.orderHistory = orderHistory;
	}

	public int getSubscriberID() {
		return subscriberID;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public List<Order> getOrderHistory() {
		return orderHistory;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
		
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Subscriber))
			return false;
		Subscriber other = (Subscriber) obj;
				return Objects.equals(subscriberID, other.subscriberID)
				&& Objects.equals(userName, other.userName);
	}
	
	@Override
	public String toString() {
		return "ID: " + subscriberID + "\nUser Name: " + userName + "\nName: " + firstName
				+ " " + lastName + "\nPhone Number: " + getPhone() + "\nEmail: " + getEmail();
	}

}

