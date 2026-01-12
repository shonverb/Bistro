package boundry;

import entities.Subscriber;
import entities.User;
import entities.UserType;
import entities.requests.OrderHistoryRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
/**
 * Controller class for the All Orders Screen.
 */
public class HistoryScreenController implements IController {
	private Subscriber user;
    @FXML
    private Button backBtn;

    @FXML
    private Button orderHistoryBtn;

    @FXML
    private TextArea orderHistoryText;

    /**
     * 	Initializes the controller class.
     */
    @FXML
    void initialize() {
    	ClientUI.console.setController(this);
    }
    
    /**
	 * Handles the back button click event.
	 * Navigates to the appropriate screen based on user type.
	 */
    @FXML
    void onBackClick(ActionEvent event) {
		if(user.getType()==UserType.SUBSCRIBER) {
			ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/ClientScreen.fxml", user);
		}
		else {
			ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/WorkerScreen.fxml", user);
		}
    }
    
    /**
	 * Handles the order history button click event.
	 */
    @FXML
    void onOrderHistoryClick(ActionEvent event) {
    	OrderHistoryRequest req = new OrderHistoryRequest(user.getSubscriberID()+"");
    	ClientUI.console.accept(req);   	
    }

    /**
     * Sets the result text in the order history text area.
     */
	@Override
	public void setResultText(Object result) {
		orderHistoryText.setText((String)result);
	}

	/**
	 * Sets the user for this controller.
	 */
	@Override
	public void setUser(User user) {
		this.user = (Subscriber)user;
		
	}

}
