package bistro_server;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import entities.Order;
import entities.Table;
import entities.requests.AddTableRequest;
import entities.requests.AlterWaitlistRequest;
import entities.requests.GetTableRequest;
import entities.requests.JoinWaitlistRequest;
import entities.requests.LeaveTableRequest;
import entities.requests.RemoveTableRequest;
import entities.requests.Request;
import entities.requests.RequestHandler;
import entities.requests.RequestType;
import entities.requests.ReserveRequest;
import entities.requests.ShowTakenSlotsRequest;
import entities.requests.UpdateTableCapacityRequest;
import entities.requests.WriteRequest;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
/**The server, extending the abstract server*/
public class BistroServer extends AbstractServer {
     final public static int DEFAULT_PORT = 5556;
     final public static int BILL = 100;
     protected static WaitingList waitlistJustArrived = new WaitingList();

     protected static SortedWaitingList waitlistOrderedInAdvance = new SortedWaitingList();

     /**An array that holds the currently connected clients*/
     protected static List<ConnectionToClient> clients;
     /**An array that holds the tables in the restaurant*/
     private static List<Table> tables;
     /**A map that holds the request handlers for each request type*/
    private HashMap<RequestType,RequestHandler> handlers;

    private HashMap<Table,Order> currentBistro;
    public static LocalDateTime dateTime = LocalDateTime.of(LocalDate.of(2026, 1, 21), LocalTime.of(12, 00));
    
    public static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
     /**A connection to the database*/
     DBconnector dbcon;
     
    /**
     * 
     * @param port the port to connect to
     */
    public BistroServer(int port) {
        super(port);
        currentBistro = new HashMap<>();
        dbcon = new DBconnector();
        clients = Collections.synchronizedList(new ArrayList<>());
        tables = dbcon.getRelevantTables();
        for (Table t : tables) {
			currentBistro.put(new Table(t.getId(), t.getCapacity(), t.isTaken()), null);
		}
        tables = dbcon.getAllTables();
        handlers = new HashMap<>();
        handlers.put(RequestType.WRITE_ORDER, this::addNewOrder);
        handlers.put(RequestType.READ_ORDER, dbcon::getOrder);
        handlers.put(RequestType.LOGIN_REQUEST, dbcon::checkLogin);
        handlers.put(RequestType.REGISTER_REQUEST, dbcon::addNewUser);
        handlers.put(RequestType.CANCEL_REQUEST, dbcon::cancelOrder);
        handlers.put(RequestType.RESERVE_TABLE, this::reserveTableInAdvance);
        handlers.put(RequestType.JOIN_WAITLIST, this::handleJoinWaitlist);
        handlers.put(RequestType.LEAVE_WAITLIST, this::handleLeaveWaitlist);
        handlers.put(RequestType.SPOT_WAITLIST, this::handleSpotWaitlist);
        handlers.put(RequestType.UPDATE_DETAILS, dbcon::updateDetails);
        handlers.put(RequestType.ORDER_HISTORY,dbcon::getOrderHistory);
        handlers.put(RequestType.CHECK_CONFCODE, dbcon::checkConfCode);
        handlers.put(RequestType.GET_ALL_ACTIVE_ORDERS, dbcon::getAllActiveOrders);
        handlers.put(RequestType.GET_ALL_SUBSCRIBERS, dbcon::getAllSubscribers);
        handlers.put(RequestType.GET_TABLE, this::getTableForOrder);
        handlers.put(RequestType.LEAVE_TABLE,this::leaveTable);
        handlers.put(RequestType.CHANGE_HOURS_DAY, dbcon::changeHoursDay);
        handlers.put(RequestType.WRITE_HOURS_DATE, dbcon::writeHoursDate);
        handlers.put(RequestType.GET_ALL_TABLES, this::getTables);
        handlers.put(RequestType.ADD_TABLE, this::addTable);
        handlers.put(RequestType.REMOVE_TABLE, this::removeTable);
        handlers.put(RequestType.UPDATE_TABLE_CAPACITY, this::updateTable);
        handlers.put(RequestType.GET_LIVE_BISTRO_STATE, this::getLiveState);
        handlers.put(RequestType.GET_REPORTS, dbcon::getReportsData);

        handlers.put(RequestType.GET_HOURS_DATE, dbcon::getAllDatesHours);
        handlers.put(RequestType.GET_HOURS_DAY, dbcon::getAllDaysHours);
        handlers.put(RequestType.GET_MAX_TABLE, this::getMaxTable);
        
        ConnectionPool.getInstance();
    }
    
