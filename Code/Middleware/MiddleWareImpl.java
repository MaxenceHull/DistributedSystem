/**
 * Created by pierrehe on 23/09/2017.
 */


import ResInterface.MiddleWare;

import java.util.*;

import java.rmi.RemoteException;
import java.util.Hashtable;

public class MiddleWareImpl implements MiddleWare {

    protected RMHashtable m_itemHT = new RMHashtable();

    //mapping between name of the server (car, room, flight) and the associated rm
    protected Hashtable serverTable = new Hashtable();


    public static void main(String args[]) {

    }

    public MiddleWareImpl() throws RemoteException {
    }

    // Reads a data item
    private RMItem readData( int id, String key )
    {
        synchronized(m_itemHT) {
            return (RMItem) m_itemHT.get(key);
        }
    }

    // Writes a data item
    private void writeData( int id, String key, RMItem value )
    {
        synchronized(m_itemHT) {
            m_itemHT.put(key, value);
        }
    }

    // Remove the item out of storage
    protected RMItem removeData(int id, String key) {
        synchronized(m_itemHT) {
            return (RMItem)m_itemHT.remove(key);
        }
    }

    // reserve an item
    protected boolean reserveItem(int id, int customerID, String serverkey, String location)
            throws RemoteException
    {
        Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +serverkey+ ", "+location+" ) called" );
        // Read customer object if it exists (and read lock it)
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + serverkey + ", "+location+")  failed--customer doesn't exist" );
            return false;
        }

        // ask the corresponding rm if the item is available and reserve it if possible. answer is a String 0-price or 1-price.
        MiddleWare rm = (MiddleWare) serverTable.get(serverkey);
        String answer = null;
        if (serverkey == "room") { answer = rm.reserveRoom(id, customerID, location); }
        else if (serverkey == "flight") { answer = rm.reserveFlight(id, customerID, Integer.parseInt(location)); }
        else if (serverkey == "car") { answer = rm.reserveCar(id, customerID, location); }
        else { return false; }

        String available = answer.split("-")[0];

        if ( available == "0" ) {
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + serverkey+", " +location+") failed--item doesn't exist or no more items" );
            return false;
        } else if ( available == "1" ){
            int price = Integer.parseInt(answer.split("-")[1]);
            String temp = serverkey + "-" + location;
            String key = temp.toLowerCase();
            cust.reserve( key, location, price);
            writeData( id, cust.getKey(), cust );

            Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
            return true;
        } else { return false; }
    }

    // Create a new flight, or add seats to existing flight
    //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
            throws RemoteException
    {
        MiddleWare curRM = (MiddleWare) serverTable.get("flight");
        return curRM.addFlight(id,flightNum,flightSeats,flightPrice);
    }

    public boolean deleteFlight (int id, int FlightNum)
            throws RemoteException
    {
        MiddleWare curRM = (MiddleWare) serverTable.get("flight");
        return curRM.deleteFlight(id, FlightNum);
    }

    public boolean addRooms(int id, String location, int count, int price)
            throws RemoteException
    {
        MiddleWare curRM = (MiddleWare) serverTable.get("room");
        return curRM.addRooms(id, location, count, price);
    }

    public boolean deleteRooms(int id, String location)
            throws RemoteException
    {
        MiddleWare curRM = (MiddleWare) serverTable.get("room");
        return curRM.deleteRooms(id, location);
    }

    public boolean addCars(int id, String location, int count, int price)
            throws RemoteException
    {
        MiddleWare curRM = (MiddleWare) serverTable.get("car");
        return curRM.addCars(id, location, count, price);
    }

    public boolean deleteCars(int id, String location)
            throws RemoteException
    {
        MiddleWare curRM = (MiddleWare) serverTable.get("car");
        return curRM.deleteCars(id, location);
    }

    public int queryFlight(int id, int flightNum)
            throws RemoteException
    {
        MiddleWare curRM = (MiddleWare) serverTable.get("flight");
        return curRM.queryFlight(id, flightNum);
    }

    public int queryFlightPrice(int id, int flightNum)
            throws RemoteException
    {
        MiddleWare curRM = (MiddleWare) serverTable.get("flight");
        return curRM.queryFlightPrice(id, flightNum);
    }

    public int queryRooms(int id, String location)
            throws RemoteException
    {
        MiddleWare curRM = (MiddleWare) serverTable.get("room");
        return curRM.queryRooms(id, location);
    }

    public int queryRoomsPrice(int id, String location)
            throws RemoteException
    {
        MiddleWare curRM = (MiddleWare) serverTable.get("room");
        return curRM.queryRoomsPrice(id, location);
    }

    public int queryCars(int id, String location)
            throws RemoteException
    {
        MiddleWare curRM = (MiddleWare) serverTable.get("car");
        return curRM.queryCars(id, location);
    }

    public int queryCarsPrice(int id, String location)
            throws RemoteException
    {
        MiddleWare curRM = (MiddleWare) serverTable.get("car");
        return curRM.queryCarsPrice(id, location);
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
            // Forward to the all the RM where the customer has a reservation.
            RMHashtable reservationHT = cust.getReservations();
            for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {
                String reservedkey = (String) (e.nextElement());
                String server = reservedkey.split("-")[0];
                MiddleWare rm = (MiddleWare) serverTable.get(server);
                //need to forward the count of reserved item to the corresponding RM
                ReservedItem reserveditem = cust.getReservedItem(reservedkey);
                int count = reserveditem.getCount();
                rm.deleteCustomer(id, customerID, reservedkey, count);
            }

            // remove the customer from the storage
            removeData(id, cust.getKey());

            Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
            return true;
        } // if
    }

    // Adds car reservation to this customer.
    public boolean reserveCar(int id, int customerID, String location)
            throws RemoteException
    {
        return reserveItem(id, customerID, "car", location);
    }


    // Adds room reservation to this customer.
    public boolean reserveRoom(int id, int customerID, String location)
            throws RemoteException
    {
        return reserveItem(id, customerID, "room", location);
    }
    // Adds flight reservation to this customer.
    public boolean reserveFlight(int id, int customerID, int flightNum)
            throws RemoteException
    {
        return reserveItem(id, customerID, "flight", String.valueOf(flightNum));
    }

    // Reserve an itinerary
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room)
            throws RemoteException
    {
        return false;
    }

}
