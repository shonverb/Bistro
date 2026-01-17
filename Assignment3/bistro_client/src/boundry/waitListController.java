package boundry;

import java.time.LocalDateTime;
import java.util.Optional;

import bistro_client.BistroClient;
import entities.User;
import entities.UserType;
import entities.requests.GetHoursDateRequest;
import entities.requests.GetHoursDayRequest;
import entities.requests.GetMaxTableRequest;
import entities.requests.JoinWaitlistRequest;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

/**
 * Controller for the Waitlist screen. Handles user interactions for joining the
 * waitlist.
 */
public class waitListController implements IController {
    private User user;
    /** Property to track login status */
    private final BooleanProperty isLoggedIn = new SimpleBooleanProperty(false);

    @FXML private ComboBox<Integer> guestsNumberComboBox;
    @FXML private TextField identifyText; // This serves as the Contact field for Guests
    @FXML private Label idLabel;
    @FXML private TextArea resultTxt;
    @FXML private Button backBtn;
    @FXML private Button submitBtn;
    private Timeline timeline;
    private Integer maxGuests;

    /** Initializes the controller and binds UI elements based on user status */
    @FXML
    public void initialize() {
        ClientUI.console.setController(this);
        
        // Requirement: Hide identification fields for logged-in Subscribers
        identifyText.visibleProperty().bind(isLoggedIn.not());
        identifyText.managedProperty().bind(isLoggedIn.not());
        idLabel.visibleProperty().bind(isLoggedIn.not());
        idLabel.managedProperty().bind(isLoggedIn.not());
        // Populate guests number ComboBox
        maxGuests = (int)ClientUI.console.sendAndWait(new GetMaxTableRequest());
        for (int i = 1; i <= maxGuests; i++) {
			guestsNumberComboBox.getItems().add(i);
		}
        setUpTimeline();
        
    }
    private void setUpTimeline() {
		timeline = new Timeline(new KeyFrame(Duration.seconds(5),event -> {
			ClientUI.console.accept(new GetMaxTableRequest());
		}));
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
	}

	/** Handles the Join Waitlist button click event
     * @param event The action event triggered by the button click
    */
    @FXML
    public void onJoinWaitlistClick(ActionEvent event) {
        int guests = guestsNumberComboBox.getValue();
        // Use identifyText since it matches your FXML "Email / Phone Number" field
        String contactInput = identifyText.getText().trim(); 

        
        
        // 2. Validate Contact (Required only for Guests)
        if (user.getType() == UserType.GUEST) {
            if (contactInput.isEmpty() || !isValidPhoneOrEmail(contactInput)) {
                resultTxt.setText("❌ Please enter a valid contact (phone or email).");
                return;
            }
        }

        // 3. Prepare data for JoinWaitlistRequest
        String subscriberId = (user.getType() != UserType.GUEST) ? 
                             String.valueOf(((entities.Subscriber)user).getSubscriberID()) : "0";
        String finalContact = (user.getType() != UserType.GUEST && contactInput.isEmpty()) ? 
                             user.getPhone() : contactInput;
        String altDateTime = LocalDateTime.now().format(BistroClient.fmt);

        // 4. Send Request
        ClientUI.console.accept(new JoinWaitlistRequest(
            altDateTime, guests+"", subscriberId, finalContact
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
    	if (result instanceof Integer) {
			int newMax = (int) result;
	        
	        // Only touch the UI if the capaciTableCapacityty actually changed!
	        if (this.maxGuests == null || this.maxGuests != newMax) {
	            this.maxGuests = newMax;
	            
	            Platform.runLater(() -> {
	                Integer currentlySelected = guestsNumberComboBox.getValue();
	                
	                guestsNumberComboBox.getItems().clear();
	                for (int i = 1; i <= newMax; i++) {
	                    guestsNumberComboBox.getItems().add(i);
	                }
	                
	                if (currentlySelected != null && currentlySelected <= newMax) {
	                    guestsNumberComboBox.setValue(currentlySelected);
	                }
	            });
	        }
	        return;
    	}
    	String message = (String) result;
        
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
            String guests = guestsNumberComboBox.getValue().toString();
            String contactInput = identifyText.getText().trim();
            String subscriberId = (user.getType() != UserType.GUEST) ? 
                                 String.valueOf(((entities.Subscriber)user).getSubscriberID()) : "0";
            String finalContact = (user.getType() == UserType.SUBSCRIBER && contactInput.isEmpty()) ? 
                                 user.getPhone() : contactInput;
            String altDateTime = LocalDateTime.now().format(BistroClient.fmt);

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