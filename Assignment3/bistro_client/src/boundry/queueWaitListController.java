package boundry;

import java.io.IOException;
import java.util.Optional;

import entities.User;
import entities.requests.AlterWaitlistRequest;
import entities.requests.CancelRequest;
import entities.requests.Request;
import entities.requests.RequestType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

/**
 * Controller class for managing the queue waitlist functionality in the client
 * application. This class handles user interactions related to spotting a
 * position in the waitlist and leaving the waitlist.
 */
public class queueWaitListController implements IController{
	private User user;
	@FXML private TextField confCodeTxt;
	@FXML private TextArea resultTxt;
	@FXML private Button spotBtn;
	@FXML private Button leaveWaitListBtn;
	@FXML private Button backBtn;

	/**
	 * Initializes the controller by setting the console reference.
	 */
	@FXML
    void initialize() {
    	ClientUI.console.setController(this);
    }

	/**
	 * Handles the action when the "Spot" button is clicked. Validates input and
	 * sends a request to spot a position in the waitlist.
	 * 
	 * @param event The action event triggered by clicking the button.
	 */
	@FXML
	void onSpotBtnClick(ActionEvent event) {
		boolean exceptionRaised = false;
    	int code = 0;
    	try {
    		//parsing integers fields
    		code = Integer.parseInt(confCodeTxt.getText().trim());
    		if(code <=0) {
    			exceptionRaised = true;
    		}
    	}catch (NumberFormatException e) { 
			exceptionRaised = true;
			confCodeTxt.clear();
    	}
    	if(exceptionRaised) {
    		Alert alert = new Alert(AlertType.ERROR);
    	    alert.setTitle("Error Occurred");
    	    alert.setHeaderText("Input Validation Failed");
    	    alert.setContentText("you cannot enter non-positive number");
    	    alert.showAndWait();
    	}
    	else {
        	AlterWaitlistRequest r = new AlterWaitlistRequest(confCodeTxt.getText().trim(),RequestType.SPOT_WAITLIST);
            ClientUI.console.accept(r);
        	
    	}
	}

	/**
	 * Handles the action when the "Leave Waitlist" button is clicked. Validates
	 * input and sends a request to leave the waitlist.
	 * 
	 * @param event The action event triggered by clicking the button.
	 */
	@FXML
	void onLeaveBtnClick(ActionEvent event) throws IOException, InterruptedException{
		boolean exceptionRaised = false;
    	int code = 0;
    	try {
    		//parsing integers fields
    		code = Integer.parseInt(confCodeTxt.getText().trim());
    		if(code <=0) {
    			exceptionRaised = true;
    		}
    	}catch (NumberFormatException e) { 
			exceptionRaised = true;
			confCodeTxt.clear();
    	}
    	if(exceptionRaised) {
    		Alert alert = new Alert(AlertType.ERROR);
    	    alert.setTitle("Error Occurred");
    	    alert.setHeaderText("Input Validation Failed");
    	    alert.setContentText("you cannot enter non-positive number");
    	    alert.showAndWait();
    	}
    	else {
        	AlterWaitlistRequest r = new AlterWaitlistRequest(confCodeTxt.getText().trim(),RequestType.LEAVE_WAITLIST);
            ClientUI.console.accept(r);
        	
    	}
	}
	
	/**
	 * Handles the action when the "Back" button is clicked. Navigates back to the
	 * @param event
	 */
    @FXML
    void onBackBtnClick(ActionEvent event) {
		ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/ClientScreen.fxml", user);

    }

	/**
	 * Sets the user for this controller.
	 * 
	 * @param user The user to be set.
	 */
	@Override
    public void setUser(User user) {
        this.user = user;
    }

	/**
	 * Sets the result text area with the provided result message.
	 * 
	 * @param result The result message to be displayed.
	 */
	@Override
    public void setResultText(Object result) {
		String message = (String) result;
		System.out.println("Leave waitlist response received: " + message);
		resultTxt.setText(message);
	}

	
}
