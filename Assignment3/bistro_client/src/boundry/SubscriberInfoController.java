package boundry;

import entities.User;
import entities.requests.GetAllSubscribersRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

/**
 * Controller class for managing subscriber information in the UI.
 */
public class SubscriberInfoController implements IController {
	private User user;
	
    @FXML
    private Button backBtn;

    @FXML
    private Button displaySubscribersButton;

    @FXML
    private TextArea resultTxt;

	/**
	 * Initializes the controller and sets up necessary configurations.
	 */
    @FXML
    void initialize() {
		ClientUI.console.setController(this);
	}

	/**
	 * Handles the action when the back button is clicked. Switches the screen back
	 * to the Worker Screen.
	 * 
	 * @param event The action event triggered by clicking the back button.
	 */
    @FXML
    void onBackBtnClick(ActionEvent event) {
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/WorkerScreen.fxml", user);
    }

    /**
     * 	Handles the action when the display subscribers button is clicked.
     * @param event
     */
    @FXML
    void onDisplaySubscribersClick(ActionEvent event) {
    	ClientUI.console.accept(new GetAllSubscribersRequest());
    }

    /**
     * Sets the result text area with the provided result.
     */
	@Override
	public void setResultText(Object result) {
		resultTxt.setText((String)result);
		
	}

	/**
	 * Sets the user for this controller.
	 */
	@Override
	public void setUser(User user) {
		this.user = user;		
	}

}
