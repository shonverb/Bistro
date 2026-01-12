package boundry;

import java.io.IOException;

import entities.Guest;
import entities.Manager;
import entities.Subscriber;
import entities.User;
import entities.UserType;
import entities.Worker;
import entities.requests.LoginRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
/**
 * A controller responsible for the login screen
 */
public class LoginScreenController implements IController{
	/** the string that will hold the response from the server*/
	private String serverResponse;
	/** the user using the screen*/
	private User user;
	/** setting the console's controller to this*/
	@FXML
	public void initialize() {
		ClientUI.console.setController(this);
	}
    @FXML
    private Button guestTerminalBtn;
    
    @FXML
    private Button guestAppBtn;

    @FXML
    private Button registerBtn;

    @FXML
    private TextField subscriberId;

    @FXML
    private TextField userName;
    
    @FXML
    private Button exitBtn;
    
    @FXML
    private Button terminalBtn;
    
    @FXML
    private Button appBtn;
    
    /**
     * when the user clicks 'enter terminal as guest'
     * @param event
     * @throws IOException
     */
    @FXML
    void onGuestTerminalClick(ActionEvent event) throws IOException {
    	this.user = new Guest(null, null,null);
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/TerminalScreen.fxml", user);
    }
    
    /**
     * when the user clicks 'enter terminal as guest'
     * @param event
     * @throws IOException
     */
    @FXML
    void onGuestAppClick(ActionEvent event) throws IOException {
    	this.user = new Guest(null, null,null);
    	ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/ClientScreen.fxml", user);
    }
    
    /**
     * when the user clicks 'terminal'
     * @param event
     * @throws IOException
     * @throws InterruptedException
     */   
    @FXML
    void onTerminalClick(ActionEvent event) throws IOException, InterruptedException {		
    	login("TERMINAL", event);
    }
    
    /**
     * when the user clicks 'App'
     * @param event
     * @throws IOException
     * @throws InterruptedException
     */ 
    @FXML
    void onAppClick(ActionEvent event) throws IOException, InterruptedException {
    	login("APP", event);
    }
 
	/**
	 * method for logging in the user
	 * 
	 * @param mode
	 * @param event
	 * @throws IOException
	 * @throws InterruptedException
	 */
    public void login(String mode, ActionEvent event) throws IOException, InterruptedException {
        int id = 0;
        boolean exceptionRaised = false;

        try {
            id = Integer.parseInt(subscriberId.getText().trim());
            exceptionRaised = id <= 0;
        } catch (NumberFormatException e) {
            exceptionRaised = true;
        }

        if (!exceptionRaised) {
            LoginRequest r = new LoginRequest(id);
            ClientUI.console.accept(r);
            Thread.sleep(200); // wait for server response

            if (!serverResponse.equals("Not found")) {
                String[] args = serverResponse.split(",");
                String[] fullName = args[0].split(" ");
                String fname = fullName[0];
                String lname = fullName[1];
                String status = args[5];

                if ("CLIENT".equals(status)) {
                    user = new Subscriber(Integer.parseInt(args[1]), args[2], fname, lname, args[3], args[4],args[5], null);
                }
                else if ("EMPLOYEE".equals(status)) {
                    user = new Worker(Integer.parseInt(args[1]), args[2], fname, lname, args[3], args[4],args[5], null);
                }
                else {
                    user = new Manager(Integer.parseInt(args[1]), args[2], fname, lname, args[3], args[4],args[5], null);
                }

                String screen;
                if (mode.equals("TERMINAL")) {
                    screen = "/boundry/fxml_files/TerminalScreen.fxml";
                }
                else {
                    if (user.getType() == UserType.BISTRO_REP || user.getType() == UserType.MANAGER) {
                        screen = "/boundry/fxml_files/WorkerScreen.fxml";
                    } else {
                        screen = "/boundry/fxml_files/ClientScreen.fxml";
                    }
                }

                ClientUI.console.switchScreen(this, event, screen, user);
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Occurred");
                alert.setHeaderText("Input Validation Failed");
                alert.setContentText("That user doesn't exist, please check your credentials");
                alert.showAndWait();
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Occurred");
            alert.setHeaderText("Input Validation Failed");
            alert.setContentText("an id must be a positive integer");
            alert.showAndWait();
        }
    }
    
    /**
     * setting the server response
     */
	@Override
	public void setResultText(Object result) {
		serverResponse = (String)result;	
	}

	/**
	 * when the user clicks 'register'
	 * 
	 * @param event
	 * @throws IOException
	 */
    @FXML
    void onExitClick(ActionEvent event) {
    	ClientUI.console.quit();
    	System.exit(0);
    }

	/**
	 * when the user clicks 'register'
	 * 
	 * @param event
	 * @throws IOException
	 */
    public void start(Stage primaryStage) throws Exception {  // Method for starting the main screen
        // Load the main screen FXML into a Parent node
        Parent root = FXMLLoader.load(getClass().getResource("/boundry/fxml_files/loginScreen.fxml"));

        Scene scene = new Scene(root);                        // Create the scene with the loaded layout
        primaryStage.setTitle("Bistro Order management tool"); // Set the window title
        primaryStage.setScene(scene);                         // Set the scene on the primary stage
        primaryStage.show();                                  // Display the window
    }

	/**
	 * getting the user
	 * 
	 * @return user
	 */
    public void setUser(User user) {
    	this.user = user;
    }
}