    /**
     * Sending messages from client over to the database connector
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        Request r = (Request) msg;
        RequestHandler handler = handlers.get(r.getType());
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("Handling request of type: " + r.getType());
        Object result = handler.handle(r);
        System.out.println("Request of type " + r.getType() + " handled with result: " + result.toString());
        System.out.println("------------------------------------------------------------------------------");
        try {
            client.sendToClient(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**Adding a client to the array*/
    @Override
    protected void clientConnected(ConnectionToClient client) {
        clients.add(client);
        MainScreenServerController.refreshClientsLive();
    }
    
    /**
     * Removing a client from the array
     */
    @Override
    protected void clientDisconnected(ConnectionToClient client) {
    	System.out.println("ClientDisconnected Called");
        clients.remove(client);
        MainScreenServerController.refreshClientsLive();
    }
    
    
    /**
     * Removing a client from the array
     */
    @Override
    protected void clientException(ConnectionToClient client, Throwable exception) {
        clients.remove(client);
        MainScreenServerController.refreshClientsLive();
    }

    /** * Handles a request to get the live state of the bistro
	 * @param r the GetLiveStateRequest
	 * @return a List<String> containing the waitlist and seated orders strings
	 */
    public List<String> getLiveState(Request r) {
        // Build Waitlist String
        StringBuilder waitlistSb = new StringBuilder();
        waitlistSb.append("--- Just Arrived ---\n");
        waitlistSb.append(waitlistJustArrived.toString()).append("\n"); 
        waitlistSb.append("\n--- Ordered In Advance ---\n");
        waitlistSb.append(waitlistOrderedInAdvance.toString()).append("\n");

        // Build Seated String from the HashMap
        StringBuilder seatedSb = new StringBuilder();
        for (Map.Entry<Table, Order> entry : currentBistro.entrySet()) {
            Table t = entry.getKey();
            Order o = entry.getValue();
            
            if (t.isTaken() && o != null) {
                seatedSb.append("Table ").append(t.getId())
                        .append(" (Max ").append(t.getCapacity()).append("): ")
                        .append("Order #").append(o.getOrderNumber())
                        .append("\n");
            }
        }
        
        // Return both strings in a list
        List<String> result = new ArrayList<>();
        result.add(waitlistSb.toString());
        result.add(seatedSb.toString());
        return result;
    }
 
    /**
     * Handles a request to get all tables
     * @param r
     * @return a list of all tables
     */
    public List<Table> getTables(Request r) {
    	tables = dbcon.getAllTables();
    	return tables;
    }

 	/**
 	 * Checks availability of tables for the given guests in time
 	 * @param tables list of tables to check
 	 * @param guests_in_time
 	 * @param orderIdentifier
 	 * @return the table ID if available, -1 if not enough tables, 0 if others can be seated
 	 */
    public synchronized int checkAvailability(List<Table> tables, Map<String,Integer> guests_in_time,String orderIdentifier) {
		Table resultTable = null;
		if (guests_in_time.size()>tables.size()) {
			return -1;
		}
		for(Entry<String, Integer> entry : guests_in_time.entrySet()) {
			boolean seated = false;
			for (Table t : tables) {
				System.out.println("Checking table ID: " + t.getId() + " with capacity: " + t.getCapacity() + " (Taken: " + t.isTaken() + ") for party size: " + entry.getValue());
				if (!t.isTaken() && t.getCapacity()>=entry.getValue()) {
					t.setTaken(true);
					if (orderIdentifier.equals(entry.getKey())) {
						resultTable = t;
					}
					seated = true;
					break;
				}
			}
			if (!seated) {
				return -1;
			}
		}
    	return (resultTable !=null) ? resultTable.getId() : 0;
    }
    
