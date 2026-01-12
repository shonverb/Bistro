package boundry;

import entities.User;
import entities.requests.GetAllActiveOrdersRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

/**
 * * Controller class for the All Orders Screen. Allows a worker to view all
 * active orders in the system.
 */
public class AllOrdersScreenController implements IController {
	private User user;
    @FXML
    private Button backBtn;

    @FXML
    private Button displayOrdersBtn;

    @FXML
    private TextArea resultTxt;
    
	/**
	 * Initializes the controller and sets the console's controller to this
	 * instance.
	 */
    @FXML
    void initialize() {
		ClientUI.console.setController(this);
    }

	/**
	 * Handles the action of clicking the back button. Navigates back to the Worker
	 * Screen.
	 * 
	 * @param event The ActionEvent triggered by clicking the back button.
	 */
    @FXML
    void onBackClick(ActionEvent event) {
		ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/WorkerScreen.fxml", user);
    }

	/**
	 * Handles the action of clicking the display orders button. Sends a request to
	 * the server to retrieve all active orders.
	 * 
	 * @param event The ActionEvent triggered by clicking the display orders button.
	 */
    
    @FXML
    void onDisplayOrdersClick(ActionEvent event) {
    	GetAllActiveOrdersRequest req = new GetAllActiveOrdersRequest();
    	ClientUI.console.accept(req);
    }
    
    /** Sets the result text area with the provided result string.*/
	@Override
	public void setResultText(Object result) {
		resultTxt.setText((String)result);
	}

	/** Sets the user for this controller.*/
	@Override
	public void setUser(User user) {
		this.user = user;		
	}
	
}
