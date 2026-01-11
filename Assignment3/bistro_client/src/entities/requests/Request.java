package entities.requests;

import java.io.Serializable;

/**
 * The Request class is an abstract class that holds information and a query that needs 
 * to go through the server 
 */
public abstract class Request implements Serializable {
	private static final long serialVersionUID = 4658395716273441025L;
	/** describing the type of the request*/
	private RequestType type;
	/** a string which is the associated query of each request */
	private String query;
	
	
	public Request(RequestType type, String query) {
		this.type = type;
		this.query = query;
	}
	/** 
	 * @return the request's query
	 * */
	public String getQuery() {
		return query;
	}
	/**
	 * @return the request's type
	 * */
	public RequestType getType() {
		return type;
	}
	/**
	 * @param type the request's type
	 * */
	public void setType(RequestType type) {
		this.type = type;
	}
}
