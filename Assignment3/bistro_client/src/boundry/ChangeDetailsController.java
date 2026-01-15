package boundry;

import javafx.scene.control.TextArea;

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
    
    @FXML
    private TextArea nameArea;
    @FXML
    private TextArea mailArea;
    @FXML
    private TextArea phoneArea;
    @FXML
    private TextArea usernameArea;
    @FXML
    private TextArea idArea;
    
    

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
    	boolean nameException = false;
    	
    	String query = "UPDATE `user` SET";
    	if(!fullNameTxt.getText().isEmpty()) {
    		String[] names = fullNameTxt.getText().trim().split("\\s+");
    		
    		
    		if(names.length != 2) {
    	        nameException = true;
    	    } else {
    	        // NOTE: Depending on your DB, you might need to update 'full_name' or separate columns.
    	        // This keeps your original query format but uses the validated text.
    	        query += " full_name = '" + names[0] + " " + names[1] + "',"; 
//    			query += " full_name = '" + fullNameTxt.getText() + "',";

    	    }
    		
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
    	
    	if(nameException) {
    	    Alert alert = new Alert(AlertType.ERROR);
    	    alert.setTitle("Error Occurred");
    	    alert.setHeaderText("Input Validation Failed");
    	    alert.setContentText("Please enter exactly two words (First Name and Last Name) separated by a space.");
    	    alert.showAndWait();
    	}
    	
    	if((!emailException) && (!phoneException) && (!nameException)) {
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
		        
		        if ( ((String) result).equals("Details updated successfully.") ) {
		            if (!fullNameTxt.getText().isEmpty()) {
		                String[] names = fullNameTxt.getText().trim().split("\\s+");
		                this.user.setFirstName(names[0]); // Set First Name
		                this.user.setLastName(names[1]);  // Set Last Name
		            }

		            this.user.setEmail(emailTxt.getText().isEmpty() ? this.user.getEmail() : emailTxt.getText());
		            this.user.setPhone(phoneNumberTxt.getText().isEmpty() ? this.user.getPhone() : phoneNumberTxt.getText());
		            this.user.setUserName(userNameTxt.getText().isEmpty() ? this.user.getUserName() : userNameTxt.getText());
		            
		            showDetails();
		        }
		        
		    });	
		 
		 
	}

	/**
	 * Sets the user for this controller.
	 * 
	 * @param user The user to be set.
	 */
	@Override
	public void setUser(User user) {
		this.user = (Subscriber) user;
		
		showDetails();
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
	
	private void showDetails() {
		nameArea.setText(this.user.getFirstName() + " " + this.user.getLastName());
	    
	    mailArea.setText(this.user.getEmail());
	    phoneArea.setText(this.user.getPhone());
	    usernameArea.setText(this.user.getUserName());
	    idArea.setText("" + this.user.getSubscriberID());
	}
	
}
