package bistro_client;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import boundry.IController;
import ocsf.client.*;
/**
 * The client itself, extending abstract client and responsible for sending and recieving messages from the server
 * */
public class BistroClient extends AbstractClient{
	/**The controller of the screen currently being displayed*/
    private IController controller;
    public static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**variables for synchronous communication*/
    private volatile boolean waitingForResponse = false;
    private Object synchResponse = null;
    private final Object lock = new Object();
    
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
	
	public Object sendAndWait(Object msg) {
		synchronized(lock) {
			try {
				synchResponse = null;
				waitingForResponse = true;
				
				sendToServer(msg);
				
				lock.wait(5000); // wait for response or timeout after 5 seconds
				
			} catch(IOException | InterruptedException e) {
				e.printStackTrace();
				return null;
			} finally {
				waitingForResponse = false;
			}
		}
		return synchResponse;
	}
	
	/**
	 * handling message from server
	 */
	@Override
	protected void handleMessageFromServer(Object msg) {
		synchronized(lock) {
			if (waitingForResponse) {
				synchResponse = msg;
				waitingForResponse = false;
				lock.notifyAll();
				return;
			}
		}
		if (controller != null) {
			controller.setResultText(msg);	
		}
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