    /** * Handles a customer leaving the waitlist
	 * Searches by order number
	 * @param r the LeaveWaitlistRequest containing the order number
	 * @return a message indicating success or failure
	 */
    public String handleLeaveWaitlist(Request r) {
        String confCode = ((AlterWaitlistRequest)r).getConfCode();
        // Accessing the static waitlist instance in BistroServer to remove the node
        String ordernum = BistroServer.waitlistJustArrived.cancel(confCode); 
        
        if (ordernum != "not found") {
        	dbcon.changeStatus("CANCELLED",ordernum);
            return confCode+ " have been removed from the waiting list.";
        } else {
            return "Could not find an order with this confiramtion code in the waiting list.";
        }
    }
    
    
    /** * Handles a walk-in joining the waitlist at the terminal.
     * @param r the JoinWaitlistRequest containing the order details
     * @return a message indicating success, prompt for confirmation, or failure
     */   
    public String handleJoinWaitlist(Request r) {
        JoinWaitlistRequest req = (JoinWaitlistRequest) r;
        int guests = Integer.parseInt(req.getNumberOfGuests());
        ShowTakenSlotsRequest slotReq = new ShowTakenSlotsRequest(guests, req.getOrderDateTime());
        System.out.println("current Bistro in join waitlist before check: "+currentBistro.toString());
        Map<String,Integer> guestList = prepareGuestsInTimeList(slotReq, false);
        for (Order o : currentBistro.values()) {
        	if (o != null) {
        		guestList.remove(o.getConfirmationCode());
        	}
        }
          Table desiredTable = null;
          int tableId = trySeatNow(guestList,"-1",guests); 
          Order waitlistOrder;
          
          if (tableId != -1) { 
        	waitlistOrder = addNewOrder(new WriteRequest(
        			req.getOrderDateTime(),
        			req.getNumberOfGuests(),
        			req.getSubscriberId(),
        			req.getContact()
        		));
        	for (Table t : currentBistro.keySet()) {
        		if (t.getId() == tableId) {
					desiredTable = t;
					break;
				}
        	}
        	waitlistOrder.setSittingtime(BistroServer.dateTime);
        	currentBistro.put(desiredTable, waitlistOrder); // Seat at the first available table

        	desiredTable.setTaken(true);
        	dbcon.markArrivalAtTerminal(waitlistOrder.getOrderNumber());
			dbcon.markOrderAsSeated(waitlistOrder.getOrderNumber());
			dbcon.setOrderType(waitlistOrder.getOrderNumber(),"ON_THE_SPOT");
            return "SUCCESS: Table is ready! Please proceed to your table.\n"
            		+ "Your confirmation code: " + (waitlistOrder.getConfirmationCode())+"\n";
        } 

        // 2. If no seats and user hasn't confirmed via popup yet
        if (!req.isWaitlistEntry()) { 
        	System.out.println(waitlistJustArrived);
            return "PROMPT: NO_SEATS_FOUND";
        }

        
        waitlistOrder = addNewOrder(new WriteRequest(
			req.getOrderDateTime(),
			req.getNumberOfGuests(),
			req.getSubscriberId(),
			req.getContact()
		));
        String orderNum = waitlistOrder.getOrderNumber();
        // Add to the DLL queue
        BistroServer.waitlistJustArrived.enqueue(waitlistOrder); 
        System.out.println(waitlistJustArrived);
        
        printWaitlists(BistroServer.waitlistJustArrived, 1);
        dbcon.markArrivalAtTerminal(waitlistOrder.getOrderNumber());
        dbcon.changeStatus("WAITING", orderNum);
		dbcon.setOrderType(waitlistOrder.getOrderNumber(),"ON_THE_SPOT");
        return "The restaurant is full. You've been added to the waitlist.\n" +
               "Order Number: " + orderNum + "\n" +
               "Confirmation Code: " + waitlistOrder.getConfirmationCode();
    }
    

    
    /** * Sorts a set of tables into a list
     * @param tableSet = the set of tables to sort
     * @param copyCurrentBistroState = whether to copy the current taken state of the tables
     * @return a sorted list of tables
     */
    protected List<Table> sortTables(Set <Table> tableSet, boolean copyCurrentBistroState) {
		List<Table> tableList = new ArrayList<>();
		for (Table t : tableSet) {
			tableList.add(new Table(t.getId(), t.getCapacity(), (copyCurrentBistroState) ? t.isTaken(): false ));
		}
		tableList.sort(null);
		return tableList;
	}
    
    
    /** * Prints the desired waitlist to the console
     * @param waitlist = the waitlist to print
     * @param isJustArrived = 1 for just arrived waitlist, 0 for ordered in advance waitlist
     */
    public void printWaitlists(WaitingList waitlist, int isJustArrived) {
    	if (isJustArrived == 1) {
			System.out.println("Waitlist - Just Arrived:");
			System.out.println(waitlist.toString());
		} else {
			System.out.println("Waitlist - Ordered In Advance:");
			System.out.println(waitlist.toString());
		}
	}

