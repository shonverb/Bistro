package entities.requests;

import java.time.LocalDateTime;

public class IsBistroOpenRequest extends Request {
		private static final long serialVersionUID = 1L;
		private LocalDateTime datetime;
	/**
	 * Constructs an IsBistroOpenRequest with the specified date and time.
	 * @param dateTime The date and time to check if the bistro is open.
	 */
	public IsBistroOpenRequest(LocalDateTime dateTime) {
		super(RequestType.IS_BISTRO_OPEN, "");
		this.datetime = dateTime;
	}

	public LocalDateTime getDatetime() {
		return datetime;
	}
}
