package boundry;

import java.util.ArrayList;
import entities.Order;
import entities.User;
import entities.requests.GetAllActiveOrdersRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller class for the All Orders Screen. Allows a worker to view all
 * active orders in the system.
 */
public class AllOrdersScreenController implements IController {
    private User user;

    @FXML
    private Button backBtn;

    @FXML
    private Button displayOrdersBtn;

    // --- REPLACED TextArea with TableView ---
    @FXML
    private TableView<Order> ordersTable;

    @FXML
    private TableColumn<Order, Integer> orderIdColumn;

    @FXML
    private TableColumn<Order, String> confCodeColumn;

    @FXML
    private TableColumn<Order, Integer> numberOfGuestsColumn;

    @FXML
    private TableColumn<Order, String> dateColumn; // Reservation Date

    @FXML
    private TableColumn<Order, String> dateOfPlacingOrderColumn;



    /**
     * Initializes the controller and sets the console's controller to this instance.
     */
    @FXML
    void initialize() {
        ClientUI.console.setController(this);

        // Map columns to Order entity fields
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        confCodeColumn.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
        numberOfGuestsColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDateTime"));
        dateOfPlacingOrderColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfPlacingOrder"));
    }

    /**
     * Handles the action of clicking the back button.
     */
    @FXML
    void onBackClick(ActionEvent event) {
        ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/WorkerScreen.fxml", user);
    }

    /**
     * Handles the action of clicking the display orders button.
     */
    @FXML
    void onDisplayOrdersClick(ActionEvent event) {
        GetAllActiveOrdersRequest req = new GetAllActiveOrdersRequest();
        ClientUI.console.accept(req);
    }

    /**
     * Sets the table items with the provided result list.
     */
    @Override
    public void setResultText(Object result) {
        // Expecting ArrayList<Order> from the server
        if (result instanceof ArrayList) {
            ArrayList<Order> activeOrders = (ArrayList<Order>) result;
            ObservableList<Order> observableOrders = FXCollections.observableArrayList(activeOrders);
            ordersTable.setItems(observableOrders);
        } else {
            System.out.println("Error: Expected ArrayList<Order> but got " + (result != null ? result.getClass().getName() : "null"));
        }
    }

    @Override
    public void setUser(User user) {
        this.user = user;        
    }
}