    /**
     * Adds a new order to the database
     * @param r the WriteRequest containing order details
     * @return the created Order
     */
    public Order addNewOrder(Request r) {
    	WriteRequest req = (WriteRequest) r;
    	System.out.println("Adding new order for subscriber ID: " + req.getSubscriberId());
    	String email;
    	if (!req.getSubscriberId().equals("0")) {	

    		email = dbcon.readEmail(req.getSubscriberId());
    	}
    	else {
    		email = req.getContact();
    	}
    	
    	
    	System.out.println("Retrieved email: " + email);
    	String orderNumber = dbcon.OrderNumber();
    	System.out.println("Generated order number: " + orderNumber);
    	ArrayList<String> args = new ArrayList<>();
    	args.add(orderNumber);
    	args.add(req.getOrderDateTime());
    	args.add(req.getNumberOfGuests());
    	args.add(1000 + Integer.parseInt(orderNumber) + ""); //confirmation code
    	args.add(req.getSubscriberId());
    	args.add(email);
    	Order o = new Order(args);
    
    	System.out.println(	dbcon.addOrder(o,req.getQuery()));
    	return o;
    }

    /**
     * Reserves a table in advance
     * @param r the ReserveRequest
     * @return the confirmation code or available slots message
     */
    public synchronized String reserveTableInAdvance(Request r) {
    	ReserveRequest req = (ReserveRequest) r;
    	LocalDateTime requested =LocalDateTime.parse(req.getOrderDateTime(), DT_FMT);
        LocalDateTime before = requested.minusHours(1).minusMinutes(30);
        LocalDateTime after   = requested.plusHours(1).plusMinutes(30);
    
    	ShowTakenSlotsRequest slotReq = new ShowTakenSlotsRequest(
				Integer.parseInt(req.getNumberOfGuests()), req.getOrderDateTime(),before,after
				);
    	Map<String,Integer> guests_in_time = prepareGuestsInTimeList(slotReq, true);
    	//prepare tables copy
    	List<Table> tables = sortTables(currentBistro.keySet(), false);
    	System.out.println("Tables: " + tables);
    	int available = checkAvailability(tables, guests_in_time,"-1");
    	for (Table t : tables) {
    		t.setTaken(false);
    	}
		if (available != -1) {
			Order o = addNewOrder(req);
			dbcon.setOrderType(o.getOrderNumber(), "IN_ADVANCE");
			return o.toString();
		}
		else{
             StringBuilder sb = new StringBuilder("No available tables at requested time. Available slots:\n");
             boolean thereAreOptions = false;
             before = before.minusHours(1).minusMinutes(30);
             requested = requested.minusHours(1).minusMinutes(30);
             LocalDateTime until = after;
             after = after.minusHours(1).minusMinutes(30);
             
             while (!before.isAfter(until)) {
            	 slotReq = new ShowTakenSlotsRequest(
						 Integer.parseInt(req.getNumberOfGuests()),
						 requested.toString(),
						 before,
						 after
						 );
            	 guests_in_time = prepareGuestsInTimeList(slotReq,true);
            	 if (checkAvailability(tables, guests_in_time,"-1") > 0) {
            		 thereAreOptions = true;
            		 sb.append(requested.format(DT_FMT).toString()).append("\n");
            	 }
            	 for (Table t : tables) {
            		 t.setTaken(false);
            	 }
            	 before = before.plusMinutes(30);
            	 requested = requested.plusMinutes(30);
            	 after = after.plusMinutes(30);
             }
             return (thereAreOptions)? sb.toString() : "No available tables at requested time or near it.";
             
		}
    }
    
