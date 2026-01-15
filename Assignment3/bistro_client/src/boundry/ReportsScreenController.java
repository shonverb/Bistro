package boundry;

import java.util.Locale;
import java.util.Map;

import entities.User;
import entities.requests.GetReportsRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
/**
 * Controller for the Reports Screen. Displays activity and lateness charts
 * based on data retrieved from the server.
 */
public class ReportsScreenController implements IController {
    private User user;

    @FXML private BarChart<String, Number> activityChart; 
    @FXML private BarChart<String, Number> latenessChart;
    @FXML private StackedBarChart<String, Number> inAdvanceVsOnTheSpotChart;
    @FXML private LineChart<String, Number> averageWaitingChart;
    
    /** X-Axes for both charts to set categories (hours) */
    @FXML private CategoryAxis activityXAxis;
    @FXML private CategoryAxis latenessXAxis;
    @FXML private CategoryAxis inAdvanceVsOnTheSpotXAxis;
    @FXML private CategoryAxis averageWaitingXAxis;
    
    @FXML private Button backBtn;
    
    
    @FXML private ComboBox<String> months;

    private int daysOfMonth;
	/**
	 * Initializes the controller. Sets up the charts and requests report data.
	 */
    @FXML
    public void initialize() {
        ClientUI.console.setController(this);
        setupAxes();
        
        // 1. Populate ComboBox with past 12 months (excluding current)
        months.getItems().clear();
        YearMonth current = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

        for (int i = 1; i <= 12; i++) {
            YearMonth pastMonth = current.minusMonths(i);
            months.getItems().add(pastMonth.format(formatter));
        }
        
        // 2. Select the first item (Last Month) by default
        if (!months.getItems().isEmpty()) {
            months.getSelectionModel().select(0);
        }

        // 3. Add Listener to handle selection changes
        months.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                requestReportForDate(newVal);
            }
        });

        // 4. Initial Request (for the default selected month)
        requestReportForDate(months.getSelectionModel().getSelectedItem());
    }

    /** Sets up the X-Axes with hour categories from 08:00 to 23:00 */
    private void setupAxes() {
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 8; i <= 23; i++) {
            hours.add(String.format("%02d:00", i));
        }
        // Force the X-Axis to use these categories so they always appear
        activityXAxis.setCategories(hours);
        latenessXAxis.setCategories(hours);
        averageWaitingXAxis.setCategories(hours);
        
        ObservableList<String> days = FXCollections.observableArrayList();
        for (int i = 1; i <= 31; i++) {
            days.add(String.valueOf(i));
        }
        
        inAdvanceVsOnTheSpotXAxis.setCategories(days);
    }


	/**
	 * Handles the result from the server and populates the charts. Expects a Map
	 * with keys: "Arrivals", "Departures", "AvgCustomerLate", "AvgRestaurantDelay"
	 * @param result The result object from the server
	 */
    @Override
    public void setResultText(Object result) {
        if (result instanceof Map) {
            Map<String, Map<Integer, Double>> data = (Map<String, Map<Integer, Double>>) result;
            
            Platform.runLater(() -> {
                populateActivityChart(data.get("Arrivals"), data.get("Departures"));
                populateLatenessChart(data.get("AvgCustomerLate"), data.get("AvgRestaurantDelay"));
                populateInAdvanceVsOnTheSpotChart(data.get("InAdvance"),data.get("OnTheSpot"));
                populateAverageWaitingChart(data.get("AvgWaiting"));
            });
        }
    }
    
    /** Populates the Order Type chart with Order type data across the month
     * @param inAdvanceMap Map of day to number of orders in advance
     * @param onSpotMap Map of day to number of orders on the spot
     *  */
    private void populateInAdvanceVsOnTheSpotChart(Map<Integer, Double> inAdvanceMap, Map<Integer, Double> onSpotMap) {
		inAdvanceVsOnTheSpotChart.getData().clear();
		XYChart.Series<String, Number> seriesAdvance = new XYChart.Series<>();
	    seriesAdvance.setName("In Advance"); // Legend label

	    XYChart.Series<String, Number> seriesSpot = new XYChart.Series<>();
	    seriesSpot.setName("On The Spot");   // Legend label
 
	    for (int i = 1; i <= daysOfMonth; i++) {
	        String dayLabel = String.valueOf(i);
	        
	        
	        // Add count for "In Advance" for day i
	        seriesAdvance.getData().add(new XYChart.Data<>(dayLabel, inAdvanceMap.getOrDefault(i, 0.0)));
	        
	        // Add count for "On The Spot" for day i
	        seriesSpot.getData().add(new XYChart.Data<>(dayLabel, onSpotMap.getOrDefault(i, 0.0)));
	    }

	    inAdvanceVsOnTheSpotChart.getData().addAll(seriesAdvance, seriesSpot);
	}

    /** Populates the activity chart with arrivals and departures data
     * @param arrivals Map of hour to number of arrivals
     * @param departures Map of hour to number of departures
     *  */
    private void populateActivityChart(Map<Integer, Double> arrivals, Map<Integer, Double> departures) {
        activityChart.getData().clear();
        
        XYChart.Series<String, Number> seriesArr = new XYChart.Series<>();
        seriesArr.setName("Arrived");
        
        XYChart.Series<String, Number> seriesDep = new XYChart.Series<>();
        seriesDep.setName("Left");

        for (int i = 8; i <= 23; i++) { 
            String hourLabel = String.format("%02d:00", i);
            seriesArr.getData().add(new XYChart.Data<>(hourLabel, arrivals.getOrDefault(i, 0.0)));
            seriesDep.getData().add(new XYChart.Data<>(hourLabel, departures.getOrDefault(i, 0.0)));
        }
        
        activityChart.getData().addAll(seriesArr, seriesDep);
    }

	/**
	 * Populates the lateness chart with customer lateness and restaurant delay data 
	 * @param customerLate    Map of hour to average customer lateness
	 * @param restaurantDelay Map of hour to average restaurant delay
	 */
    private void populateLatenessChart(Map<Integer, Double> customerLate, Map<Integer, Double> restaurantDelay) {
		latenessChart.getData().clear();
		latenessChart.setTitle("Lateness vs Delay (Minutes)");

		XYChart.Series<String, Number> seriesCust = new XYChart.Series<>();
		seriesCust.setName("Avg Customer Late"); // Late arrival

		XYChart.Series<String, Number> seriesRest = new XYChart.Series<>();
		seriesRest.setName("Avg Restaurant Delay"); // Waiting for table

		for (int i = 8; i <= 23; i++) {
			String hourLabel = String.format("%02d:00", i);
            // Use getOrDefault to prevent NullPointer if data is missing for an hour
			seriesCust.getData().add(new XYChart.Data<>(hourLabel, customerLate.getOrDefault(i, 0.0)));
			seriesRest.getData().add(new XYChart.Data<>(hourLabel, restaurantDelay.getOrDefault(i, 0.0)));
		}

		latenessChart.getData().addAll(seriesCust, seriesRest);
	}
    
    /**
     * Populates the Line Chart with average waiting data.
     * @param avgWaitingMap Map of hour to average customers
     */
    private void populateAverageWaitingChart(Map<Integer, Double> avgWaitingMap) {
        averageWaitingChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Avg Waiting Customers");
        
        // We loop from 8 to 23 (Operating Hours)
        for (int h = 8; h <= 23; h++) {
            String hourLabel = String.format("%02d:00", h);
            
            // Get the value from the map, default to 0.0 if empty
            Double val = avgWaitingMap.getOrDefault(h, 0.0);
            
            series.getData().add(new XYChart.Data<>(hourLabel, val));
        }
        
        averageWaitingChart.getData().add(series);    
    }
    // Helper method to parse string and send request
    private void requestReportForDate(String dateString) {
        if (dateString == null) return;
        
        try {
            YearMonth ym = YearMonth.parse(dateString, DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
            daysOfMonth = ym.lengthOfMonth();
            ClientUI.console.accept(new GetReportsRequest(ym.getMonthValue(), ym.getYear()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Handles the Back button click event to return to the Worker Screen
     * @param event The action event triggered by clicking the button
     */
    @FXML
    void onBackBtnClick(ActionEvent event) {
        ClientUI.console.switchScreen(this, event, "/boundry/fxml_files/WorkerScreen.fxml", user);
    }
    

	/**
	 * Sets the current user for the controller
	 * 
	 * @param user The user to set
	 */
    @Override
    public void setUser(User user) {
        this.user = user;
    }
}