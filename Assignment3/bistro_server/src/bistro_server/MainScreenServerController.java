package bistro_server;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import ocsf.server.ConnectionToClient;


/**
 * A controller for the server UI
 */
public class MainScreenServerController {
    @FXML
    private Button exitBtn;

    @FXML
    private Button showIP;

    @FXML
    private TextArea resultTxt;
    
    @FXML
    private TextArea resultTxt1;
    
    @FXML
    private Button seedDatabaseBtn;
    
    /** Singleton instance of the controller */
    private static MainScreenServerController instance;

	/**
	 * Initializes the controller after its root element has been completely processed.
	 */
    @FXML
    void initialize() {
        instance = this;
        refreshClientsText();
    }
    
    /** Updates the resultTxt1 TextArea with a new message */
    public void updateTxt(String msg) {
		if (instance == null) return;
		Platform.runLater(() -> {
			String currentText = instance.resultTxt1.getText();
			instance.resultTxt1.setText(currentText + "\n" + msg);
		});
	}
    
    /** Refreshes the clients list in the UI */
    public static void refreshClientsLive() {
        if (instance == null) return;
        Platform.runLater(() -> instance.refreshClientsText());
    }

    /** Updates the resultTxt TextArea with the list of connected clients */
    private void refreshClientsText() {
        if (BistroServer.clients == null || BistroServer.clients.isEmpty()) {
            resultTxt.setText("No Client Connected.");
            return;
        }

        StringBuilder clientString = new StringBuilder("Clients Connected: \n");
        for (int i = 0; i < BistroServer.clients.size(); i++) {
            ConnectionToClient client = BistroServer.clients.get(i);
            if (client != null) {
                clientString.append(i + 1).append(". ").append(client.toString()).append("\n");
            }
        }
        resultTxt.setText(clientString.toString());
    }
    
    @FXML
    void onSeedDatabaseClick(ActionEvent event) {
    	BistroServer.instance.seedDatabase();
    }
    /** Handles the exit button click event to close the application */
    @FXML
    void onExitClick(ActionEvent event) {
        System.exit(0);
    }

    /** Handles the show IP button click event to display the server IP address */
    public void start(Stage primaryStage) throws Exception {  // Method for starting the main screen
        // Load the main screen FXML into a Parent node
        Parent root = FXMLLoader.load(getClass().getResource("/bistro_server/serverui.fxml"));

        Scene scene = new Scene(root);                        // Create the scene with the loaded layout
        primaryStage.setTitle("Server UI"); // Set the window title
        primaryStage.setScene(scene);                         // Set the scene on the primary stage
        primaryStage.show();                                  // Display the window
    }
}
