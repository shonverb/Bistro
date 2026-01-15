package entities;

import java.io.Serializable;

/** A type of user describing a guest (not registered in the system)*/
public class Guest extends User implements Serializable {


	private static final long serialVersionUID = -7924939766545340775L;

	/**
	 * Constructs a Guest user with the specified email and phone.
	 * 
	 * @param email The email of the guest.
	 * @param phone The phone number of the guest.
	 */
	public Guest(String email, String phone,String status) {
		super(UserType.GUEST, email, phone,status);
	}

}