    /**
     * Prepares a map of guests in time from the database response
     * @param r the ShowTakenSlotsRequest
     * @param isNotInDatabase indicates if the current request is not yet in the database
     * @return a map of confirmation codes to number of guests
     */
    protected Map<String,Integer> prepareGuestsInTimeList(Request r,boolean isNotInDatabase) {
    	ShowTakenSlotsRequest slotReq = (ShowTakenSlotsRequest) r;
    	String open_orders_in_time_string = dbcon.getTakenSlots(slotReq);
		String[] open_orders_in_time_array = open_orders_in_time_string.split(",");
		HashMap<String,Integer> guests_in_time = new HashMap<>();
		//prepare guests in time list
		for (String s : open_orders_in_time_array) {
			if (!s.isEmpty())
				guests_in_time.put(s.split(":")[0], Integer.parseInt(s.split(":")[1]));
		}
		if (isNotInDatabase) {
			guests_in_time.put("-1",slotReq.getNumberOfGuests());
		}
		System.out.println("Guests in time is: "+ guests_in_time);
    	return guests_in_time;
		
    }
    
    

    /**Starting the server
     * @param p the port to listen on
     * */
    public static void runServer(String p) {
        int port;
        try {
            port = Integer.parseInt(p);
        } catch (Exception e) {
            port = DEFAULT_PORT;
        }

        BistroServer sv = new BistroServer(port);
        Thread monitorThread = new Thread(new BistroMonitor(sv));
        monitorThread.setDaemon(true); // dies when server stops
        monitorThread.start();
        try {
            sv.listen();
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
        }
    }
    
