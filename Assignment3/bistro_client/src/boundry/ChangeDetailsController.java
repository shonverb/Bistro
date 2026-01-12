package boundry;

import entities.Subscriber;
import entities.User;
import entities.UserType;
import entities.requests.UpdateDetailsRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Controller class for handling user detail changes.
 */
public class ChangeDetailsController implements IController {
	private Subscriber user;
    @FXML
    private Button backBtn;

    @FXML
    private TextField emailTxt;

    @FXML
    private TextField fullNameTxt;

    @FXML
    private TextField phoneNumberTxt;

    @FXML
    private Button updateBtn;

    @FXML
    private TextField userNameTxt;

	/**
	 * Initializes the controller and sets up input validation.
	 */
    @FXML
    void initialize() {
    	System.out.println("initialize ChangeDetailsController");
    	ClientUI.console.setController(this);
    	phoneNumberTxt.textProperty().addListener((obs, oldValue, newValue) -> {
    	    if (!newValue.matches("\\d*")) {
    	    	phoneNumberTxt.setText(oldValue);
    	    }
    	});
    }
    
	/**
	 * Handles the back button click event to navigate to the appropriate screen.
	 * 
	 * @param event The action event triggered by clicking the back button.
	 */
    @FXML
    void onBackBtnClick(ActionEvent event) {
    	if(user.getType() ==UserType.SUBSCRIBER) {
    		ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/ClientScreen.fxml", user);
    	}
    	else {
			ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/WorkerScreen.fxml", user);
		}
    }
    
	/**
	 * Handles the update button click event to update user details.
	 * 
	 * @param event The action event triggered by clicking the update button.
	 */
    @FXML
    void onUpdateClick(ActionEvent event) {
    	boolean emailException = false;
    	boolean phoneException = false;
    	String query = "UPDATE `user` SET";
    	if(!fullNameTxt.getText().isEmpty()) {
			query += " full_name = '" + fullNameTxt.getText() + "',";
		}
    	if(!emailTxt.getText().isEmpty()) {
    		if(!isValidEmail(emailTxt.getText()))
    			emailException = true;
    		else
    			query += " email = '" + emailTxt.getText() + "',";
    	}
    	if(!phoneNumberTxt.getText().isEmpty()) {
    		if(phoneNumberTxt.getText().length() != 10)
    			phoneException = true;
    		else
    			query += " phone_number = '" + phoneNumberTxt.getText() + "',";
    	}
    	if(!userNameTxt.getText().isEmpty()) {
			query += " username = '" + userNameTxt.getText() + "',";
		}
    	if(emailException) {
			Alert alert = new Alert(AlertType.ERROR);
    		alert.setTitle("Error Occurred");
    		alert.setHeaderText("Input Validation Failed");
    		alert.setContentText("Please enter valid email");
    		alert.showAndWait();
    	}
    	if(phoneException) {
			Alert alert = new Alert(AlertType.ERROR);
    		alert.setTitle("Error Occurred");
    		alert.setHeaderText("Input Validation Failed");
    		alert.setContentText("Please enter valid phone number with 10 digits");
    		alert.showAndWait();
    	}
    	if((!emailException) && (!phoneException)) {
        	query = query.substring(0, query.length() - 1); // remove last comma
        	query += " WHERE subscriber_id = " + user.getSubscriberID();
        	UpdateDetailsRequest req = new UpdateDetailsRequest(query);
        	ClientUI.console.accept(req);
    	}   	
    }

    /**
     * Sets the result text to display update details.
     */
	@Override
	public void setResultText(Object result) {
		 Platform.runLater(() -> {
		        Alert alert = new Alert(AlertType.INFORMATION);
		        alert.setTitle("Update Details");
		        alert.setHeaderText(null);
		        alert.setContentText((String) result);
		        alert.showAndWait();
		    });	}

	/**
	 * Sets the user for this controller.
	 * 
	 * @param user The user to be set.
	 */
	@Override
	public void setUser(User user) {
		this.user = (Subscriber) user;
	}

	/**
	 * Validates the email format.
	 * 
	 * @param email The email string to be validated.
	 * @return true if the email format is valid, false otherwise.
	 */
	public boolean isValidEmail(String email) {
	    return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
	}

}
