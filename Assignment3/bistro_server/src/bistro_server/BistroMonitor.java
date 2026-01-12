package bistro_server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import entities.Order;
import entities.Table;
import entities.requests.CancelRequest;
import entities.requests.ShowTakenSlotsRequest;

/**
 * A class that monitors the bistro server for various tasks such as checking
 * order times, notifying customers, and managing the waiting list.
 */
public class BistroMonitor implements Runnable {
	private BistroServer server;
	private Map<Order, LocalDateTime> pending;
	private static final EmailService emailService = new EmailService();
	/**
	 * Constructor for BistroMonitor.
	 * 
	 * @param server The BistroServer instance to monitor.
	 */
	public BistroMonitor(BistroServer server) {
		pending = new HashMap<>();
		this.server = server;
	}

	/**
	 * The main run method that continuously checks orders and manages time.
	 */
	@Override
	public void run() {
		while (true) {
			try {
				checkOrdersAndAdvanceTime();
				checkPendingOrders();
				checkExpiredOrders();
				notifyAboutOrder();
				trySeatFromWaitlist();
				Thread.sleep(30000); // Check every 60 seconds
				 BistroServer.dateTime = BistroServer.dateTime.plusMinutes(15);
				 System.out.println("Time advanced to: " + BistroServer.dateTime);

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	/**
	 * A method that notifies customers about their orders that are due in 2 hours
	 */
	private void notifyAboutOrder() {
		Map<String,String> contacts=server.dbcon.OrdersToNotify();
		for(Map.Entry<String, String> entry : contacts.entrySet()) {
	    	String orderNumber=entry.getKey();
	    	String contact=entry.getValue();
	    	System.out.println(" Order " + orderNumber
					+ " has been notified through contact.");
	    	if(contact.contains("@")) sendEmailOrderInTwoHours(orderNumber, contact);
			ServerUI.updateInScreen("for contact: "+ contact+
				"\n your order " + orderNumber
				+ " is in 2 hours" 
			);
	    }
		
	}
	
	/**
	 * A method that checks orders that ordered in advance and didn't arrive yet
	 */
	private void checkExpiredOrders() {
		Map<Table, Order> currentBistro = server.getCurrentBistro();
		Set<String> expiredOrders = new HashSet<>();
		for(Order o:currentBistro.values()) {
			if(o!=null) {
				expiredOrders.add(o.getOrderNumber());
			}
		
		}
	    Map<String,String> contacts=server.dbcon.ExpirePendingOrders(expiredOrders);
	    for(Map.Entry<String, String> entry : contacts.entrySet()) {
	    	String orderNumber=entry.getKey();
	    	String contact=entry.getValue();
	    	System.out.println(" Order " + orderNumber
					+ " has expired before seating.");
	    	if(contact.contains("@")) sendEmailOrderExpired(orderNumber, contact);
			ServerUI.updateInScreen("for contact: "+ contact+
				" Order " + orderNumber
				+ " has expired before seating, please contact the bistro staff."
			);
	    }
	      
	}
	/**
	 * A method that checks orders that entered through the waiting lists and didn't arrive yet 
	 */
	private void checkPendingOrders() {
		List<Order> toRemove = new ArrayList<>();
		Map<Table, Order> currentBistro = server.getCurrentBistro();
	    for (Map.Entry<Order, LocalDateTime> entry : pending.entrySet()) {
	        Order order = entry.getKey();
	        if(order.getSittingtime()!=null) {
	        	toRemove.add(order);
	        	continue;
	        }
	        LocalDateTime addedTime = entry.getValue();
	        if (BistroServer.dateTime.isAfter(addedTime.plusMinutes(15))) {
	            // Time exceeded 15 minutes
	            System.out.println(" Seating time exceeded for order: " + order.getConfirmationCode());
	            if(order.getContact().contains("@")) sendEmailOrderExpired(order.getOrderNumber(), order.getContact());
	            ServerUI.updateInScreen("for contact: "+ order.getContact()+
	                " \n Seating time exceeded for order: " + order.getOrderNumber()
	                + " Please contact the bistro staff."
	            );
	            server.dbcon.cancelOrder(new CancelRequest(order.getConfirmationCode()));
	            for(Map.Entry<Table, Order> tableEntry : currentBistro.entrySet()) {
	            	if(tableEntry.getValue()!=null && tableEntry.getValue().getConfirmationCode().equals(order.getConfirmationCode())) {
	            		Table table=tableEntry.getKey();
	            		table.setTaken(false);
	            		currentBistro.put(table, null);
	            		break;
	            	}
	            }
	            toRemove.add(order);
	        }
	    }
	        
	    for(Order o:toRemove) {
	    	 pending.remove(o);
	    	  
	    }
	}
	

	/**
	 * A method that checks orders that have been seated for more than 2 hours and
	 * notifies the customers.
	 */
	private void checkOrdersAndAdvanceTime() {
	    Map<Table, Order> currentBistro = server.getCurrentBistro();

	    System.out.println("Current simulated time: " + BistroServer.dateTime);

	    for (Map.Entry<Table, Order> entry : currentBistro.entrySet()) {

	        Table table = entry.getKey();
	        Order order = entry.getValue();

	        if (order == null || order.getSittingtime() == null) {
	            continue; // table is empty
	        }
	        System.out.println("Checking order number " + order.getOrderNumber());

	        LocalDateTime sittingTime = order.getSittingtime();
	        LocalDateTime twoHoursAfterSitting = sittingTime.plusHours(2);

	        //  check if current time is AFTER sitting time + 2 hours
	        if (!BistroServer.dateTime.isBefore(twoHoursAfterSitting)) {

	            
	            System.out.println(" Order " + order.getConfirmationCode()
	                    + " exceeded 2 hours at table " + table.getId());
	            if(order.getContact().contains("@")) sendEmailTimeExceeded(order.getOrderNumber(), order.getContact());
	            ServerUI.updateInScreen("contact: "+ order.getContact()+
	                " Order " + order.getOrderNumber()
	                + "\nYour bill is " + (order.getSubscriberId().equals("0")?BistroServer.BILL : BistroServer.BILL*0.9) + " NIS. Thank you for dining with us!"
	                
	            );
	        }
	    }

	}
	
	/**
	 * A method that tries to seat orders from the waiting list when tables become
	 * available.
	 */
	private void trySeatFromWaitlist() {
		System.out.println("------------------------------------------");
		List<WaitlistNode> toRemove = new ArrayList<>();
		WaitingList RegularList= server.getRegularWaitlist();
		int res;
		WaitingList InAdvanceList= server.getAdvanceWaitlist();
		Map<Table, Order> currentBistro = server.getCurrentBistro();
	    for(Order wl: InAdvanceList) {
	    	if(!currentBistro.containsValue(null)) {
	    		break;
	    	}
		    res=trySeatHelper(wl,InAdvanceList);
		    System.out.println("Tried to seat order from inAdvance, confCode: "+wl.getConfirmationCode()+", result is: "+res);
		    if(res!=-1) {
		    	toRemove.add(new WaitlistNode(wl));
		    	if(wl.getContact().contains("@")) sendTableReadyEmail(wl.getContact(), wl.getOrderNumber(), res);
		    	ServerUI.updateInScreen("for contact: "+wl.getContact()+"\n order number: "+wl.getOrderNumber()+ "\ntable number "+res+" got available for you,please come to the Bistro within 15 minute from this massage");
		    	server.dbcon.changeStatus("OPEN", wl.getOrderNumber());
		   	}
	    }
	    for(WaitlistNode node : toRemove) {
	    	InAdvanceList.dequeue(node);
	    }
	    toRemove.clear();
		for(Order wl2: RegularList) {
			if(!currentBistro.containsValue(null)) {
	    		break;
			}
		    res=trySeatHelper(wl2,RegularList);
		    System.out.println("Tried to seat order from regular, confCode: "+wl2.getConfirmationCode()+", result is: "+res);
		    if(res!=-1) {
		    	toRemove.add(new WaitlistNode(wl2));
		    	if(wl2.getContact().contains("@")) sendTableReadyEmail(wl2.getContact(), wl2.getOrderNumber(), res);
		    	ServerUI.updateInScreen("for contact: "+wl2.getContact()+"\norder Number: "+wl2.getOrderNumber()+"\ntable number  "+res+" got available for you,please come to the Bistro within 15 minute from this massage");
		    	server.dbcon.changeStatus("OPEN", wl2.getOrderNumber());
		    }
		}
		for(WaitlistNode node : toRemove) {
	    	RegularList.dequeue(node);
	    }
		
	}
	    
	/**
	 * A helper method that attempts to seat an order from the waiting list.
	 * 
	 * @param order    The order to be seated.
	 * @param waitlist The waiting list from which the order came.
	 * @return The table ID if seated successfully, -1 otherwise.
	 */
	private int trySeatHelper(Order order,WaitingList waitlist) {
		Map<Table, Order> currentBistro = server.getCurrentBistro();
		int guests=Integer.parseInt(order.getNumberOfGuests());
		String confcode=order.getConfirmationCode();
		String OrderDateTime = BistroServer.dateTime.format( DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		Map<String,Integer> guests_in_time = server.prepareGuestsInTimeList(new ShowTakenSlotsRequest(guests,OrderDateTime), false);
		for (Order o : currentBistro.values()) {
			if (o != null) {
				guests_in_time.remove(o.getConfirmationCode());
			}
		}
		guests_in_time.remove(confcode);
		
		System.out.println("Guests in time list: " + guests_in_time.toString());
		List<Table> tablesCopy = server.sortTables(currentBistro.keySet(),true);
		Map<String,Integer> tempGuestsInTime = new HashMap<>();
		tempGuestsInTime.put(confcode,guests);
		int tableId = server.checkAvailability(tablesCopy, tempGuestsInTime,confcode);
		System.out.println("Tables copy: " + tablesCopy.toString());
		int canSeatOthers = server.checkAvailability(tablesCopy, guests_in_time,confcode);
		System.out.println("Current bistro status: " + currentBistro.toString());
		Table desiredTable = null;
		if (tableId != -1 && canSeatOthers == 0) {
        	for (Table t : currentBistro.keySet()) {
        		System.out.println("Checking table with ID: " + t.getId());
        		if (t.getId() == tableId) {
        			desiredTable = t;
        			System.out.println("Found desired table with ID: " + t.getId());	
					currentBistro.put(t, order); // Seat at the first available table
					t.setTaken(true);
					pending.put(order, BistroServer.dateTime);
					break;
				}
        	}
			return (desiredTable !=null)? desiredTable.getId() : -1;
		}
		return -1;
	}
	
	
	private void sendEmailOrderInTwoHours(String orderNum,String contact) {
		String subject = String.format("Reminder: Your reservation at The Bistro (Order #%s)", orderNum);
		String content = String.format(
			    "Hello,\n\n" +
			    "We are looking forward to hosting you at The Bistro!\n\n" +
			    "This is a friendly reminder that your reservation (Order #%s) is scheduled for today in approximately 2 hours.\n" +
			    "Please try to arrive 5-10 minutes early to ensure we can seat you promptly.\n\n" +
			    "See you soon,\n" +
			    "The Bistro Team", 
			    orderNum
			);
		emailService.sendEmail(contact, subject, content);
	}
	
	private void sendEmailOrderExpired(String orderNum,String contact) {
		String subject = String.format("Reminder: Your reservation at The Bistro (Order #%s)", orderNum);
		String content = String.format(
			    "Hello,\n\n" +
			    "We noticed you haven't arrived for your reservation (Order #%s) yet.\n\n" +
			    "As it has been over 15 minutes since your scheduled time, our system has automatically released the table to accommodate other waiting guests.\n" +
			    "If this is a mistake or you are still on your way, please contact the host immediately.\n\n" +
			    "We hope to see you another time.\n" +
			    "The Bistro Team", 
			    orderNum
			);
		emailService.sendEmail(contact, subject, content);
	}
	
	private void sendEmailTimeExceeded(String orderNum, String contact) {
		String subject = String.format("Reminder: Your reservation at The Bistro (Order #%s)", orderNum);
		String content = String.format(
			    "Hello,\n\n" +
			    "We hope you are enjoying your meal with us!\n\n" +
			    "We just wanted to gently notify you that your 2-hour seating window for Order #%s has come to an end.\n" +
			    "We have guests waiting for reservations, so we would appreciate it if you could begin wrapping up your visit.\n\n" +
			    "Thank you for understanding and for dining with us!\n" +
			    "The Bistro Team", 
			    orderNum
			);
		emailService.sendEmail(contact, subject, content);
	}
	
	public void sendTableReadyEmail(String recipientEmail, String orderNum, int tableNumber) {
	    String subject = String.format("Good news! Your table is ready (Order #%s)", orderNum);
	    
	    String body = String.format(
	        "Hello,\n\n" +
	        "Good news! Your table is now ready.\n\n" +
	        "We have prepared Table #%d for your party (Order #%s).\n" +
	        "Please head to the host stand within the next 5 minutes to be seated.\n\n" +
	        "Enjoy your meal!\n" +
	        "The Bistro Team", 
	        tableNumber, orderNum
	    );

	    emailService.sendEmail(recipientEmail, subject, body);
	}
}