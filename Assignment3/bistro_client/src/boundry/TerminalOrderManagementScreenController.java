package boundry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import entities.Subscriber;
import entities.User;
import entities.UserType;
import entities.requests.AlterWaitlistRequest;
import entities.requests.CancelRequest;
import entities.requests.CheckConfCodeRequest;
import entities.requests.GetTableRequest;
import entities.requests.GetUserActiveOrdersRequest;
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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;


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
    private ComboBox<String> confCodeCombo;

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
        
            // Text field is visible/managed ONLY when NOT logged in
            confCodeTxt.visibleProperty().bind(isLoggedIn.not());
            confCodeTxt.managedProperty().bind(isLoggedIn.not());

            // ComboBox is visible/managed ONLY when logged in
            confCodeCombo.visibleProperty().bind(isLoggedIn);
            confCodeCombo.managedProperty().bind(isLoggedIn);
        }
   
    /**
     * Helper method to get the code from the correct input field
     * based on user state.
     */
    private String getEffectiveConfCode() {
        if (isLoggedIn.get() && confCodeCombo != null) {
            String selection = confCodeCombo.getValue();
            return (selection == null) ? "" : selection.trim();
        } else {
            return confCodeTxt.getText().trim();
        }
    }

    @FXML
    void onCancelOrderClick(ActionEvent event) {
        String rawCode = getEffectiveConfCode();
        
        boolean exceptionRaised = false;
        int code = 0;
        try {
            code = Integer.parseInt(rawCode);
            if(code <= 0) {
                exceptionRaised = true;
            }
        } catch (NumberFormatException e) { 
            exceptionRaised = true;
            if(!isLoggedIn.get()) confCodeTxt.clear(); 
        }

        if(exceptionRaised) {
            showError("Input Validation Failed", "Please enter (or select) a valid positive number.");
        }
        else {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Your order will be cancelled");
            alert.setContentText("Are you sure you want to continue?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                CancelRequest c = new CancelRequest(rawCode);
                ClientUI.console.accept(c);
            }
        }
    }
    
    @FXML
    void onbackClick(ActionEvent event) {
        ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/TerminalScreen.fxml", user);
    }

    @FXML
    void onLostMyCodeClick(ActionEvent event) {
        
        String contact;

        // GUEST
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

            if (result.isEmpty()) return;

            contact = result.get().trim();

            if (contact.isEmpty() || !isValidPhoneOrEmail(contact)) {
                showError("Invalid contact", "Please enter a valid phone number or email used in the order.");
                return;
            }
        }
        // SUBSCRIBER
        else {
            contact = user.getEmail();
        }
        
        ClientUI.console.accept(new CheckConfCodeRequest(contact));
    }

    @FXML
    void onLeaveTableClick(ActionEvent event) {
        String confcode = getEffectiveConfCode(); // Use helper
        
        if (confcode.isEmpty() || !confcode.matches("\\d+")) {
            showError("Error Occurred", "Please enter/select a valid confirmation code.");
            return;
        }
        LeaveTableRequest leaveTableRequest = new LeaveTableRequest(confcode);
        ClientUI.console.accept(leaveTableRequest);     
        try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        if(isLoggedIn.get()) {
        	loadUserActiveOrders();
        }
    }

    @FXML
    void onGetTableClick(ActionEvent event) {
        String confcode = getEffectiveConfCode(); // Use helper
        
        if (confcode.isEmpty() || !confcode.matches("\\d+")) {
            showError("Error Occurred", "Please enter/select a valid confirmation code.");
            return;
        }
        ClientUI.console.accept(new GetTableRequest(confcode));
    }
    
    @FXML
    void onLeaveWaitingListBtnClick(ActionEvent event) throws IOException, InterruptedException{
        String rawCode = getEffectiveConfCode(); // Use helper
        
        boolean exceptionRaised = false;
        int code = 0;
        try {
            code = Integer.parseInt(rawCode);
            if(code <= 0) {
                exceptionRaised = true;
            }
        } catch (NumberFormatException e) { 
            exceptionRaised = true;
            if(!isLoggedIn.get()) confCodeTxt.clear();
        }
        
        if(exceptionRaised) {
            showError("Input Validation Failed", "Please enter (or select) a valid positive number.");
        }
        else {
            AlterWaitlistRequest r = new AlterWaitlistRequest(rawCode, RequestType.LEAVE_WAITLIST);
            ClientUI.console.accept(r);
        }
    }

    @Override
    public void setResultText(Object result) {
        if(result instanceof ArrayList) {
        	List<String> confCodes = (List<String>) result;
        	Platform.runLater(() -> {
                if (confCodeCombo != null) {
                    confCodeCombo.getItems().clear(); // Clear old data
                    
                    if (confCodes.isEmpty()) {
                        confCodeCombo.setPromptText("No active orders found");
                    } else {
                        confCodeCombo.getItems().addAll(confCodes);
                        confCodeCombo.setPromptText("Select an order...");
                        // Optional: Select the first one automatically
                        confCodeCombo.getSelectionModel().selectFirst();
                    }
                }
            });
        }
        else {
    	Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Operation result");
            alert.setHeaderText(null);
            alert.setContentText((String) result);
            alert.showAndWait();
        });
        }
    }

    @Override
    public void setUser(User user) {
        this.user = user;   
        isLoggedIn.set(user != null && user.getType() != UserType.GUEST);
        
        if (isLoggedIn.get()) {
            loadUserActiveOrders();
        }
    }
    
    /**
     * method to load orders into the ComboBox.
     * 
     */
    private void loadUserActiveOrders() {
        if(confCodeCombo == null) return;
        
        confCodeCombo.getItems().clear();
        
        ClientUI.console.accept(new GetUserActiveOrdersRequest(((Subscriber)user).getSubscriberID()));
        try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
    }
    
	/**
	 * Validates if the given string is a valid phone number or email address.
	 * 
	 * @param s The string to validate.
	 * @return true if valid, false otherwise.
	 */
    public boolean isValidPhoneOrEmail(String s) {
        boolean looksEmail = s.contains("@") && s.contains(".") && s.indexOf('@') > 0;
        String digits = s.replaceAll("[^0-9]", "");
        boolean looksPhone = digits.length() >= 9 && digits.length() <= 15;
        return looksEmail || looksPhone;
    }


    // Helper for simple error alerts to clean up code
    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error Occurred");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}