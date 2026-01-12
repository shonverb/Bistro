package bistro_client;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import boundry.IController;
import ocsf.client.*;
/**
 * The client itself, extending abstract client and responsible for sending and recieving messages from the server
 * */
public class BistroClient extends AbstractClient{
	/**The controller of the screen currently being displayed*/
    private IController controller;
    public static LocalDateTime dateTime = LocalDateTime.of(LocalDate.of(2026, 1, 21), LocalTime.of(12, 00));
    public static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * creating client and connecting it to server
     * @param host
     * @param port
     */
	public BistroClient(String host, int port) {
        super(host, port);
        try {
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	/**
	 * handling message from server
	 */
	@Override
	protected void handleMessageFromServer(Object msg) {
	    controller.setResultText(msg);
	}

    /**Setting the controller field whenever a screen is switched*/
    public void setController(IController controller) {
    	this.controller = controller;
    }
    
    /**
     * closing connection between client to server
     */
    public void quit() {
    	System.out.println("Closing connection to client: "+ this.toString());
        try {
            closeConnection();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(0);
        }
    }

}