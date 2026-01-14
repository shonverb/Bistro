package entities.requests;

/**
 * Request to remove a table from the database.
 */
public class RemoveTableRequest extends Request {
	private static final long serialVersionUID = -108745930514193747L;
	int tableID;
	
	/**
	 * Constructor for RemoveTableRequest.
	 * 
	 * @param tableID The ID of the table to be removed.
	 */
	public RemoveTableRequest(int tableID) {
		super(RequestType.REMOVE_TABLE, "UPDATE tables\n"
				+ "SET active_to = CURRENT_DATE + INTERVAL 1 MONTH\n"
				+ "WHERE id = ?;");
		this.tableID = tableID;
	}
	
	public int getId() {
		return tableID;
	}

}
