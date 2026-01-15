package entities.requests;

/**
 * Request to update the capacity of a table by removing the old table and
 * adding a new one with the desired capacity.
 */
public class UpdateTableCapacityRequest extends Request {
	private static final long serialVersionUID = 2136807209259641605L;
	private int tableId;
	private int newCap;
	/**
	 * Constructs an UpdateTableCapacityRequest.
	 * 
	 * @param tableId The ID of the table to be updated.
	 * @param newCap  The new capacity for the table.
	 */
	public UpdateTableCapacityRequest(int tableId, int newCap) {
		super(RequestType.UPDATE_TABLE_CAPACITY, "");
		this.tableId = tableId;
		this.newCap = newCap;
	}
	
	public int getTableId() {
		return tableId;
	}
	
	public int getnewCap() {
		return newCap;
	}

}
