package entities;

import java.io.Serializable;
import java.sql.Time;
import java.time.LocalDate;

/**
 * Represents a specific date with opening and closing times.
 */
public class SpecificDate implements Serializable{
	private static final long serialVersionUID = -708879168269297256L;
	private LocalDate date;
	private Time open;
	private Time close;

	/**
	 * Constructs a SpecificDate with the given date, opening time, and closing
	 * time.
	 * 
	 * @param date  the specific date
	 * @param open  the opening time
	 * @param close the closing time
	 */
	public SpecificDate(LocalDate date, Time open, Time close) {
		this.date = date;
		this.open = open;
		this.close = close;
	}
	
	public LocalDate getDate() {
		return this.date;
	}
	
	public Time getOpen() {
		return this.open;
	}
	
	public Time getClose() {
		return this.close;
	}
}
