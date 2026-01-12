package boundry;

import entities.User;
import entities.UserType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller class for the Worker Screen. Manages user interactions and
 * navigation to other screens.
 */
public class WorkerScreenController implements IController {
	private User user;
	
    @FXML
    private Button bistroManagementBtn;

    @FXML
    private Button changeDetailsBtn;

    @FXML
    private Button historyBtn;

    @FXML
    private Button logOutBtn;

    @FXML
    private Button orderBtn;

    @FXML
    private Button orderDetailsBtn;

    @FXML
    private Button reportBtn;

    @FXML
    private Button subscriberDetailsBtn;

    @FXML
    private Button waitingListBtn;
    
    @FXML
    private Button registerBtn;
    
    /** Boolean property to determine if the user is a manager */
    private BooleanProperty isManager = new SimpleBooleanProperty(false);
    
	/**
	 * Initializes the controller. Sets up bindings and initial states.
	 */
    @FXML
	void initialize() {
    	ClientUI.console.setController(this);
    	reportBtn.visibleProperty().bind(isManager);
		reportBtn.managedProperty().bind(isManager);
    }

	/**
	 * Handles the click event for the Bistro Management button. Navigates to the
	 * Bistro Management screen.
	 * 
	 * @param event The action event triggered by the button click.
	 */
    @FXML
    void onBistroManagementClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/BistroManagementScreen.fxml", user);
    }

	/**
	 * Handles the click event for the Change Details button. Navigates to the
	 * Change Details screen.
	 * 
	 * @param event The action event triggered by the button click.
	 */
    @FXML
    void onChangeDetailsClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/ChangeDetailsScreen.fxml", user);
    }

    @FXML
    void onHistoryBtnClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/HistoryScreen.fxml", user);
    }

	/**
	 * Handles the click event for the Log Out button. Navigates back to the login
	 * screen.
	 * 
	 * @param event The action event triggered by the button click.
	 */
    @FXML
    void onLogOutClick(ActionEvent event) {
		ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/loginScreen.fxml", null);
    }

	/**
	 * Handles the click event for the New Order button. Navigates to the Order
	 * screen.
	 * 
	 * @param event The action event triggered by the button click.
	 */
    @FXML
    void onNewOrderBtn(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/OrderScreen.fxml", user);
    }

	/**
	 * Handles the click event for the Order Details button. Navigates to the All
	 * Orders screen.
	 * 
	 * @param event The action event triggered by the button click.
	 */
    @FXML
    void onOrderDetailsClick(ActionEvent event) {
		ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/AllOrdersScreen.fxml", user);
    }

	/**
	 * Handles the click event for the Report button. Navigates to the Reports
	 * screen if the user is a manager.
	 * 
	 * @param event The action event triggered by the button click.
	 */
    @FXML
    void onReportClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/ReportsScreen.fxml", user);
    }

	/**
	 * Handles the click event for the Subscriber Details button. Navigates to the
	 * Subscriber Info screen.
	 * 
	 * @param event The action event triggered by the button click.
	 */
    @FXML
    void onSubscriberDetailsClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/SubscriberInfoScreen.fxml", user);
    }

	/**
	 * when the user clicks on 'Waiting List'
	 * 
	 * @param event
	 */
    @FXML
    void onWaitingListClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/CurrentStateScreen.fxml", user);
    }
    
    /**
     * when the user clicks on 'Register'
     * @param event
     */
    @FXML
    void onRegisterClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/registerScreen.fxml",user);
    }

    /**
     * Sets the result text. Not used in this controller.
     */
	@Override
	public void setResultText(Object result) {
		return;
		
	}

	/**
	 * Sets the current user and updates the manager status.
	 */
	@Override
	public void setUser(User user) {
		this.user = user;
		isManager.set(user.getType()==UserType.MANAGER);
	}

}
