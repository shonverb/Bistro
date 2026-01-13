package boundry;

import java.util.ArrayList;
import entities.User;
import entities.requests.GetAllSubscribersRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller class for managing subscriber information in the UI.
 */
public class SubscriberInfoController implements IController {
    private User user;

    @FXML
    private Button backBtn;

    @FXML
    private Button displaySubscribersButton;

    // --- REPLACED TextArea with TableView Components ---
    @FXML
    private TableView<User> subscriberTable;

    @FXML
    private TableColumn<User, String> idColumn;

    @FXML
    private TableColumn<User, String> nameColumn;
    
    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> phoneColumn;

    @FXML
    private TableColumn<User, String> emailColumn;
    // --------------------------------------------------

    /**
     * Initializes the controller and sets up necessary configurations.
     */
    @FXML
    void initialize() {
        ClientUI.console.setController(this);
        
        idColumn.setCellValueFactory(new PropertyValueFactory<>("SubscriberID"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone")); 
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    /**
     * Handles the action when the back button is clicked. 
     */
    @FXML
    void onBackBtnClick(ActionEvent event) {
        ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/WorkerScreen.fxml", user);
    }

    /**
     * Handles the action when the display subscribers button is clicked.
     */
    @FXML
    void onDisplaySubscribersClick(ActionEvent event) {
        ClientUI.console.accept(new GetAllSubscribersRequest());
    }

    /**
     * Sets the table items with the provided result list.
     */
    @Override
    public void setResultText(Object result) {
        if (result instanceof ArrayList) {
            ArrayList<User> subscribersList = (ArrayList<User>) result;
            
            ObservableList<User> observableData = FXCollections.observableArrayList(subscribersList);
            
            subscriberTable.setItems(observableData);
        } else {
            System.out.println("Error: Expected ArrayList<User> but got " + result.getClass().getName());
        }
    }

    @Override
    public void setUser(User user) {
        this.user = user;        
    }
}