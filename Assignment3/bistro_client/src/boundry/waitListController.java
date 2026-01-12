package boundry;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import bistro_client.BistroClient;
import entities.User;
import entities.UserType;
import entities.requests.JoinWaitlistRequest;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for the Waitlist screen. Handles user interactions for joining the
 * waitlist.
 */
public class waitListController implements IController {
    private User user;
    /** Property to track login status */
    private final BooleanProperty isLoggedIn = new SimpleBooleanProperty(false);

    @FXML private TextField guestsNumberTxt;
    @FXML private TextField identifyText; // This serves as the Contact field for Guests
    @FXML private Label idLabel;
    @FXML private TextArea resultTxt;
    @FXML private Button backBtn;
    @FXML private Button submitBtn;

    /** Initializes the controller and binds UI elements based on user status */
    @FXML
    public void initialize() {
        ClientUI.console.setController(this);
        
        // Requirement: Hide identification fields for logged-in Subscribers
        identifyText.visibleProperty().bind(isLoggedIn.not());
        identifyText.managedProperty().bind(isLoggedIn.not());
        idLabel.visibleProperty().bind(isLoggedIn.not());
        idLabel.managedProperty().bind(isLoggedIn.not());
    }

    /** Handles the Join Waitlist button click event
     * @param event The action event triggered by the button click
    */
    @FXML
    public void onJoinWaitlistClick(ActionEvent event) {
        String guests = guestsNumberTxt.getText().trim();
        // Use identifyText since it matches your FXML "Email / Phone Number" field
        String contactInput = identifyText.getText().trim(); 

        // 1. Validate Guest Count
        if (guests.isEmpty() || !guests.matches("\\d+") || Integer.parseInt(guests) <= 0) {
            resultTxt.setText("Please enter a valid number of guests.");
            return;
        }
        
        // 2. Validate Contact (Required only for Guests)
        if (user.getType() == UserType.GUEST) {
            if (contactInput.isEmpty() || !isValidPhoneOrEmail(contactInput)) {
                resultTxt.setText("❌ Please enter a valid contact (phone or email).");
                return;
            }
        }

        // 3. Prepare data for JoinWaitlistRequest
        String orderDateTime = LocalDate.now().toString() + " " + LocalTime.now().toString().substring(0, 8);
        String subscriberId = (user.getType() != UserType.GUEST) ? 
                             String.valueOf(((entities.Subscriber)user).getSubscriberID()) : "0";
        String finalContact = (user.getType() != UserType.GUEST && contactInput.isEmpty()) ? 
                             user.getPhone() : contactInput;
        String altDateTime = BistroClient.dateTime.format(BistroClient.fmt);

        // 4. Send Request
        ClientUI.console.accept(new JoinWaitlistRequest(
            altDateTime, guests, subscriberId, finalContact
        ));
    }

    /** Helper for contact validation */
    private boolean isValidPhoneOrEmail(String input) {
        return input.matches("^\\d{10}$") || input.contains("@");
    }

	/**
	 * Sets the result text in the UI based on the server response
	 * 
	 * @param result The result object from the server, expected to be a String
	 */
    @Override
    public void setResultText(Object result) {
        String message = (String) result;
        
     // WRAP THE ENTIRE BLOCK: All UI updates (TextAreas, Alerts) must be on FX thread
        Platform.runLater(() -> {
            if (message.contains("PROMPT: NO_SEATS_FOUND")) {
                showWaitlistConfirmPopup();
            } else {
                resultTxt.setText(message);
            }
        });
    }

    /** Displays a confirmation popup for joining the waitlist */
    private void showWaitlistConfirmPopup() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        ButtonType btnJoin = new ButtonType("Join Waitlist");
        ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnJoin, btnCancel);
        alert.setTitle("Waitlist Selection");
        alert.setHeaderText("No open spots found.");
        alert.setContentText("Would you like to enter the waitlist or cancel?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnJoin) {
            resultTxt.setText("⏳ Adding you to the waitlist...");
            
            // Prepare the data again for the second attempt
            String guests = guestsNumberTxt.getText().trim();
            String contactInput = identifyText.getText().trim();
            String subscriberId = (user.getType() == UserType.SUBSCRIBER) ? 
                                 String.valueOf(((entities.Subscriber)user).getSubscriberID()) : "0";
            String finalContact = (user.getType() == UserType.SUBSCRIBER && contactInput.isEmpty()) ? 
                                 user.getPhone() : contactInput;
            String altDateTime = BistroClient.dateTime.format(BistroClient.fmt);

            // SEND SECOND REQUEST with the flag set to TRUE
            ClientUI.console.accept(new JoinWaitlistRequest(
                altDateTime, guests, subscriberId, finalContact, true
            ));
        } else {
            // Desired behavior: return 'canceled' prompt locally
            resultTxt.setText("Registration canceled.");
        }
    }

    /** Sets the user and updates login status */
    @Override
    public void setUser(User user) {
        this.user = user;
        Platform.runLater(() -> {
            isLoggedIn.set(user != null && user.getType() != UserType.GUEST);
        });
    }

	/**
	 * Handles the Back button click event
	 * 
	 * @param event The action event triggered by the button click
	 */
    @FXML
    void OnBackBtnClick(ActionEvent event) {
        try {
            ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/TerminalScreen.fxml", user);
        } catch (Exception e) {
            resultTxt.setText("Error switching screens.");
        }
    }
}