package boundry;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import entities.Day;
import entities.SpecificDate;
import entities.User;
import entities.UserType;
import entities.requests.GetHoursDateRequest;
import entities.requests.GetHoursDayRequest;
import entities.requests.GetMaxTableRequest;
import entities.requests.ReserveRequest;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import entities.Subscriber;

/**
 * Controller for the Order Screen.
 */
public class OrderScreenController implements IController {

    private User user;
    private List<SpecificDate> allSpecificDates;
    private List<Day> allDays;
	
    @FXML private DatePicker orderDatePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private ComboBox<Integer> guestsComboBox;
    @FXML private TextArea resultTxt;

    /** Contact input for guest users */
    @FXML private HBox contactBox;
    @FXML private TextField contactTxt;

	/**
	 * Property indicating if the user is logged in. Used to show/hide contact input
	 * for guests.
	 */
    private final BooleanProperty isLoggedIn = new SimpleBooleanProperty(false);
    private Integer pendingGuests;
    private String pendingSubscriberIdStr;
    private String pendingContact;
    private String pendingOrderDateTime;
    private Integer maxTableCapacity;
    @FXML private Button orderBtn;
    @FXML private Button backBtn;

	/**
	 * Initializes the controller.
	 */
    @FXML
    public void initialize() throws InterruptedException {
        ClientUI.console.setController(this);

        contactBox.visibleProperty().bind(isLoggedIn.not());
        contactBox.managedProperty().bind(isLoggedIn.not());
        ClientUI.console.accept(new GetMaxTableRequest());
        Thread.sleep(200);
        for (int i = 1; i <= maxTableCapacity; i++) {
        	guestsComboBox.getItems().add(i);
        }

        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusMonths(1);

        orderDatePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date.isBefore(today) || date.isAfter(maxDate)) setDisable(true);
            }
        });

        orderDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> updateAvailableTimes(newDate));
        
        ClientUI.console.accept(new GetHoursDateRequest());
        ClientUI.console.accept(new GetHoursDayRequest());
        
    }
 
	/**
	 * Retrieves the opening and closing hours for a given date.
	 *
	 * @param date The date to check.
	 * @return A Pair containing opening and closing LocalTime, or null if not
	 *         found.
	 */
    private Pair<LocalTime, LocalTime> getHoursForDate(LocalDate date) {
        if (allSpecificDates != null) {
            for (SpecificDate d : allSpecificDates) {
                if (d.getDate().equals(date)) {
                    return new Pair<>(d.getOpen().toLocalTime(), d.getClose().toLocalTime());
                }
            }
        }

        if (allDays != null) {
            int dayOfWeek = date.getDayOfWeek().getValue();
            dayOfWeek = (dayOfWeek % 7) + 1;
            for (Day d : allDays) {
                if (d.getDay() == dayOfWeek) {
                    return new Pair<>(d.getOpen().toLocalTime(), d.getClose().toLocalTime());
                }
            }
        }
        return null;
    }

/**
 * Updates the available times in the timeComboBox based on the selected date.
 * @param selectedDate
 */
    private void updateAvailableTimes(LocalDate selectedDate) {
        timeComboBox.getItems().clear();
        if (selectedDate == null) return;

        Pair<LocalTime, LocalTime> hours = getHoursForDate(selectedDate);
        if (hours == null) return;

        LocalTime opening = hours.getKey();
        LocalTime closing = hours.getValue();
        LocalDateTime nowPlusHour = LocalDateTime.now().plusHours(1);

        for (LocalTime t = opening; t.isBefore(closing); t = t.plusMinutes(30)) {
            LocalDateTime candidate = LocalDateTime.of(selectedDate, t);
            if (selectedDate.equals(LocalDate.now()) && candidate.isBefore(nowPlusHour)) continue;
            timeComboBox.getItems().add(t.toString());
        }
    }

    /**
     * Handles the order button click event.
     * @param event
     */
    @FXML
    void OnOrderClick(ActionEvent event) {
        resultTxt.clear();

        try {
        	//*====================================== VALIDATIONS =============================*//
            LocalDate date = orderDatePicker.getValue();
            String time = timeComboBox.getValue();
            Integer guests = guestsComboBox.getValue();
            if (date == null || time == null || guests == null) throw new IllegalArgumentException();

            LocalTime chosen = LocalTime.parse(time);
            
            if (date.equals(LocalDate.now())) {
                if (LocalDateTime.of(date, chosen).isBefore(LocalDateTime.now().plusHours(1))) {
                    resultTxt.setText("❌ If ordering today, choose a time at least 1 hour from now.");
                    return;
                }
            }

            pendingGuests = guests;
            pendingOrderDateTime = date + " " + time + ":00";
            if (!isLoggedIn.get()) {
                String contact = (contactTxt.getText() == null) ? "" : contactTxt.getText().trim();
                if (contact.isEmpty() || !isValidPhoneOrEmail(contact)) {
                    resultTxt.setText("❌ Guest must enter a valid phone OR email.");
                    return;
                }
                pendingSubscriberIdStr = "0";
                pendingContact = contact;

                
                
                
            }else {
            	pendingSubscriberIdStr = String.valueOf(((Subscriber)user).getSubscriberID());
				pendingContact = ((Subscriber)user).getEmail();
            }
            
            ClientUI.console.accept(new ReserveRequest(pendingOrderDateTime, pendingGuests+"", pendingSubscriberIdStr, pendingContact));


        } catch (Exception e) {
            resultTxt.setText("❌ Invalid input.");
        }
    }

	/**
	 * Validates if the given string is a valid phone number or email address.
	 * 
	 * @param s The string to validate.
	 * @return true if valid, false otherwise.
	 */
    public static boolean isValidPhoneOrEmail(String s) {
        boolean looksEmail = s.contains("@") && s.contains(".") && s.indexOf('@') > 0;
        String digits = s.replaceAll("[^0-9]", "");
        boolean looksPhone = digits.length() >= 9 && digits.length() <= 15;
        return looksEmail || looksPhone;
    }

/**
 * Handles the back button click event. 
 * @param event
 * @throws IOException
 */
    @FXML
    void onBackClick(ActionEvent event) throws IOException {
    	if(user.getType() == UserType.GUEST) {
            ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/AppOrderManagementScreen.fxml", user);
    	}
    	else if(user.getType() == UserType.SUBSCRIBER) {
    		ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/AppOrderManagementScreen.fxml", user);
    	}
    	else {
    		ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/WorkerScreen.fxml", user);
    	}
    }
    
	/**
	 * Sets the current user.
	 * 
	 * @param user The user to set.
	 */
    @Override
    public void setUser(User user) {
        this.user = user;
        isLoggedIn.set(user != null && user.getType() != UserType.GUEST);
    }

	/**
	 * Sets the result text or updates internal data based on the result object.
	 * 
	 * @param result The result object which can be a List<Day>, List<SpecificDate>,
	 *               String message, or Integer max table capacity.
	 */
    public void setResultText(Object result) {
        if (result instanceof List<?> list) {
            if (!list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof Day) {
                    allDays = (List<Day>) list;
                } else if (first instanceof SpecificDate) {
                    allSpecificDates = (List<SpecificDate>) list;
                }
            }
            Platform.runLater(() -> updateAvailableTimes(orderDatePicker.getValue()));
        } else if (result instanceof String msg) {
            resultTxt.setText(msg);
        }
        else if (result instanceof Integer) {
        	this.maxTableCapacity = (int)result;
        }
    }

}
