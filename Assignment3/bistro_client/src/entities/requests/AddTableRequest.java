package entities.requests;

/**
 * Represents a request to add a new table to the restaurant.
 */
public class AddTableRequest extends Request {
	private static final long serialVersionUID = -5143115274171229327L;
	int tableCapacity;

	/**
	 * Constructs an AddTableRequest with the specified table capacity.
	 * 
	 * @param tableCapacity The number of seats at the new table.
	 */
	public AddTableRequest(int tableCapacity) {
		super(RequestType.ADD_TABLE, "INSERT INTO `table` (table_number, number_of_seats, active_from, active_to)\n"
				+ "VALUES (?, ?, CURRENT_DATE + INTERVAL 1 MONTH, NULL);");
		this.tableCapacity = tableCapacity;
	}
	
	/**
	 * Gets the capacity of the table to be added.
	 * 
	 * @return The number of seats at the new table.
	 */
	public int getCap() {
		return tableCapacity;
	}

}
