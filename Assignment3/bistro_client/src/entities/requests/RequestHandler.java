package entities.requests;

/**
 * an interface that each method of the database connector implements to allow
 * for the strategy pattern 
 * */
@FunctionalInterface
public interface RequestHandler {
	Object handle(Request r);
}
