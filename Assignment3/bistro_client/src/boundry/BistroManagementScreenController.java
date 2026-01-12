package boundry;

import java.time.LocalDate;
import java.util.ArrayList;

import entities.User;
import entities.requests.AddTableRequest;
import entities.requests.ChangeHoursDayRequest;
import entities.requests.GetAllTablesRequest;
import entities.requests.RemoveTableRequest;
import entities.requests.WriteHoursDateRequest;
import entities.Table;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.scene.control.Alert.AlertType;

/**
 * Controller class for the Bistro Management Screen. This class handles user
 * interactions and updates the UI accordingly.
 */
public class BistroManagementScreenController implements IController{
	private User user;
	@FXML private DatePicker datePicker;
	@FXML private ComboBox<Integer> dayOfWeek;
	@FXML private ComboBox<Integer> openHour;
	@FXML private ComboBox<Integer> closeHour;
	@FXML private Button confirm;
	@FXML private Button tablesBtn;
	@FXML private Button backBtn;
	@FXML private TextField AddTableCapText;
	@FXML private ComboBox<Table> currentTables;
	@FXML private CheckBox removeTableCheck;
    @FXML private TextField setTableCapText;

    /**
     * Initializes the controller class. This method is automatically called
     */
	@FXML
	public void initialize() {
		ClientUI.console.setController(this);
		for (int i = 1; i <= 7; i++) dayOfWeek.getItems().add(i);
		for (int i = 0; i <= 23; i++) openHour.getItems().add(i);
		for (int i = 0; i <= 23; i++) closeHour.getItems().add(i);
		Callback<ListView<Table>, ListCell<Table>> cellFactory = lv -> new ListCell<Table>() {
		    @Override
		    protected void updateItem(Table item, boolean empty) {
		        super.updateItem(item, empty);
		        if (empty || item == null) {
		            setText(null);
		            setTextFill(Color.BLACK);
		        } else {
		            if (item.getActiveTo() != null) {
		                setText("Table " + item.getId() + " (Scheduled for removal: " + item.getActiveTo() + ")");
		                setTextFill(Color.RED);
		            }else if(item.getActiveFrom().isAfter(LocalDate.now())) {
		                setText("Table " + item.getId() + " (Scheduled to start operation: " + item.getActiveFrom() + ")");
		                setTextFill(Color.BLUE);
		            } else {
		                // Normal table
		                setText("Table " + item.getId() + " (Capacity: " + item.getCapacity() + ")");
		                setTextFill(Color.BLACK);
		            }
		        }
		    }		    		    
		};
		currentTables.setCellFactory(cellFactory);
		currentTables.setButtonCell(cellFactory.call(null));
		ClientUI.console.accept(new GetAllTablesRequest());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // wait for response
		LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusMonths(1);

        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date.isBefore(today) || date.isAfter(maxDate)) setDisable(true);
            }
        });
	}

	/**
	 * Handles the confirm button click event. Validates input and sends appropriate
	 * requests to the server.
	 * 
	 * @param event The action event triggered by clicking the confirm button.
	 */
	@FXML
	void onConfirmClick(ActionEvent event) {
		ArrayList<String> args = new ArrayList<>();
    	boolean exceptionRaised = false;
    	boolean hoursException = false;
		Integer day;
		Integer open;
		Integer close;
    	try {
        	LocalDate date = datePicker.getValue();
    		day = dayOfWeek.getValue();
    		open = openHour.getValue();
    		close = closeHour.getValue();
    		
    		if((date != null && day != null) || (date == null && day == null)) {
    			exceptionRaised = true;
    		}
    		
    		else if(open == null || close == null) {
    			hoursException = true;
    		}
    		
    		
    		else if (date != null && day == null){
    			args.add(date.toString());
    			args.add(open.toString());
    			args.add(close.toString());
    			WriteHoursDateRequest r = new WriteHoursDateRequest(args.get(0),args.get(1),args.get(2));
    			ClientUI.console.accept(r);
    		}
    		
    		else if (date == null && day != null) {
    			args.add(day.toString());
    			args.add(open.toString());
    			args.add(close.toString());
    			ChangeHoursDayRequest r = new ChangeHoursDayRequest(args.get(0),args.get(1),args.get(2));
    			ClientUI.console.accept(r);
    		}
    	}catch (Exception e) {
    		exceptionRaised = true;
    	}
    		
    	if(exceptionRaised) {
			Alert alert = new Alert(AlertType.ERROR);
    		alert.setTitle("Error Occurred");
    		alert.setHeaderText("Input Validation Failed");
    		alert.setContentText("Please choose one from date and day of week");
    		alert.showAndWait();
    	}
    	
    	else if(hoursException) {
			Alert alert = new Alert(AlertType.ERROR);
    		alert.setTitle("Error Occurred");
    		alert.setHeaderText("Input Validation Failed");
    		alert.setContentText("Please choose both opening hour and closing hour");
    		alert.showAndWait();
    	}
	}

	/**
	 * Handles the tables button click event. Validates input and sends appropriate
	 * requests to the server.
	 * 
	 * @param event The action event triggered by clicking the tables button.
	 */
	@FXML
	void onTablesBtnClick(ActionEvent event) throws InterruptedException {
		try {
			
			if(removeTableCheck.isSelected()) {
				Table selectedTable = currentTables.getValue();
				if(selectedTable != null) {
					ClientUI.console.accept(new RemoveTableRequest(selectedTable.getId()));
					removeTableCheck.setSelected(false);
				} else {
					Alert alert = new Alert(AlertType.ERROR);
		    		alert.setTitle("Error Occurred");
		    		alert.setHeaderText("No Table Selected");
		    		alert.setContentText("Please select a table to remove");
		    		alert.showAndWait();
				}
			} 
			
			else if(!AddTableCapText.getText().isEmpty()) {
				int capacity = Integer.parseInt(AddTableCapText.getText());
				ClientUI.console.accept(new AddTableRequest(capacity));
				AddTableCapText.clear();
			} 
			
			else if(!setTableCapText.getText().isEmpty()) {
				Table selectedTable = currentTables.getValue();
				if(selectedTable != null) {
					int newCapacity = Integer.parseInt(setTableCapText.getText());
					ClientUI.console.accept(new entities.requests.UpdateTableCapacityRequest(selectedTable.getId(), newCapacity));
					setTableCapText.clear();
				} else {
					Alert alert = new Alert(AlertType.ERROR);
		    		alert.setTitle("Error Occurred");
		    		alert.setHeaderText("No Table Selected");
		    		alert.setContentText("Please select a table to update its capacity");
		    		alert.showAndWait();
				}
			}
			Thread.sleep(200);
			ClientUI.console.accept(new GetAllTablesRequest());
		} catch (NumberFormatException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error Occurred");
			alert.setHeaderText("Invalid Input");
			alert.setContentText("Please enter a valid number for table capacity");
			alert.showAndWait();
		}
	}

	/**
	 * Handles the back button click event. Navigates back to the worker screen.
	 * 
	 * @param event The action event triggered by clicking the back button.
	 */
	@FXML
	void onBackBtnClick(ActionEvent event) {
		ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/WorkerScreen.fxml", user);
	}

	/**
	 * Sets the result text in the UI based on the provided result object.
	 * 
	 * @param result The result object containing data to be displayed.
	 */
	@Override
	public void setResultText(Object result) {
		if( result instanceof ArrayList<?>) {
			
			@SuppressWarnings("unchecked")
			ArrayList<Table> tables = (ArrayList<Table>) result;
			currentTables.getItems().clear();
			for(Table t: tables) {
				currentTables.getItems().add(t);
			}
		}
		else {
			Platform.runLater(()-> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Operation Result");
			alert.setHeaderText(null);
			alert.setContentText((String)result);
			alert.showAndWait();
			});
		}
	}

	/**
	 * Sets the user for this controller.
	 * 
	 * @param user The user to be set.
	 */
	@Override
	public void setUser(User user) {
		this.user = user;	
	}	
}
