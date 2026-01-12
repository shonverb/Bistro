package boundry;

import java.io.IOException;
import java.util.List;

import entities.User;
import entities.UserType;
import entities.requests.GetLiveStateRequest;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

/**
 * Controller class for displaying the waitlist and current orders.
 */
public class ShowWaitlistAndCurrentOrdersController implements IController {

    private User user;
    
    /** Property to track if user is logged in */
    private final BooleanProperty isLoggedIn = new SimpleBooleanProperty(false);

    @FXML private Button refreshBtn;
    @FXML private Button backBtn;
    @FXML private TextArea showWaitlist;
    @FXML private TextArea showCurrentOrders;

	/**
	 * Initializes the controller class.
	 */
    @FXML
    public void initialize() {
        ClientUI.console.setController(this);
        // Automatically load data when screen opens
        loadData();
    }

    /**
     * Reusable method to send the request to the server
     */
    private void loadData() {
        // Clear current text to show user something is happening (Optional)
        showWaitlist.setText("Loading...");
        showCurrentOrders.setText("Loading...");
        
        // Send request
        GetLiveStateRequest req = new GetLiveStateRequest();
        ClientUI.console.accept(req);
    }

	/**
	 * Handles the refresh button click event.
	 */
    @FXML
    void refreshClick(ActionEvent event) {
        loadData();
    }

	/**
	 * Handles the back button click event.
	 */
    @FXML
    void onBackClick(ActionEvent event) throws IOException {
        String path = "/boundry/fxml_files/WorkerScreen.fxml";
        ClientUI.console.switchScreen(this, event, path, user);
    }

	/**
	 * Sets the current user and updates login status.
	 * 
	 * @param user The user to set.
	 */
    @Override
    public void setUser(User user) {
        this.user = user;
        isLoggedIn.set(user != null && user.getType() != UserType.GUEST);
    }

	/**
	 * Updates the UI with the result from the server.
	 * 
	 * @param result The result object from the server, expected to be List<String>.
	 */
    @SuppressWarnings("unchecked")
	@Override
    public void setResultText(Object result) {
        // We expect a List<String> from the server now
        if (result instanceof List) {
            List<String> data = (List<String>) result;
            
            javafx.application.Platform.runLater(() -> {
                if (data.size() >= 2) {
                    showWaitlist.setText(data.get(0));      // The Waitlist String
                    showCurrentOrders.setText(data.get(1)); // The Seated String
                }
            });
        } else {
            System.out.println("Error: Expected List<String> but got " + result.getClass());
        }
    }
}