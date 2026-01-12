package bistro_client;


import java.io.IOException;

import boundry.IController;
import entities.User;
import entities.requests.Request;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
/**
 * A wrapper around the client, sending messages and holding the method to switch screens
 */
public class Console {
	/**the BistroClient instance*/
    private static BistroClient bc;
    
    /**default port number*/
    final public static int DEFAULT = 5556;

    /**constructor*/
    public Console(String host, int port) {
        bc = new BistroClient(host,port);
    }
    
    /**setting the controller*/
  	public void setController(IController controller) {
    	  bc.setController(controller);
  	}
  	
  	/**sending request to server*/
    public void accept(Request r) 
    {
    	System.out.println("in accept");
    	try {
			bc.sendToServer(r);
		} catch (IOException e) {
			System.out.println("Error sending request to server!");
			e.printStackTrace();
		}
    }
    /**
     * A method that switches between screens
     * @param controller the controller of the screen switched from
     * @param event the event the triggers the switch (usually a button click)
     * @param newScreenPath a string which holds the path to the new screen
     * @param user the user using the session (to use setUser)
     */
	public void switchScreen(Object controller, ActionEvent event, String newScreenPath, User user) { //switching screens
		try{
        	FXMLLoader loader = new FXMLLoader(getClass().getResource(newScreenPath));
        	Parent root = loader.load();
        	Object main = loader.getController();
        	if (user != null) {
        		((IController)main).setUser(user);
        	}
        	Scene scene = new Scene(root);
        	Stage primaryStage = new Stage();
        	((Node)event.getSource()).getScene().getWindow().hide();    // Hide primary window (current window)
        	primaryStage.setScene(scene);
        	primaryStage.show();
    	} catch (IOException e) {
    		e.printStackTrace();
    		System.out.println("Couldn't switch to screen " + newScreenPath);
    	}          
	}
	
	/**quitting the client*/
	public void quit() {
		bc.quit();
	}
	

}