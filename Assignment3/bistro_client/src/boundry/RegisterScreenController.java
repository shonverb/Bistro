package boundry;
import java.util.ArrayList;
import java.util.Random;

import entities.Subscriber;
import entities.User;
import entities.requests.RegisterRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
/**
 * A controller for the registration screen
 */
public class RegisterScreenController implements IController{
	/** the current user of the session*/
	private User user;
	/** for setting the subscriber id*/
	Random random;
	
    @FXML
    private Button cancelBtn;

    @FXML
    private TextField email;

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private TextField phoneNumber;

    @FXML
    private Button submitBtn;

    @FXML
    private TextField userName;
    
    @FXML
    private TextArea resultTxt;

	/**
	 * initializes the controller
	 */
	@FXML
	void initialize() {
		random = new Random();
		ClientUI.console.setController(this);
    	phoneNumber.textProperty().addListener((obs, oldValue, newValue) -> {
    	    if (!newValue.matches("\\d*")) {
    	    	phoneNumber.setText(oldValue);
    	    }
    	});
	}

    /**
     * when the user clicks 'cancel'
     * */
    @FXML
    void onCancelClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/WorkerScreen.fxml",user);
    }
    /**
     * when the user clicks 'Submit' (registering the user)
     * @param event
     */
    @FXML
    void onSubmitClick(ActionEvent event) {
    	boolean emptyException = false;
    	boolean emailException = false;
    	boolean phoneException = false;
    	String fname = firstName.getText().trim();
    	String lname = lastName.getText().trim();
    	String phone = phoneNumber.getText().trim();
    	String userName = this.userName.getText().trim();
    	String email = this.email.getText().trim();
    	String status = "CLIENT";
    	if(fname.isEmpty() || lname.isEmpty() || phone.isEmpty() || email.isEmpty() || userName.isEmpty()) {
    		emptyException = true;
    	}
    	if(!isValidEmail(email)) {
    		emailException = true;
    	}
    	if(phone.length() != 10) {
    		phoneException = true;
    	}
    	if((!emailException) && (!phoneException) && (!emptyException)) {
    		int generatedId = this.random.nextInt(1_000_000);
    		Subscriber s = new Subscriber(generatedId,userName,fname,lname,phone,email,status,new ArrayList<>());
    		RegisterRequest r = new RegisterRequest(s);
    		ClientUI.console.accept(r);
    		user = s;
    	}
    	if(emptyException) {
    		Alert alert = new Alert(AlertType.ERROR);
    		alert.setTitle("Error Occurred");
    		alert.setHeaderText("Input Validation Failed");
    		alert.setContentText("please enter a value in all fields");
    		alert.showAndWait();
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
    }

    /**
     * validates email format
     * @param email
     * @return
     */
    public boolean isValidEmail(String email) {
	    return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
	}

	/**
	 * sets the result text area
	 */
	@Override
	public void setResultText(Object result) {
		resultTxt.setText((String)result);
	}

	/**
	 * sets the user of the session
	 */
    public void setUser(User user) {
    	this.user = user;
    }
}
