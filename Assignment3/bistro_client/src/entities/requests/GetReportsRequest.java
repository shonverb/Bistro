package entities.requests;

/**
 * Represents a request to retrieve reports from the server. This request does
 * not require any query parameters as the server handles the logic for fetching
 * reports internally.
 */
public class GetReportsRequest extends Request {
    private static final long serialVersionUID = -2223654504686094882L;
	private int month;
    private int year;

    // Default constructor (Previous Month)
    public GetReportsRequest() {
        super(RequestType.GET_REPORTS, "");
        // Logic to default to previous month can be handled here or server-side
        this.month = -1; 
        this.year = -1;
    }

    // New constructor for specific date
    public GetReportsRequest(int month, int year) {
        super(RequestType.GET_REPORTS, "");
        this.month = month;
        this.year = year;
    }

    public int getMonth() { return month; }
    public int getYear() { return year; }
}