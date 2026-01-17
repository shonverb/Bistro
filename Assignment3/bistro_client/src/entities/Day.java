package entities;

import java.io.Serializable;
import java.sql.Time;

/**
 * Represents a day of the week with opening and closing times.
 */
public class Day implements Serializable{
	private static final long serialVersionUID = 3584305537514718664L;
	private int day;
	private Time open;
	private Time close;
	private boolean isClosed;
	
	/**
	 * Constructs a Day object with specified day, opening time, and closing time.
	 * 
	 * @param day   the day of the week (1 for Monday, 7 for Sunday)
	 * @param open  the opening time
	 * @param close the closing time
	 */
	public Day(int day, Time open, Time close) {
		this.day = day;
		this.open = open;
		this.close = close;
	}
	
	public int getDay() {
		return this.day;
	}
	
	public Time getOpen() {
		return this.open;
	}
	
	public Time getClose() {
		return this.close;
	}

	public boolean isClosed() {
		return isClosed;
	}
	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}
}
