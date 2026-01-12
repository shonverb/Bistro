package boundry;

import java.util.Optional;

import entities.User;
import entities.UserType;
import entities.requests.CancelRequest;
import entities.requests.LeaveTableRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
/**
 * Controller class for managing orders in the application.
 */
public class AppOrderManagementController implements IController {
	private User user;
    @FXML
    private Button backBtn;

    @FXML
    private Button cancelOrderBtn;

    @FXML
    private TextField confCodeTxt;
    
    @FXML
    private Button finishOrderBtn;

    @FXML
    private Button newOrderBtn;

    @FXML
    void initialize() {
    	ClientUI.console.setController(this);
    }
    
    /**
	 * Handles the action when the back button is clicked.
	 * Navigates to the appropriate screen based on user type.
	 * 
	 * @param event The action event triggered by clicking the back button.
	 */
    
    @FXML
    void onBackClick(ActionEvent event) {
    	if((user.getType() == UserType.SUBSCRIBER) || (user.getType() == UserType.GUEST)) {
			ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/ClientScreen.fxml", user);
		}
		else {
			ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/WorkerScreen.fxml", user);
		}
	}
    
    /**
	 * Handles the action when the cancel order button is clicked.
	 * Validates input and sends a cancel request if confirmed.
	 * @param event The action event triggered by clicking the cancel order button.
	 */
    @FXML
    void onCancelOrderClick(ActionEvent event) {
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
    		Alert alert = new Alert(AlertType.CONFIRMATION);
        	alert.setTitle("Confirmation");
        	alert.setHeaderText("Your order will be cancelled");
        	alert.setContentText("Are you sure you want to continue?");
        	Optional<ButtonType> result = alert.showAndWait();
        	if (result.isPresent() && result.get() == ButtonType.OK) {
        		CancelRequest c = new CancelRequest(confCodeTxt.getText().trim());
            	ClientUI.console.accept(c);
        	}
    	}
    }
    
    /**
     * Handles the action when the finish order button is clicked.
     * @param event
     */
    @FXML
    void onFinishOrderClick(ActionEvent event) {
    	int res = -1;
    	try {
    		res = Integer.parseInt(confCodeTxt.getText().trim());
    	} catch (NumberFormatException e) {
    		Alert alert = new Alert(AlertType.ERROR);
    	    alert.setTitle("Error Occurred");
    	    alert.setHeaderText("Input Validation Failed");
    	    alert.setContentText("Please enter an integer as a confirmation code");
    	    alert.showAndWait();
    	}
    	if (res<=0) {
    		Alert alert = new Alert(AlertType.ERROR);
    	    alert.setTitle("Error Occurred");
    	    alert.setHeaderText("Input Validation Failed");
    	    alert.setContentText("Please enter a positive integer as a confirmation code");
    	    alert.showAndWait();
    	}
    	else {
        	LeaveTableRequest r = new LeaveTableRequest(confCodeTxt.getText().trim());
        	ClientUI.console.accept(r);
    	}
    }

	/**
	 * Handles the action when the new order button is clicked.
	 * 
	 * @param event
	 */
    @FXML
    void onNewOrderClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/OrderScreen.fxml", user);

    }

    /**
     * Sets the result text in an alert dialog.
     */
	@Override
	public void setResultText(Object result) {
		Platform.runLater(()->{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Operation Result");
		    alert.setHeaderText(null);
		    alert.setContentText((String) result);
		    alert.showAndWait();
		}
		);
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

}
