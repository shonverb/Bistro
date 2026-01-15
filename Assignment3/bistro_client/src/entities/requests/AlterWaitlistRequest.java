package entities.requests;

/**
 * A concrete implementation of Request for removing a customer from the waiting list.
 */
public class AlterWaitlistRequest extends Request {
    private static final long serialVersionUID = -7930899150645985508L;
	private String confCode;

    /**
     * @param orderNum The unique identifier for the waitlist entry to be removed.
     */
    public AlterWaitlistRequest(String confCode,RequestType type) {
        // We pass the RequestType and an empty query as the logic is handled by the DLL in the server.
        super(type, "");
        this.confCode = confCode;
    }

    public String getConfCode() {
        return confCode;
    }
}