// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
//
package MiddlewareImpl;

import LockManager.DataObj;
import LockManager.TimeObj;
import ResInterface.*;
import TransactionManager.TransactionManager;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class MiddlewareManagerImpl implements ResourceManager
{
    static ResourceManager rmFlight = null;
    static ResourceManager rmCar = null;
    static ResourceManager rmRoom = null;
    static TransactionManager transactionManager = null;

    static Hashtable<Integer, Long> clientTime = new Hashtable<>();
    static long timeout = 30000;

    public static void main(String args[]) {
        //Set up the server
        int port_server = 1099;

        if (args.length == 1) {
            port_server = Integer.parseInt(args[0]);
        } else if (args.length != 0 &&  args.length != 1) {
            System.err.println ("Wrong usage");
            System.out.println("Usage: java ResImpl.ResourceManagerImpl [port]");
            System.exit(1);
        }

        try {
            // create a new Server object
            MiddlewareManagerImpl obj = new MiddlewareManagerImpl();
            // dynamically generate the stub (client proxy)
            ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry(port_server);
            registry.rebind("Group4MiddlewareManager", rm);

            System.err.println("Middleware ready");
        } catch (Exception e) {
            System.err.println("Middleware exception: " + e.toString());
            e.printStackTrace();
        }

        //Connect to the resource managers
        rmCar = connectToAResourceManager("localhost", 1099, "Group4ResourceManagerCar");
        rmFlight = connectToAResourceManager("localhost", 1099, "Group4ResourceManagerFlight");
        rmRoom = connectToAResourceManager("localhost", 1099, "Group4ResourceManagerRoom");

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }

        //Create the transaction manager
        transactionManager = new TransactionManager();

    }

    public static ResourceManager connectToAResourceManager(String server, int port, String registeryName)
    {
        ResourceManager rm = null;
        try
        {
            // get a reference to the rmiregistry
            Registry registry = LocateRegistry.getRegistry(server, port);
            // get the proxy and the remote reference by rmiregistry lookup
            rm = (ResourceManager) registry.lookup(registeryName);
            if(rm!=null)
            {
                System.out.println("Successful");
                System.out.println("Connected to RM: "+registeryName);
            }
            else
            {
                System.out.println("Unsuccessful connection to "+registeryName);
            }
            // make call on remote method
        }
        catch (Exception e)
        {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }

        return rm;
    }

    public MiddlewareManagerImpl() throws RemoteException {
    }


    @Override
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyFlight(flightNum), DataObj.WRITE)){
            try{
                result = rmFlight.addFlight(id,flightNum,flightSeats,flightPrice);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public boolean addCars(int id, String location, int numCars, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyCar(location), DataObj.WRITE)){
            try{
                result = rmCar.addCars(id,location,numCars,price);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyRoom(location), DataObj.WRITE)){
            try{
                result = rmRoom.addRooms(id,location,numRooms,price);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public int newCustomer(int id) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        int customerId = -1;
        try{
            customerId =rmFlight.newCustomer(id);
            rmRoom.newCustomer(id, customerId);
            rmCar.newCustomer(id, customerId);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        transactionManager.lock(id, TransactionManager.getKeyCustomer(customerId), DataObj.WRITE);
        return customerId;
    }

    @Override
    public boolean newCustomer(int id, int cid) throws RemoteException, TransactionAbortedException, InvalidTransactionException{
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyCustomer(cid), DataObj.WRITE)){
            try{
                result = rmFlight.newCustomer(id, cid) &&
                        rmRoom.newCustomer(id, cid) &&
                        rmCar.newCustomer(id, cid);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public boolean deleteFlight(int id, int flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyFlight(flightNum), DataObj.WRITE)){
            try{
                result = rmFlight.deleteFlight(id,flightNum);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public boolean deleteCars(int id, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyCar(location), DataObj.WRITE)){
            try{
                result = rmCar.deleteCars(id,location);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public boolean deleteRooms(int id, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyRoom(location), DataObj.WRITE)){
            try{
                result = rmRoom.deleteRooms(id,location);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public boolean deleteCustomer(int id, int customer) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyCustomer(customer), DataObj.WRITE)){
            try{
                result = rmFlight.deleteCustomer(id, customer) &&
                        rmCar.deleteCustomer(id, customer) &&
                        rmRoom.deleteCustomer(id, customer);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public int queryFlight(int id, int flightNumber) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        int seats = -1;
        if(transactionManager.lock(id, TransactionManager.getKeyFlight(flightNumber), DataObj.READ)){
            try{
                seats=rmFlight.queryFlight(id,flightNumber);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return seats;
    }

    @Override
    public int queryCars(int id, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        int numCars = -1;
        if(transactionManager.lock(id, TransactionManager.getKeyCar(location), DataObj.READ)){
            try{
                numCars=rmCar.queryCars(id,location);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return numCars;
    }

    @Override
    public int queryRooms(int id, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        int numRooms = -1;
        if(transactionManager.lock(id, TransactionManager.getKeyRoom(location), DataObj.READ)){
            try{
                numRooms=rmRoom.queryRooms(id,location);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return numRooms;
    }

    @Override
    public String queryCustomerInfo(int id, int customer) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        String bill = "";
        if(transactionManager.lock(id, TransactionManager.getKeyCustomer(customer), DataObj.READ)){
            try{
                bill+=rmFlight.queryCustomerInfo(id, customer);
                String lines[] =rmCar.queryCustomerInfo(id, customer).split("\\r?\\n");
                if (lines.length > 1 ){
                    bill += lines[1] + "\n";
                }
                lines =rmRoom.queryCustomerInfo(id, customer).split("\\r?\\n");
                if (lines.length > 1 ){
                    bill += lines[1];
                }
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return bill;
    }

    @Override
    public int queryFlightPrice(int id, int flightNumber) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        int price = -1;
        if(transactionManager.lock(id, TransactionManager.getKeyFlight(flightNumber), DataObj.READ)){
            try{
                price=rmFlight.queryFlightPrice(id, flightNumber);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return price;
    }

    @Override
    public int queryCarsPrice(int id, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        int price = -1;
        if(transactionManager.lock(id, TransactionManager.getKeyCar(location), DataObj.READ)){
            try{
                price=rmCar.queryCarsPrice(id,location);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return price;
    }

    @Override
    public int queryRoomsPrice(int id, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        int price = -1;
        if(transactionManager.lock(id, TransactionManager.getKeyRoom(location), DataObj.READ)){
            try{
                price=rmRoom.queryRoomsPrice(id,location);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return price;
    }

    @Override
    public boolean reserveFlight(int id, int customer, int flightNumber) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyFlight(flightNumber), DataObj.WRITE) &&
                transactionManager.lock(id, TransactionManager.getKeyCustomer(customer), DataObj.WRITE)){
            try{
                result = rmFlight.reserveFlight(id,customer,flightNumber);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public boolean reserveCar(int id, int customer, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyCar(location), DataObj.WRITE) &&
                transactionManager.lock(id, TransactionManager.getKeyCustomer(customer), DataObj.WRITE)){
            try{
                result = rmCar.reserveCar(id,customer,location);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public boolean reserveRoom(int id, int customer, String locationd) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)) {
            resetTime(id);
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyRoom(locationd), DataObj.WRITE) &&
                transactionManager.lock(id, TransactionManager.getKeyCustomer(customer), DataObj.WRITE)){
            try{
                result = rmRoom.reserveRoom(id, customer, locationd);
            }
            catch(Exception e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public boolean cancelRoom(int id, int customer, String location) throws RemoteException {
        return false;
    }

    @Override
    public boolean cancelFlight(int id, int customer, int flightNumber) throws RemoteException {
        return false;
    }

    @Override
    public boolean cancelCar(int id, int customer, String location) throws RemoteException {
        return false;
    }

    @Override
    public boolean itinerary(int id, int customer, Vector flightNumbers, String location, boolean Car, boolean Room) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)){
            resetTime(id);
        } // the case where the transaction has been aborted due to Client Timeout is
        //notified to the client by the exception thrown when tries to lock with invalid Xid
        //try {
            Vector flightResults = new Vector(flightNumbers.size());
            boolean carResult = true, roomResult = true;
            for(Object flightNumber: flightNumbers){
                String flightNumberString = String.valueOf(flightNumber);
                flightResults.add(rmFlight.reserveFlight(id, customer, Integer.parseInt(flightNumberString)));
            }
            if(Car){
                carResult = rmCar.reserveCar(id, customer, location);
            }
            if(Room){
                roomResult = rmRoom.reserveRoom(id, customer, location);
            }

            // Verify if a reservation has not been made
            /*if (flightResults.contains(false) || !carResult || !roomResult) {
                // Cancel all reservations
                for(int i=0; i < flightNumbers.size(); i++){
                    if((boolean)flightResults.get(i)){
                        String flightNumberString = String.valueOf(flightNumbers.get(i));
                        rmFlight.cancelFlight(id, customer, Integer.parseInt(flightNumberString));
                    }
                }
                if(Car && carResult){
                    rmCar.cancelCar(id, customer, location);
                }
                if(Room && roomResult){
                    rmRoom.cancelRoom(id, customer, location);
                }

                return false;
            }*/
            return true;
        //}

    }

    @Override
    public int start() throws RemoteException {
        checkOldTransactions();
        int id = transactionManager.start();
        System.out.println("started transaction : " + id);
        resetTime(id);
        return id;
    }

    @Override
    public boolean commit(int id) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        checkOldTransactions();
        if (clientTime.containsKey(id)){
            clientTime.remove(id);
        }
        return transactionManager.commit(id);

    }

    @Override
    public void abort(int id) throws RemoteException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)){
            TransactionManager.transactions.remove(id);
            clientTime.remove(id);
            System.out.println("aborted transaction " + id);
        }

    }

    @Override
    public boolean shutdown() throws RemoteException {
        return false;
    }

    public void resetTime(int id) {
        Date date = new Date();
        long timestamp = date.getTime();
        clientTime.put(id, timestamp);
        System.out.println("resetted time for "+id);
    }

    public void checkOldTransactions() {
        Date date = new Date();
        long time = date.getTime();
        // create a list of id to remove from ClientTime
        // and remove them after for loop
        //otherwise, raise ConcurrentModificationException
        List<Integer> oldTransactions = new ArrayList<>();
        for (int id : clientTime.keySet()){
            long timestamp = clientTime.get(id);
            if (time - (timestamp + timeout) > 0) {
                oldTransactions.add(id);
            }
        }
        for (int id : oldTransactions){
            try {
                timeoutAbort(id);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (InvalidTransactionException e) {
                e.printStackTrace();
            }
        }
    }

    public void timeoutAbort(int id) throws RemoteException, InvalidTransactionException {
        //different than abort, in order to not return an irrelevant answer to a client when he makes a request
        TransactionManager.transactions.remove(id);
        clientTime.remove(id);
        System.out.println("aborted transaction " + id + " due to time out from client");
    }


}