    @Override
    protected void serverStopped() {
    	try {
			ConnectionPool.getInstance().shutdown();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	System.exit(0);
    }
    
    /**
     * Gets the table for a given order based on confirmation code
     * @param r the GetTableRequest
     * @return the table information or waitlist status
     */
    private String getTableForOrder(Request r) {
		GetTableRequest req = (GetTableRequest) r;
		System.out.println("Current Bistro: "+currentBistro.toString());
		for (Entry<Table, Order> entry : currentBistro.entrySet()) {
			Order order = entry.getValue();
			if (order != null && order.getConfirmationCode().equals(req.getConfcode())) {
				order.setSittingtime(BistroServer.dateTime);
				dbcon.markOrderAsSeated(order.getOrderNumber());
				return  entry.getKey().prettyToString();
				
			}
		}
		//waitlistJustArrived, waitListOrderedInAdvance
		for( Order o : waitlistOrderedInAdvance) {
			if (o.getConfirmationCode().equals(req.getConfcode())) {
				return "Your table will be available soon.";
			}
		}
		for( Order o : waitlistJustArrived) {
			if (o.getConfirmationCode().equals(req.getConfcode())) {
				return "You are currently on the waitlist.";
			}
		}
		String[] args = dbcon.getOrderFromConfCode(req.getQuery(), req.getConfcode()).split(",");
		if (args[0].equals("Not found")) {
			return "No open order found with that number";
		}
		String date = args[1];
		int number_of_guests = Integer.parseInt(args[2]);
		
		LocalDateTime orderDate = LocalDateTime.parse(date, DT_FMT);
//		if(orderDate.isBefore(LocalDateTime.now().minusMinutes(15)) || orderDate.isAfter(LocalDateTime.now().plusMinutes(15))) {
//			return "Your reservation is for " + orderDate.format(DT_FMT).toString()+" Please arrive within 15 minutes of your reservation time.";
//		}
		if(orderDate.isBefore(BistroServer.dateTime.minusMinutes(15)) || orderDate.isAfter(BistroServer.dateTime.plusMinutes(15))) {
			return "Your reservation is for " + orderDate.format(DT_FMT).toString()+" Please arrive within 15 minutes of your reservation time.";
		}
		Map<String,Integer> guests_in_time = prepareGuestsInTimeList(new ShowTakenSlotsRequest(number_of_guests,date), false);
		for (Order o : currentBistro.values()) {
			if (o != null) {
				guests_in_time.remove(o.getConfirmationCode());
			}
		}
		guests_in_time.remove(req.getConfcode());
		
		System.out.println("Guests in time list: " + guests_in_time.toString());
		Order o = new Order(Arrays.asList(args),1);
		Table desiredTable = null;
		int tableId = trySeatNow(guests_in_time,req.getConfcode(),number_of_guests);
		// update actual arrival time
		dbcon.markArrivalAtTerminal(o.getOrderNumber());
		if(tableId!=-1) {
        	for (Table t : currentBistro.keySet()) {
        		if (t.getId() == tableId) {
        			desiredTable = t;
        			System.out.println("Found desired table with id: " + t.getId());
        			o.setSittingtime(BistroServer.dateTime);
        			System.out.println("Setting sitting time to current Bistro time: " + BistroServer.dateTime.toString() + "To order number "+ o.getOrderNumber());
					currentBistro.put(t, o); // Seat at the first available table
					t.setTaken(true);
				
					dbcon.markOrderAsSeated(o.getOrderNumber());
					break;
				}
        	}
			return (desiredTable !=null)? desiredTable.prettyToString() : "Error locating table.";
		}
		else {
			System.out.println("BeforeEnqueue, order dateTime is: "+o.getOrderDateTime());
			dbcon.changeStatus("WAITING",o.getOrderNumber());
			waitlistOrderedInAdvance.enqueue(o);
			System.out.println("After enqueue");
			return "No available tables at the moment. Please wait to be seated.";
		}
	
	}

	/**
	 * * Handles a customer leaving their table Searches by confirmation code
	 * 
	 * @param r the LeaveTableRequest containing the confirmation code
	 * @return a message indicating success or failure
	 */
	public String leaveTable(Request r) {
		LeaveTableRequest req = (LeaveTableRequest) r;
		String confcode = req.getConfCode();
		for (Entry<Table, Order> entry : currentBistro.entrySet()) {
			Order order = entry.getValue();
			if (order != null && order.getConfirmationCode().equals(confcode)&& order.getSittingtime()!=null) {
				currentBistro.put(entry.getKey(), null);
				entry.getKey().setTaken(false);
				String userType = dbcon.closeOrder(req);
				
				if(userType == null) {
					return "Order closed successfully. Your bill is " + BILL + " NIS. Thank you for dining with us!";
				}
				if (userType.equals("Error")) {
					return "Error closing order in database.";
				}
				if(userType.equals("Not found")) {
					return "No order found with that confirmation code.";
				}
				else {
					
					return "Order closed successfully. As a subscriber, you get a 10% discount! Your final bill is " + (BILL*0.9) + " NIS. Thank you for dining with us!";
				}
			}
		}
		return "Error: No order with that confirmation code is currently seated.";
		}	

	/**
	 * * Handles adding a new table to the restaurant
	 * 
	 * @param r the AddTableRequest containing the table details
	 * @return a message indicating success or failure
	 */
	public String addTable(Request r) {
		AddTableRequest req = (AddTableRequest)r;
		if(dbcon.addNewTable(req)) {
			return "new Table with " +req.getCap() +" spots added sucessfully, action will take effect in the next month";
		}
		else {
			return "ERROR: new Table could not be added";
		}
	}

	/**
	 * * Handles removing a table from the restaurant
	 * 
	 * @param r the RemoveTableRequest containing the table ID
	 * @return a message indicating success or failure
	 */
	public String removeTable(Request r) {
		RemoveTableRequest req = (RemoveTableRequest)r;
		if(dbcon.removeTable(req)) {
			return "Table number " +req.getId() + " was removed sucessfully, action will take effect in the next month"; 
		}
		return "ERROR: table could not be removed";
	}

	/**
	 * * Handles updating a table's capacity
	 * 
	 * @param r the UpdateTableCapacityRequest containing the table details
	 * @return a message indicating success or failure
	 */
	public String updateTable(Request r) {
		UpdateTableCapacityRequest req = (UpdateTableCapacityRequest)r;
		if(!dbcon.removeTable(req.getRemoveReq())) {
			return "ERROR: updating the table failed";
		}
		if(!dbcon.addNewTable(req.getAddReq())) {
			return "ERROR: updating the table failed";
		}
		return "Updated table number " +req.getRemoveReq().getId() + " to " +req.getAddReq().getCap() + " sucessfully"; 
	}

	/**
	 * * Attempts to seat a party immediately 
	 * @param guests_in_time   map of confirmation codes to number of guests
	 * @param confCode         the confirmation code of the party to seat now
	 * @param number_of_guests the number of guests in the party to seat now
	 * @return the table ID if seated, -1 if not enough tables or others can be
	 *         seated
	 */
	public int trySeatNow(Map<String,Integer> guests_in_time, String confCode, int number_of_guests) {
		System.out.println("trySeatNow started with Args:\n1.guests_in_time=" +guests_in_time+"\n2.confCode= "+confCode+"\n3.numberOfGuests: "+number_of_guests);
		List<Table> tablesCopy = sortTables(currentBistro.keySet(),true);
		Map<String,Integer> tempGuestsInTime = new HashMap<>();
		tempGuestsInTime.put(confCode, number_of_guests);
		int tableId = checkAvailability(tablesCopy, tempGuestsInTime,confCode);
		System.out.println("TableId is: "+tableId);
		int canSeatOthers = checkAvailability(tablesCopy, guests_in_time,confCode);
		System.out.println("CanSeatOthers is: "+canSeatOthers);
		if (tableId != -1 && canSeatOthers == 0) {
			return tableId;
		}
		return -1;
	}

	/**
	 * * Handles a customer checking their spot in the waitlist Searches by
	 * confirmation code
	 * 
	 * @param r the AlterWaitlistRequest containing the confirmation code
	 * @return a message indicating their spot or failure
	 */
	public String handleSpotWaitlist(Request r) {
		String confCode = ((AlterWaitlistRequest)r).getConfCode();
        // Accessing the static waitlist instance in BistroServer to remove the node
        int spot= BistroServer.waitlistJustArrived.getSpotInQueue(confCode);
        
        if (spot != -1) {
            return confCode+ " is in place "+spot+ " in the waiting list.";
        } else {
            return "Could not find an order with this confiramtion code in the waiting list.";
        }
	}

	public Map<Table,Order> getCurrentBistro(){
		return currentBistro;
	}
	public WaitingList getRegularWaitlist() {
		return waitlistJustArrived;
	}
	public WaitingList getAdvanceWaitlist() {
		return waitlistOrderedInAdvance;
	}
	public int getMaxTable(Request r) {
		int res = 0;
		for(Table t : currentBistro.keySet()) {
			if(t.getCapacity()>res) {
				res = t.getCapacity();
			}
		}
		return res;
	}
}


