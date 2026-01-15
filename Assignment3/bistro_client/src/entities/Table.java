package entities;

import java.io.Serializable;
import java.time.LocalDate;
/**
 * A class representing a table in the bistro
 */
public class Table implements Comparable<Table>, Serializable {

	private static final long serialVersionUID = 7325742576731798068L;
	/** the table's id*/
	private int id;
	/** the table's capacity*/
	private int capacity;
	/** whether the table is taken or not*/
	private boolean isTaken;
	
	/**whether the table is pending removal*/
	private boolean isPendingRemoval;

	
	/**
	 * Constructor for Table class
	 * 
	 * @param id       the table's id
	 * @param capacity the table's capacity
	 * @param isTaken  whether the table is taken or not
	 */
	public Table(int id, int capacity, boolean isTaken) {
		super();
		this.id = id;
		this.capacity = capacity;
		this.isTaken = isTaken;
		this.isPendingRemoval = false;
	}
	
	public Table(int id, int capacity, boolean isTaken, boolean isPendingRemoval) {
		super();
		this.id = id;
		this.capacity = capacity;
		this.isTaken = isTaken;
	}
	
	
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public boolean isTaken() {
		return isTaken;
	}
	public void setTaken(boolean isTaken) {
		this.isTaken = isTaken;
	}
	public int getId() {
		return id;
	}
	
	public boolean pendingRemoval() {
		return isPendingRemoval;
	}
	
	public void setPendingRemoval(boolean pendingRemoval) {
		this.isPendingRemoval = pendingRemoval;
	}
	@Override
	public int compareTo(Table o) {
		if (this.capacity>o.capacity)
			return 1;
		else if (this.capacity<o.capacity)
			return -1;
		else
		return 0;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Table other = (Table) obj;
		if (id != other.getId())
			return false;
		return true;
	}
	
	public String toString() {
		return "Table Number: " + id + ", Capacity: " + capacity+", "+ (isTaken ? "Taken" : "Available");
	}
	
	public String prettyToString() {
		return "Your Table Is Numbered "+ id +" With " +capacity +" Spots";
	}
	
}
