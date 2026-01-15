package boundry;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller class for the Terminal Screen. Handles user interactions on the
 * terminal screen.
 */
public class TerminalScreenController implements IController {
    private User user;
    
    @FXML private Button haveOrderBtn;
    @FXML private Button dontHaveOrderBtn;
    @FXML private Button logOutBtn;

	/**
	 * Initializes the controller class. This method is automatically called after
	 * the FXML file has been loaded.
	 */
    @FXML
    public void initialize() {
        ClientUI.console.setController(this);
    }

	/**
	 * Sets the result text. This method is part of the IController interface but is
	 * not used in this controller.
	 *
	 * @param result The result object (not used).
	 */
    @Override
    public void setResultText(Object result) {
        return;
    }

	/**
	 * Sets the user for this controller.
	 *
	 * @param user The user to set.
	 */
    @Override
    public void setUser(User user) {
        this.user = user;
    }

	/**
	 * Handles the event when the "Have Order" button is clicked. Switches to the
	 * terminal order management screen.
	 *
	 * @param event The action event triggered by the button click.
	 */
    @FXML
    void onHaveOrderClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/terminalOrderManagementScreen.fxml",user);
    }

    /**
     * 	Handles the event when the "Don't Have Order" button is clicked. Switches to
     * @param event
     */
    @FXML
    void onDontHaveOrderClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/ImmidiateArrival.fxml",user);
    }
 
	/**
	 * Handles the event when the "Logout" button is clicked. Switches to the login
	 * screen.
	 *
	 * @param event The action event triggered by the button click.
	 */
    @FXML
    void OnLogoutClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/loginScreen.fxml",null);
    }    
}