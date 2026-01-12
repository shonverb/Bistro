package boundry;

import java.io.IOException;
import java.util.Optional;

import entities.User;
import entities.UserType;
import entities.requests.AlterWaitlistRequest;
import entities.requests.CancelRequest;
import entities.requests.CheckConfCodeRequest;
import entities.requests.GetTableRequest;
import entities.requests.LeaveTableRequest;
import entities.requests.RequestType;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar;

/**
 * Controller class for managing orders in the application.
 */
public class TerminalOrderManagementScreenController implements IController {
	private User user;
	/**
	 * Property to track if the user is logged in or a guest.
	 */
	private final BooleanProperty isLoggedIn = new SimpleBooleanProperty(false);

    @FXML
    private Button backBtn;
    
    @FXML
    private Button cancelOrderBtn;

    @FXML
    private Button lostMyCodeBtn;

    @FXML
    private TextField confCodeTxt;

    @FXML
    private Button leaveTableBtn;

    @FXML
    private Button getTableBtn;
    
    @FXML
    private Button leaveWaitingListBtn;

    /**
     * Initializes the controller and sets up necessary configurations.
     */
    @FXML
    void initialize() {
    	ClientUI.console.setController(this);
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
	 * Handles the action when the back button is clicked.
	 * Navigates to the appropriate screen based on user type.
	 * 
	 * @param event The action event triggered by clicking the back button.
	 */
    @FXML
    void onbackClick(ActionEvent event) {
			ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/TerminalScreen.fxml", user);
		}
	
    /**
	 * Handles the action when the cancel order button is clicked.
	 * Validates input and sends a cancel request if confirmed.
	 * @param event The action event triggered by clicking the cancel order button.
	 */
    @FXML
    void onLostMyCodeClick(ActionEvent event) {

        String contact;

        // ---------- GUEST ----------
        if (!isLoggedIn.get()) {

            TextField contactField = new TextField();
            contactField.setPromptText("Enter phone or email used in the order");

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Find Your Order");
            dialog.setHeaderText("Please enter your contact");

            ButtonType confirmBtn = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(contactField);

            dialog.setResultConverter(btn -> {
                if (btn == confirmBtn) {
                    return contactField.getText();
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();

            // user cancelled popup
            if (result.isEmpty()) return;

            contact = result.get().trim();

            if (contact.isEmpty() || !OrderScreenController.isValidPhoneOrEmail(contact)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Occurred");
                alert.setHeaderText("Invalid contact");
                alert.setContentText(
                    "Please enter a valid phone number or email\n" +
                    "that you used when placing the order."
                );
                alert.showAndWait();
                return;
            }
        }

        // ---------- SUBSCRIBER ----------
        else {
            contact = user.getEmail();
        }

        // ---------- SEND REQUEST ----------
        ClientUI.console.accept(new CheckConfCodeRequest(contact));
    }


    /**
     * 	Handles the action when the leave table button is clicked.
     * @param event
     */
    @FXML
    void onLeaveTableClick(ActionEvent event) {
    	String confcode = confCodeTxt.getText().trim();
		if (confcode.isEmpty() ||  !confcode.matches("\\d+")) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error Occurred");
			alert.setContentText("Please enter a valid confirmation code.");
			alert.showAndWait();
			return;
		}
		LeaveTableRequest leaveTableRequest = new LeaveTableRequest(confcode);
		ClientUI.console.accept(leaveTableRequest);   	
    }

	/**
	 * Handles the action when the get table button is clicked.
	 * 
	 * @param event
	 */
    @FXML
    void onGetTableClick(ActionEvent event) {
    	String confcode = confCodeTxt.getText().trim();
    	if (confcode.isEmpty() ||  !confcode.matches("\\d+")) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error Occurred");
			alert.setContentText("Please enter a valid confirmation code.");
			alert.showAndWait();
			return;
		}
    	ClientUI.console.accept(new GetTableRequest(confcode));
    	
    	

    }
    
    /**
	 * Handles the action when the "Leave Waitlist" button is clicked. Validates
	 * input and sends a request to leave the waitlist.
	 * 
	 * @param event The action event triggered by clicking the button.
	 */
	@FXML
	void onLeaveWaitingListBtnClick(ActionEvent event) throws IOException, InterruptedException{
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
     * Sets the result text to be displayed to the user.
     */
	@Override
	public void setResultText(Object result) {
		Platform.runLater(() -> 
		{Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Operation result");
        alert.setHeaderText(null);
	    alert.setContentText((String) result);
	    alert.showAndWait();
		}
	    );
	}

	/**
	 * Sets the user for the controller and updates the logged-in status.
	 */
	@Override
	public void setUser(User user) {
		this.user = user;	
		isLoggedIn.set(user != null && user.getType() != UserType.GUEST);
	}

}

	


