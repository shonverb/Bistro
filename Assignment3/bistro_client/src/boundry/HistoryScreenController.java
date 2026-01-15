package boundry;

import java.util.ArrayList;
import entities.Order; // Make sure this import exists
import entities.Subscriber;
import entities.User;
import entities.UserType;
import entities.requests.OrderHistoryRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller class for the All Orders Screen.
 */
public class HistoryScreenController implements IController {
    private Subscriber user;

    @FXML
    private Button backBtn;

    @FXML
    private Button orderHistoryBtn;

    @FXML
    private TableView<Order> orderTable;

    @FXML
    private TableColumn<Order, Integer> orderIdColumn;

    @FXML
    private TableColumn<Order, String> numberOfGuestsColumn;

    @FXML
    private TableColumn<Order, String> dateColumn;

    @FXML
    private TableColumn<Order, String> confCodeColumn;

    @FXML
    private TableColumn<Order, String> statusColumn;

    @FXML
    private TableColumn<Order, String> dateOfPlacingOrderColumn;

    /**
     * Initializes the controller class.
     */
    @FXML
    void initialize() {
        ClientUI.console.setController(this);

        // Map columns to Order entity fields
        // Ensure entities.Order has getOrderId(), getRestaurant(), etc.
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        numberOfGuestsColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDateTime"));
        dateOfPlacingOrderColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfPlacingOrder"));
        confCodeColumn.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
    }

    /**
     * Handles the back button click event.
     */
    @FXML
    void onBackClick(ActionEvent event) {
        if (user.getType() == UserType.SUBSCRIBER) {
            ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/ClientScreen.fxml", user);
        } else {
            ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/WorkerScreen.fxml", user);
        }
    }

    /**
     * Handles the order history button click event.
     */
    @FXML
    void onOrderHistoryClick(ActionEvent event) {
        OrderHistoryRequest req = new OrderHistoryRequest(user.getSubscriberID() + "");
        ClientUI.console.accept(req);
    }

    /**
     * Sets the result list in the table view.
     */
    @Override
    public void setResultText(Object result) {
        if (result instanceof ArrayList) {
            ArrayList<Order> orderList = (ArrayList<Order>) result;
            ObservableList<Order> observableOrders = FXCollections.observableArrayList(orderList);
            orderTable.setItems(observableOrders);
        } else {
            System.out.println("Error: Expected ArrayList<Order> but got " + (result != null ? result.getClass().getName() : "null"));
        }
    }

    @Override
    public void setUser(User user) {
        this.user = (Subscriber) user;
    }
}