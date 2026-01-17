package bistro_server;

import javafx.application.Application;
import javafx.stage.Stage;
/**
 * The entry point for the server side of the app
 */
public class ServerUI extends Application {
	/**
	 * The main frame of the server UI
	 */
	private  static MainScreenServerController aFrame;
	
	/**
	 * The default port for the server
	 */
	final public static String DEFAULT_PORT = "5556";
	
	/**
	 * The main method that runs the server and launches the UI
	 * 
	 * @param args command line arguments
	 * @throws Exception if an error occurs
	 */
	public static void main( String args[] ) throws Exception
   { 	
		BistroServer.runServer(DEFAULT_PORT);
	    launch(args);  
   }
	
	/**
	 * Starts the JavaFX application
	 * 
	 * @param primaryStage the primary stage for this application
	 * @throws Exception if an error occurs
	 */
	public void start(Stage primaryStage) throws Exception {
		aFrame = new MainScreenServerController();
		 
		aFrame.start(primaryStage);
	}
	
	/**
	 * Updates the server UI with a new message
	 * 
	 * @param msg the message to be displayed
	 */
	public static void updateInScreen(String msg) {
		if (aFrame != null)
			aFrame.updateTxt(msg+"\n");
	}
}
