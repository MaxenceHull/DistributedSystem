// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
//
package ResImpl;

import ResInterface.*;

import java.util.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ResourceManagerImpl implements ResourceManager 
{
    
    protected RMHashtable m_itemHT = new RMHashtable();
    private Log log;
    private Backup backup;
    static private boolean doBackupAtLaunch = true;
    private boolean crash = false;
    private static int CRASH_AFTER_REQUEST = 0;
    private static int CRASH_AFTER_SENDING_ANSWER = 1;
    private static int CRASH_AFTER_DECISION = 2;
    private static int CRASH_IMMEDIATLY = 3;
    private HashMap<Integer, Boolean> errors;


    public static void main(String args[]) {
        // Figure out where server is running
        String server = "localhost";
        String registryName = "";
        int port = 1099;

        if (args.length == 2) {
            port = Integer.parseInt(args[0]);
            registryName = args[1];
        } else if (args.length != 2) {
            System.err.println ("Wrong usage");
            System.out.println("Usage: java ResImpl.ResourceManagerImpl [port] [registry name]");
            System.exit(1);
        }

        try {
            // create a new Server object
            ResourceManagerImpl obj = new ResourceManagerImpl(registryName);
            // dynamically generate the stub (client proxy)
            ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind(registryName, rm);

            System.err.println("Server " + registryName+ " ready");
            obj.finishCommit();

        } catch (Exception e) {
            System.err.println("Server" + registryName+" exception: " + e.toString());
            e.printStackTrace();
        }

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            //System.setSecurityManager(new RMISecurityManager());
        }

    }
     
    public ResourceManagerImpl(String path) throws RemoteException {
        backup = new Backup(path);
        if(doBackupAtLaunch){
            RMHashtable oldData = backup.getBackup();
            if(oldData == null){
                m_itemHT = new RMHashtable();
            } else {
                m_itemHT = oldData;
            }
            Log oldTransactions = backup.getBackupLog();
            if(oldTransactions != null){
                this.log = oldTransactions;
            }else {
                log = new Log();
            }

        }
        //Set all errors to false
        errors = new HashMap<>();
        errors.put(CRASH_AFTER_REQUEST, false);
        errors.put(CRASH_AFTER_SENDING_ANSWER, false);
        errors.put(CRASH_AFTER_DECISION, false);
        errors.put(CRASH_IMMEDIATLY, false);

    }

    public void finishCommit(){
        for(Integer key:log.hasCommited.keySet()){
            if(log.hasCommited.get(key)){
                System.out.println("Try commit for transaction "+key);
                try{
                    commit(key);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

    private void addEntryForTransaction(int id){
        if(!log.writtenItemsByTrx.containsKey(id)){
            log.writtenItemsByTrx.put(id, new RMHashtable());
            backup.saveTransaction(log);
        }
    }

    // Reads a data item
    private RMItem readData( int id, String key )
    {
        synchronized (log.writtenItemsByTrx){
            addEntryForTransaction(id);
            if(log.writtenItemsByTrx.get(id).containsKey(key)){
                RMItem item = (RMItem) log.writtenItemsByTrx.get(id).get(key);
                if(item.isDeleted){
                    return null;
                }
                return item;
            }
        }

        //Get item from main memory
        synchronized(m_itemHT) {
            return (RMItem) m_itemHT.get(key);
        }
    }

    // Writes a data item
    private void writeData( int id, String key, RMItem value )
    {
        synchronized (log.writtenItemsByTrx){
            addEntryForTransaction(id);
            log.writtenItemsByTrx.get(id).put(key, value);
            backup.saveTransaction(log);
        }

        /*synchronized(m_itemHT) {
            m_itemHT.put(key, value);
        }*/
        //backup.save(m_itemHT);
    }
    
    // Remove the item out of storage
    protected RMItem removeData(int id, String key) {
        addEntryForTransaction(id);
        RMItem item = readData(id, key);
        if(item != null){
            synchronized (log.writtenItemsByTrx){
                item.isDeleted = true;
                backup.saveTransaction(log);
            }
        }

        /*synchronized(m_itemHT) {
             item = (RMItem)m_itemHT.remove(key);
        }*/
        //backup.save(m_itemHT);
        return item;
    }
    
    
    // deletes the entire item
    protected boolean deleteItem(int id, String key)
    {
        Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key );
        // Check if there is such an item in the storage
        if ( curObj == null ) {
            Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
            return false;
        } else {
            if (curObj.getReserved()==0) {
                writeData(id, key, getCopy(curObj));
                removeData(id, curObj.getKey());
                Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
                return true;
            }
            else {
                Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
                return false;
            }
        } // if
    }
    

    // query the number of available seats/rooms/cars
    protected int queryNum(int id, String key) {
        Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key);
        int value = 0;  
        if ( curObj != null ) {
            value = curObj.getCount();
        } // else
        Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
        return value;
    }    
    
    // query the price of an item
    protected int queryPrice(int id, String key) {
        Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key);
        int value = 0; 
        if ( curObj != null ) {
            value = curObj.getPrice();
        } // else
        Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
        return value;        
    }
    
    // reserve an item
    protected boolean reserveItem(int id, int customerID, String key, String location) {
        Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );        
        // Read customer object if it exists (and read lock it)
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );        
        if ( cust == null ) {
            Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
            return false;
        } 
        
        // check if the item is available
        ReservableItem item = (ReservableItem)readData(id, key);
        if ( item == null ) {
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
            return false;
        } else if (item.getCount()==0) {
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
            return false;
        } else {
            Customer copyCustomer = new Customer(cust);
            ReservableItem copyItem = getCopy(item);
            copyCustomer.reserve( key, location, copyItem.getPrice());
            writeData( id, cust.getKey(),  copyCustomer);
            
            // decrease the number of available items in the storage
            copyItem.setCount(item.getCount() - 1);
            copyItem.setReserved(item.getReserved()+1);
            writeData(id, key, copyItem);
            
            Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
            return true;
        }        
    }

    private ReservableItem getCopy(ReservableItem item) {
        ReservableItem itemCopy = null;
        if(item instanceof Hotel){
            itemCopy = new Hotel(item.getLocation(), item.getCount(), item.getPrice());
        }else if(item instanceof Car){
            itemCopy = new Car(item.getLocation(), item.getCount(), item.getPrice());
        }else if(item instanceof Flight){
            itemCopy = new Flight(item.getLocation(), item.getCount(), item.getPrice());
        }
        return itemCopy;
    }

    // Create a new flight, or add seats to existing flight
    //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
        throws RemoteException
    {
        Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $" + flightPrice + ", " + flightSeats + ") called" );
        Flight curObj = (Flight) readData( id, Flight.getKey(flightNum) );
        if ( curObj == null ) {
            // doesn't exist...add it
            Flight newObj = new Flight( flightNum, flightSeats, flightPrice );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addFlight(" + id + ") created new flight " + flightNum + ", seats=" +
                    flightSeats + ", price=$" + flightPrice );
        } else {
            // add seats to existing flight and update the price...
            curObj.setCount( curObj.getCount() + flightSeats );
            if ( flightPrice > 0 ) {
                curObj.setPrice( flightPrice );
            } // if
            writeData( id, curObj.getKey(), new Flight( flightNum, curObj.getCount(), curObj.getPrice()));
            Trace.info("RM::addFlight(" + id + ") modified existing flight " + flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice );
        } // else
        return(true);
    }


    
    public boolean deleteFlight(int id, int flightNum)
        throws RemoteException
    {
        return deleteItem(id, Flight.getKey(flightNum));
    }



    // Create a new room location or add rooms to an existing location
    //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int id, String location, int count, int price)
        throws RemoteException
    {
        Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
        Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
        if ( curObj == null ) {
            // doesn't exist...add it
            Hotel newObj = new Hotel( location, count, price );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addRooms(" + id + ") created new room location " + location + ", count=" + count + ", price=$" + price );
        } else {
            // add count to existing object and update price...
            curObj.setCount( curObj.getCount() + count );
            if ( price > 0 ) {
                curObj.setPrice( price );
            } // if
            writeData( id, curObj.getKey(), new Hotel(location, curObj.getCount(), curObj.getPrice()) );
            Trace.info("RM::addRooms(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
        } // else
        return(true);
    }

    // Delete rooms from a location
    public boolean deleteRooms(int id, String location)
        throws RemoteException
    {
        return deleteItem(id, Hotel.getKey(location));
        
    }

    // Create a new car location or add cars to an existing location
    //  NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int id, String location, int count, int price)
        throws RemoteException
    {
        Trace.info("RM::addCars(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
        Car curObj = (Car) readData( id, Car.getKey(location) );
        if ( curObj == null ) {
            // car location doesn't exist...add it
            Car newObj = new Car( location, count, price );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addCars(" + id + ") created new location " + location + ", count=" + count + ", price=$" + price );
        } else {
            // add count to existing car location and update price...
            curObj.setCount( curObj.getCount() + count );
            if ( price > 0 ) {
                curObj.setPrice( price );
            } // if
            writeData( id, curObj.getKey(), new Car(location, curObj.getCount(), curObj.getPrice()) );
            Trace.info("RM::addCars(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
        } // else
        return(true);
    }


    // Delete cars from a location
    public boolean deleteCars(int id, String location)
        throws RemoteException
    {
        return deleteItem(id, Car.getKey(location));
    }



    // Returns the number of empty seats on this flight
    public int queryFlight(int id, int flightNum)
        throws RemoteException
    {
        return queryNum(id, Flight.getKey(flightNum));
    }


    // Returns price of this flight
    public int queryFlightPrice(int id, int flightNum )
        throws RemoteException
    {
        return queryPrice(id, Flight.getKey(flightNum));
    }


    // Returns the number of rooms available at a location
    public int queryRooms(int id, String location)
        throws RemoteException
    {
        return queryNum(id, Hotel.getKey(location));
    }


    
    
    // Returns room price at this location
    public int queryRoomsPrice(int id, String location)
        throws RemoteException
    {
        return queryPrice(id, Hotel.getKey(location));
    }


    // Returns the number of cars available at a location
    public int queryCars(int id, String location)
        throws RemoteException
    {
        return queryNum(id, Car.getKey(location));
    }


    // Returns price of cars at this location
    public int queryCarsPrice(int id, String location)
        throws RemoteException
    {
        return queryPrice(id, Car.getKey(location));
    }

    // Returns data structure containing customer reservation info. Returns null if the
    //  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
    //  reservations.
    public RMHashtable getCustomerReservations(int id, int customerID)
        throws RemoteException
    {
        Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return null;
        } else {
            return cust.getReservations();
        } // if
    }

    // return a bill
    public String queryCustomerInfo(int id, int customerID)
        throws RemoteException
    {
        Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
        } else {
                String s = cust.printBill();
                Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
                System.out.println( s );
                return s;
        } // if
    }

    // customer functions
    // new customer just returns a unique customer identifier
    
    public int newCustomer(int id)
        throws RemoteException
    {
        Trace.info("INFO: RM::newCustomer(" + id + ") called" );
        // Generate a globally unique ID for the new customer
        int cid = Integer.parseInt( String.valueOf(id) +
                                String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                String.valueOf( Math.round( Math.random() * 100 + 1 )));
        Customer cust = new Customer( cid );
        writeData( id, cust.getKey(), cust );
        Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
        return cid;
    }

    // I opted to pass in customerID instead. This makes testing easier
    public boolean newCustomer(int id, int customerID )
        throws RemoteException
    {
        Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            cust = new Customer(customerID);
            writeData( id, cust.getKey(), cust );
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
            return true;
        } else {
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
            return false;
        } // else
    }


    // Deletes customer from the database. 
    public boolean deleteCustomer(int id, int customerID)
        throws RemoteException
    {
        Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return false;
        } else {
            Customer copyCutomer = new Customer(cust);
            // Increase the reserved numbers of all reservable items which the customer reserved. 
            RMHashtable reservationHT = copyCutomer.getReservations();
            for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {        
                String reservedkey = (String) (e.nextElement());
                ReservedItem reserveditem = copyCutomer.getReservedItem(reservedkey);
                Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
                ReservableItem item  = (ReservableItem) readData(id, reserveditem.getKey());
                ReservableItem copyItem = getCopy(item);
                Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
                copyItem.setReserved(item.getReserved()-reserveditem.getCount());
                copyItem.setCount(item.getCount()+reserveditem.getCount());
                writeData(id, reserveditem.getKey(), copyItem);
            }
            
            // remove the customer from the storage
            removeData(id, copyCutomer.getKey());
            
            Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
            return true;
        } // if
    }

    
    // Adds car reservation to this customer. 
    public boolean reserveCar(int id, int customerID, String location)
        throws RemoteException
    {
        return reserveItem(id, customerID, Car.getKey(location), location);
    }


    // Adds room reservation to this customer. 
    public boolean reserveRoom(int id, int customerID, String location)
        throws RemoteException
    {
        return reserveItem(id, customerID, Hotel.getKey(location), location);
    }

    // Adds flight reservation to this customer.
    public boolean reserveFlight(int id, int customerID, int flightNum)
        throws RemoteException
    {
        return reserveItem(id, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
    }
    
    // Reserve an itinerary 
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room)
        throws RemoteException
    {
        return false;
    }

    @Override
    public int start() throws RemoteException {
        return -1;
    }

    @Override
    public synchronized boolean commit(int id) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        if(crash && errors.get(CRASH_AFTER_SENDING_ANSWER)){
            System.exit(1);
        }
        System.out.println("Decision received for transaction "+id);
        log.hasCommited.put(id, true);
        backup.saveTransaction(log);
        if(crash && errors.get(CRASH_AFTER_DECISION)){
            System.exit(1);
        }

        RMHashtable writes = log.writtenItemsByTrx.get(id);
        if(writes != null){
            synchronized (m_itemHT) {
                for (Object key : writes.keySet()) {
                    String strKey = (String) key;
                    RMItem item = (RMItem) writes.get(key);
                    if (item.isDeleted) {
                        m_itemHT.remove(strKey);
                    } else {
                        item.isDeleted = false;
                        m_itemHT.put(strKey, writes.get(strKey));
                    }
                }
                backup.save(m_itemHT);
            }
        }
        log.writtenItemsByTrx.remove(id);
        log.hasCommited.remove(id);
        backup.saveTransaction(log);
        System.out.println("Transaction "+id+" committed");
        return true;
    }

    @Override
    public void abort(int id) throws RemoteException, InvalidTransactionException {
        log.writtenItemsByTrx.remove(id);
        System.out.println("Transaction "+id+" aborted");
    }

    @Override
    public boolean shutdown() throws RemoteException {
        System.exit(0);
        return true;
    }

    @Override
    public boolean voteRequest(int id) throws RemoteException {
        System.out.println("Vote request received for transaction "+id);
        if(crash && errors.get(CRASH_AFTER_REQUEST)){
            System.exit(1);
        }
        System.out.println("Voted yes");
        return true;
    }

    @Override
    public void crash(String location, int errorCode) throws RemoteException {
        crash = true;
        if(errors.containsKey(errorCode)){
            errors.replace(errorCode, true);
            if(errorCode == CRASH_IMMEDIATLY){
                System.exit(1);
            }
        }else{
            System.out.println("Error code "+errorCode+" is not correct");
        }
    }

}