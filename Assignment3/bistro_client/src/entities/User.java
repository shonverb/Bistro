package entities;

import java.io.Serializable;

/**
 * an abstract class describing a general user of the system
 * each user has at least an email and phone, and we use a type to distinguish
 * between different users  
 * */
public abstract class User implements Serializable {
	private static final long serialVersionUID = -71142264396109022L;
	private UserType type;
	private String email;
	private String phone;
	private String status;
	/**
	 * @param type the type of the user (hard-coded in derived classes)
	 * @param email the user's email
	 * @param phone the user's phone number
	 * */
	public User(UserType type, String email, String phone, String status) {
		this.type = type;
		this.email = email;
		this.phone = phone;
		this.status = status;
	}
	/**
	 * @return the user's type
	 * */
	public UserType getType() {
		return type;
	}
	/**
	 * @param type the type to change the user to 
	 * */
	public void setType(UserType type) {
		this.type = type;
	}
	/**
	 * @return the user's email
	 * */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to switch to for the user
	 * */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the user's phone number as a string
	 * */
	public String getPhone() {
		return phone;
	}
	/**
	 * @param the phone number to switch to
	 * */
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	/**
	 * @return the user's status as a string
	 * */
	public String getStatus() {
		return status;
	}
	/**
	 * @param the phone number to switch to
	 * */
	public void setStatus(String status) {
		this.status = status;
	}

}
