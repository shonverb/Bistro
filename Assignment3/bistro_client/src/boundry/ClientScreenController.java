package boundry;

import entities.User;
import entities.UserType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller class for the Client Screen. Manages user interactions and
 * navigation for client-related functionalities.
 */
public class ClientScreenController implements IController {
	private User user;
    @FXML
    private Button changeDetailsBtn;

    @FXML
    private Button logOutBtn;

    @FXML
    private Button orderHistoryBtn;

    @FXML
    private Button orderManagementBtn;
    
    @FXML
    private Button waitingListBtn;
    
    /** Property to track if a user is logged in */
    private final BooleanProperty isLoggedIn = new SimpleBooleanProperty(false);

	/**
	 * Initializes the controller. Binds button visibility to the logged-in state.
	 */
    @FXML
    void initialize() {
    	ClientUI.console.setController(this);
    	changeDetailsBtn.visibleProperty().bind(isLoggedIn);
    	changeDetailsBtn.managedProperty().bind(isLoggedIn);
    	orderHistoryBtn.visibleProperty().bind(isLoggedIn);
    	orderHistoryBtn.managedProperty().bind(isLoggedIn);
    }

	/**
	 * Handles the action when the "Change Details" button is clicked. Navigates to
	 * the Change Details screen.
	 *
	 * @param event The action event triggered by clicking the button.
	 */
    @FXML
    void onChangeDetailsClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/ChangeDetailsScreen.fxml", user);
    }

    /**
     * 	Handles the action when the "Log Out" button is clicked. Navigates to
     * @param event
     */
    @FXML
    void onLogOutBtnClick(ActionEvent event) {
		ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/loginScreen.fxml", null);

    }

	/**
	 * Handles the action when the "Order History" button is clicked. Navigates to
	 * the Order History screen.
	 *
	 * @param event The action event triggered by clicking the button.
	 */
    @FXML
    void onOrderHistoryClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/HistoryScreen.fxml", user);

    }

	/**
	 * Handles the action when the "Order Management" button is clicked. Navigates
	 * to the Order Management screen.
	 *
	 * @param event The action event triggered by clicking the button.
	 */
    @FXML
    void onOrderManagementClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/AppOrderManagementScreen.fxml", user);
    }

	/**
	 * Handles the action when the "Waiting List" button is clicked. Navigates to
	 * the Waiting List screen.
	 *
	 * @param event The action event triggered by clicking the button.
	 */
    @FXML
    void onWaitingListClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/queueWaitListScreen.fxml", user);
    }

    /**
     * Sets the result text. Not used in this controller.
     */
	@Override
	public void setResultText(Object result) {
		return;
	}

	/**
	 * Sets the current user and updates the logged-in state.
	 *
	 * @param user The user to set.
	 */
	@Override
	public void setUser(User user) {
		this.user = user;
        isLoggedIn.set(user != null && user.getType() != UserType.GUEST);
	}

}
