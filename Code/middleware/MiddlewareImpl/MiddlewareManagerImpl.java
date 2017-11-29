// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
//
package MiddlewareImpl;

import LockManager.DataObj;
import ResInterface.*;
import TransactionManager.TransactionManager;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class MiddlewareManagerImpl implements ResourceManager
{
    static private boolean doBackupAtLaunch = true;
    static private boolean crash = false;
    static public int RM_ROOM = 0;
    static public int RM_FLIGHT = 1;
    static public int RM_CAR = 2;

    static private int CRASH_BEFORE_REQUEST = 0;
    static private int CRASH_AFTER_REQUEST = 1;
    static private int CRASH_AFTER_SOME_VOTES = 2;
    static private int CRASH_AFTER_VOTES = 3;
    static private int CRASH_AFTER_DECIDING = 4;
    static private int CRASH_AFTER_SOME_DECISIONS = 5;
    static private int CRASH_AFTER_DECISIONS = 6;
    static private int CRASH_IMMEDIATLY = 7;
    private HashMap<Integer, Boolean> errors = new HashMap<>();

    static ResourceManager rmFlight = null;
    static ResourceManager rmCar = null;
    static ResourceManager rmRoom = null;
    static ReentrantLock lockAbort = new ReentrantLock();
    static ReentrantLock lockCommit = new ReentrantLock();

    static long ttl = 3000000;

    private TransactionManager transactionManager;


    private static String serverCar = "localhost";
    private static String serverFlight = "localhost";
    private static String serverRoom = "localhost";


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
            //Connect to the resource managers
            rmCar = connectToAResourceManager(serverCar, 1099, "Group4ResourceManagerCar");
            rmFlight = connectToAResourceManager(serverFlight, 1099, "Group4ResourceManagerFlight");
            rmRoom = connectToAResourceManager(serverRoom, 1099, "Group4ResourceManagerRoom");
            obj.finish2PC();
            System.err.println("Middleware ready");
        } catch (Exception e) {
            System.err.println("Middleware exception: " + e.toString());
            e.printStackTrace();
        }



        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            //System.setSecurityManager(new RMISecurityManager());
        }

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

    public MiddlewareManagerImpl(){
        if(doBackupAtLaunch) {
            TransactionManager tm = MiddlewareBackup.getBackup();
            if(tm == null){
                transactionManager = new TransactionManager();
            } else {
                transactionManager = tm;
            }
        }

        errors.put(CRASH_BEFORE_REQUEST, false);
        errors.put(CRASH_AFTER_REQUEST, false);
        errors.put(CRASH_AFTER_SOME_VOTES, false);
        errors.put(CRASH_AFTER_VOTES, false);
        errors.put(CRASH_AFTER_DECIDING, false);
        errors.put(CRASH_AFTER_DECIDING, false);
        errors.put(CRASH_AFTER_SOME_DECISIONS, false);
        errors.put(CRASH_AFTER_DECISIONS, false);
        errors.put(CRASH_IMMEDIATLY, false);
    }

    private void finish2PC(){
        Iterator iter = transactionManager.transactions.iterator();
        while (iter.hasNext()) {
            int transactionId = (int)iter.next();
            if(transactionManager.hasCommitted.get(transactionId)){
                try {
                    commit(transactionId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmFlight == null){
            rmFlight = connectToAResourceManager(serverFlight, 1099, "Group4ResourceManagerFlight");
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyFlight(flightNum), DataObj.WRITE)){
            try{
                result = rmFlight.addFlight(id,flightNum,flightSeats,flightPrice);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmFlight = null;
                abort(id);
                throw e;
            }

            MiddlewareBackup.save(transactionManager);
        }
        return result;
    }

    @Override
    public boolean addCars(int id, String location, int numCars, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmCar == null){
            rmCar = connectToAResourceManager(serverCar, 1099, "Group4ResourceManagerCar");
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyCar(location), DataObj.WRITE)){
            try{
                result = rmCar.addCars(id,location,numCars,price);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmCar = null;
                abort(id);
                throw e;
            }

            MiddlewareBackup.save(transactionManager);
        }
        return result;
    }

    @Override
    public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmRoom == null){
            rmRoom = connectToAResourceManager(serverRoom, 1099, "Group4ResourceManagerRoom");
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyRoom(location), DataObj.WRITE)){
            try{
                result = rmRoom.addRooms(id,location,numRooms,price);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmRoom = null;
                abort(id);
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return result;
    }

    @Override
    public int newCustomer(int id) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmRoom == null){
            rmRoom = connectToAResourceManager(serverRoom, 1099, "Group4ResourceManagerRoom");
        }
        if (rmCar == null){
            rmCar = connectToAResourceManager(serverCar, 1099, "Group4ResourceManagerCar");
        }
        if (rmFlight == null){
            rmFlight = connectToAResourceManager(serverFlight, 1099, "Group4ResourceManagerFlight");
        }
        int customerId = -1;
        pingServers(id);
        try{
            customerId = rmFlight.newCustomer(id);
            rmCar.newCustomer(id, customerId);
            rmRoom.newCustomer(id, customerId);
        } catch (RemoteException e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            abort(id);
            throw e;
        }
        try {
            transactionManager.lock(id, TransactionManager.getKeyCustomer(customerId), DataObj.WRITE);
        } catch(InvalidTransactionException e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            throw e;
        } catch (TransactionAbortedException e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            abort(id);
            throw e;
        }

        MiddlewareBackup.save(transactionManager);
        return customerId;
    }

    @Override
    public boolean newCustomer(int id, int cid) throws RemoteException, TransactionAbortedException, InvalidTransactionException{
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmRoom == null){
            rmRoom = connectToAResourceManager(serverRoom, 1099, "Group4ResourceManagerRoom");
        }
        if (rmCar == null){
            rmCar = connectToAResourceManager(serverCar, 1099, "Group4ResourceManagerCar");
        }
        if (rmFlight == null){
            rmFlight = connectToAResourceManager(serverFlight, 1099, "Group4ResourceManagerFlight");
        }
        pingServers(id);
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyCustomer(cid), DataObj.WRITE)){
            try{
                result = rmFlight.newCustomer(id, cid) &&
                        rmRoom.newCustomer(id, cid) &&
                        rmCar.newCustomer(id, cid);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return result;
    }

    @Override
    public boolean deleteFlight(int id, int flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmFlight == null){
            rmFlight = connectToAResourceManager(serverFlight, 1099, "Group4ResourceManagerFlight");
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyFlight(flightNum), DataObj.WRITE)){
            try{
                result = rmFlight.deleteFlight(id,flightNum);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmFlight = null;
                abort(id);
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return result;
    }

    @Override
    public boolean deleteCars(int id, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmCar == null){
            rmCar = connectToAResourceManager(serverCar, 1099, "Group4ResourceManagerCar");
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyCar(location), DataObj.WRITE)){
            try{
                result = rmCar.deleteCars(id,location);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmCar = null;
                abort(id);
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return result;
    }

    @Override
    public boolean deleteRooms(int id, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmRoom == null){
            rmRoom = connectToAResourceManager(serverRoom, 1099, "Group4ResourceManagerRoom");
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyRoom(location), DataObj.WRITE)){
            try{
                result = rmRoom.deleteRooms(id,location);

            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmRoom = null;
                abort(id);
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return result;
    }

    @Override
    public boolean deleteCustomer(int id, int customer) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmRoom == null){
            rmRoom = connectToAResourceManager(serverRoom, 1099, "Group4ResourceManagerRoom");
        }
        if (rmCar == null){
            rmCar = connectToAResourceManager(serverCar, 1099, "Group4ResourceManagerCar");
        }
        if (rmFlight == null){
            rmFlight = connectToAResourceManager(serverFlight, 1099, "Group4ResourceManagerFlight");
        }

        List<String> customerObjects = getObjectCustomer(id, customer);
        for(String key: customerObjects){
            transactionManager.lock(id, key, DataObj.WRITE);
        }
        pingServers(id);
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyCustomer(customer), DataObj.WRITE)){
            try{
                result = rmFlight.deleteCustomer(id, customer) &&
                        rmCar.deleteCustomer(id, customer) &&
                        rmRoom.deleteCustomer(id, customer);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return result;
    }

    @Override
    public int queryFlight(int id, int flightNumber) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmFlight == null){
            rmFlight = connectToAResourceManager(serverFlight, 1099, "Group4ResourceManagerFlight");
        }
        int seats = -1;
        if(transactionManager.lock(id, TransactionManager.getKeyFlight(flightNumber), DataObj.READ)){
            try{
                seats=rmFlight.queryFlight(id,flightNumber);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmFlight = null;
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return seats;
    }

    @Override
    public int queryCars(int id, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmCar == null){
            rmCar = connectToAResourceManager(serverCar, 1099, "Group4ResourceManagerCar");
        }
        int numCars = -1;
        if(transactionManager.lock(id, TransactionManager.getKeyCar(location), DataObj.READ)){
            try{
                numCars=rmCar.queryCars(id,location);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmCar = null;
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return numCars;
    }

    @Override
    public int queryRooms(int id, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmRoom == null){
            rmRoom = connectToAResourceManager(serverRoom, 1099, "Group4ResourceManagerRoom");
        }
        int numRooms = -1;
        if(transactionManager.lock(id, TransactionManager.getKeyRoom(location), DataObj.READ)){
            try{
                numRooms=rmRoom.queryRooms(id,location);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmRoom = null;
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return numRooms;
    }

    @Override
    public String queryCustomerInfo(int id, int customer) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmRoom == null){
            rmRoom = connectToAResourceManager(serverRoom, 1099, "Group4ResourceManagerRoom");
        }
        if (rmCar == null){
            rmCar = connectToAResourceManager(serverCar, 1099, "Group4ResourceManagerCar");
        }
        if (rmFlight == null){
            rmFlight = connectToAResourceManager(serverFlight, 1099, "Group4ResourceManagerFlight");
        }
        pingServers(id);
        String bill = "";
        if(transactionManager.lock(id, TransactionManager.getKeyCustomer(customer), DataObj.READ)){
            try{
                bill+=rmFlight.queryCustomerInfo(id, customer);
                String lines[] =rmCar.queryCustomerInfo(id, customer).split("\\r?\\n");
                if (lines.length > 1 ){
                    for(int i=1; i<lines.length; i++){
                        bill += lines[i] + "\n";
                    }
                }
                lines =rmRoom.queryCustomerInfo(id, customer).split("\\r?\\n");
                if (lines.length > 1 ){
                    for(int i=1; i<lines.length; i++){
                        bill += lines[i] + "\n";
                    }
                }
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return bill;
    }

    private List<String> getObjectCustomer(int id, int customer){
        List<String> results = new ArrayList<>();
        try {
            String[] flights = rmFlight.queryCustomerInfo(id, customer).split("\n");
            for(int i=1; i<flights.length; i++){
                String[] data = flights[i].split(" ");
                results.add(data[1]);
            }

            String[] rooms = rmRoom.queryCustomerInfo(id, customer).split("\n");
            for(int i=1; i<rooms.length; i++){
                String[] data = rooms[i].split(" ");
                results.add(data[1]);
            }

            String[] cars = rmCar.queryCustomerInfo(id, customer).split("\n");
            for(int i=1; i<cars.length; i++){
                String[] data = cars[i].split(" ");
                results.add(data[1]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    @Override
    public int queryFlightPrice(int id, int flightNumber) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmFlight == null){
            rmFlight = connectToAResourceManager(serverFlight, 1099, "Group4ResourceManagerFlight");
        }
        int price = -1;
        if(transactionManager.lock(id, TransactionManager.getKeyFlight(flightNumber), DataObj.READ)){
            try{
                price=rmFlight.queryFlightPrice(id, flightNumber);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmFlight = null;
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return price;
    }

    @Override
    public int queryCarsPrice(int id, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmCar == null){
            rmCar = connectToAResourceManager(serverCar, 1099, "Group4ResourceManagerCar");
        }
        int price = -1;
        if(transactionManager.lock(id, TransactionManager.getKeyCar(location), DataObj.READ)){
            try{
                price=rmCar.queryCarsPrice(id,location);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmCar = null;
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return price;
    }

    @Override
    public int queryRoomsPrice(int id, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmRoom == null){
            rmRoom = connectToAResourceManager(serverRoom, 1099, "Group4ResourceManagerRoom");
        }
        int price = -1;
        if(transactionManager.lock(id, TransactionManager.getKeyRoom(location), DataObj.READ)){
            try{
                price=rmRoom.queryRoomsPrice(id,location);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmRoom = null;
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return price;
    }

    @Override
    public boolean reserveFlight(int id, int customer, int flightNumber) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmFlight == null){
            rmFlight = connectToAResourceManager(serverFlight, 1099, "Group4ResourceManagerFlight");
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyFlight(flightNumber), DataObj.WRITE) &&
                transactionManager.lock(id, TransactionManager.getKeyCustomer(customer), DataObj.WRITE)){
            try{
                result = rmFlight.reserveFlight(id,customer,flightNumber);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmFlight = null;
                abort(id);
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return result;
    }

    @Override
    public boolean reserveCar(int id, int customer, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmCar == null){
            rmCar = connectToAResourceManager(serverCar, 1099, "Group4ResourceManagerCar");
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyCar(location), DataObj.WRITE) &&
                transactionManager.lock(id, TransactionManager.getKeyCustomer(customer), DataObj.WRITE)){
            try{
                result = rmCar.reserveCar(id,customer,location);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmCar = null;
                abort(id);
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return result;
    }

    @Override
    public boolean reserveRoom(int id, int customer, String locationd) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)) {
            resetTime(id);
        }
        if (rmRoom == null){
            rmRoom = connectToAResourceManager(serverRoom, 1099, "Group4ResourceManagerRoom");
        }
        boolean result = false;
        if(transactionManager.lock(id, TransactionManager.getKeyRoom(locationd), DataObj.WRITE) &&
                transactionManager.lock(id, TransactionManager.getKeyCustomer(customer), DataObj.WRITE)){
            try{
                result = rmRoom.reserveRoom(id, customer, locationd);
            }
            catch(InvalidTransactionException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                throw e;
            } catch (TransactionAbortedException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                abort(id);
                throw e;
            } catch (RemoteException e){
                System.out.println("EXCEPTION:");
                System.out.println(e.getMessage());
                rmRoom = null;
                abort(id);
                throw e;
            }
        }
        MiddlewareBackup.save(transactionManager);
        return result;
    }


    @Override
    public boolean itinerary(int id, int customer, Vector flightNumbers, String location, boolean Car, boolean Room) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)){
            resetTime(id);
        } // the case where the transaction has been aborted due to Client Timeout is
        //notified to the client by the exception thrown when tries to lock with invalid Xid
        if (rmRoom == null){
            rmRoom = connectToAResourceManager(serverRoom, 1099, "Group4ResourceManagerRoom");
        }
        if (rmCar == null){
            rmCar = connectToAResourceManager(serverCar, 1099, "Group4ResourceManagerCar");
        }
        if (rmFlight == null){
            rmFlight = connectToAResourceManager(serverFlight, 1099, "Group4ResourceManagerFlight");
        }
        pingServers(id);
        ArrayList<Boolean> results = new ArrayList<>();
        //Lock everything
        transactionManager.lock(id, TransactionManager.getKeyCustomer(customer), DataObj.WRITE);
        for (Object flightNumber : flightNumbers) {
            try{
                String flightNumberString = String.valueOf(flightNumber);
                transactionManager.lock(id, TransactionManager.getKeyFlight(Integer.parseInt(flightNumberString)), DataObj.WRITE);
            } catch (InvalidTransactionException e){
                throw e;
            } catch (TransactionAbortedException e){
                abort(id);
                throw e;
            }
        }

        if(Car){
            try{
                transactionManager.lock(id, TransactionManager.getKeyCar(location), DataObj.WRITE);
            } catch (InvalidTransactionException e){
                throw e;
            } catch (TransactionAbortedException e){
                abort(id);
                throw e;
            }
        }

        if(Room){
            try{
                transactionManager.lock(id, TransactionManager.getKeyRoom(location), DataObj.WRITE);
            } catch (InvalidTransactionException e){
                throw e;
            } catch (TransactionAbortedException e){
                abort(id);
                throw e;
            }
        }
        for (Object flightNumber : flightNumbers) {
            String flightNumberString = String.valueOf(flightNumber);
            results.add(rmFlight.reserveFlight(id, customer, Integer.parseInt(flightNumberString)));

        }
        if (Car) {
            results.add(rmCar.reserveCar(id, customer, location));
        }
        if (Room) {
            results.add(rmRoom.reserveRoom(id, customer, location));
        }

        if(results.contains(false)){
            abort(id);
            throw new TransactionAbortedException(id, "Item not available anymore, abort transaction");
        }
        MiddlewareBackup.save(transactionManager);
        return true;

    }

    @Override
    public int start() throws RemoteException {
        checkOldTransactions();
        int idTransaction = transactionManager.start();
        resetTime(idTransaction);
        MiddlewareBackup.save(transactionManager);
        return idTransaction;
    }

    @Override
    public synchronized boolean commit(int id) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        checkOldTransactions();
        if (transactionManager.clientTime.containsKey(id)){
            transactionManager.clientTime.remove(id);
        }
        if (rmRoom == null){
            rmRoom = connectToAResourceManager(serverRoom, 1099, "Group4ResourceManagerRoom");
        }
        if (rmCar == null){
            rmCar = connectToAResourceManager(serverCar, 1099, "Group4ResourceManagerCar");
        }
        if (rmFlight == null){
            rmFlight = connectToAResourceManager(serverFlight, 1099, "Group4ResourceManagerFlight");
        }
        if(!transactionManager.transactions.contains(id)){
            throw new InvalidTransactionException(id, "Transaction "+id+" does not exist");
        }
        System.out.println("\n######### 2PC for transaction "+id+" #########");
        transactionManager.hasCommitted.replace(id, true);
        MiddlewareBackup.save(transactionManager);
        if(crash && errors.get(CRASH_BEFORE_REQUEST)){
            System.exit(1);
        }
        if(transactionManager.votes.get(id).get(RM_ROOM) == null){
            Boolean vote;
            try{
                System.out.println("    Transaction "+id+": Vote request sent to RM Room");
                vote = rmRoom.voteRequest(id);
                transactionManager.votes.get(id).replace(RM_ROOM, vote);
                System.out.println("    Transaction "+id+": RM Room voted "+vote.toString());
            } catch (RemoteException e){
                System.out.println("    Transaction "+id+": RM Room timeout");
                transactionManager.votes.get(id).replace(RM_ROOM, false);
                rmRoom = null;
            }
            MiddlewareBackup.save(transactionManager);
        }

        if(crash && errors.get(CRASH_AFTER_SOME_VOTES)){
            System.exit(1);
        }

        if(transactionManager.votes.get(id).get(RM_FLIGHT) == null){
            Boolean vote;
            try{
                System.out.println("    Transaction "+id+": Vote request sent to RM Flight");
                vote = rmFlight.voteRequest(id);
                transactionManager.votes.get(id).replace(RM_FLIGHT, vote);
                System.out.println("    Transaction "+id+": RM Flight voted "+vote.toString());
            } catch (RemoteException e){
                System.out.println("    Transaction "+id+": RM Flight timeout");
                transactionManager.votes.get(id).replace(RM_FLIGHT, false);
                rmFlight = null;
            }
            MiddlewareBackup.save(transactionManager);
        }

        if(transactionManager.votes.get(id).get(RM_CAR) == null){
            Boolean vote;
            try{
                System.out.println("    Transaction "+id+": Vote request sent to RM Car");
                vote = rmCar.voteRequest(id);
                transactionManager.votes.get(id).replace(RM_CAR, vote);
                System.out.println("    Transaction "+id+": RM Car voted "+vote.toString());
            } catch (RemoteException e){
                System.out.println("    Transaction "+id+": RM Car timeout");
                transactionManager.votes.get(id).replace(RM_CAR, false);
                rmCar = null;
            }
            MiddlewareBackup.save(transactionManager);
        }

        if(crash && errors.get(CRASH_AFTER_VOTES)){
            System.exit(1);
        }

        if(!transactionManager.votes.get(id).get(RM_CAR) ||
                !transactionManager.votes.get(id).get(RM_FLIGHT) ||
                !transactionManager.votes.get(id).get(RM_ROOM)){
            abort(id);
            return false;
        }

        System.out.println("    Commit transaction "+id);
        if(crash && errors.get(CRASH_AFTER_DECIDING)){
            System.exit(1);
        }

        if(transactionManager.decisions.get(id).get(RM_CAR) == null){
            System.out.println("    Transaction "+id+": Send decision to RM Car");
            try {
                rmCar.commit(id);
                transactionManager.decisions.get(id).replace(RM_CAR, true);
            }catch (RemoteException e){
                System.out.println("    Transaction "+id+": RM Car timeout");
                rmCar = null;
            }
            MiddlewareBackup.save(transactionManager);
        }

        if(crash && errors.get(CRASH_AFTER_SOME_DECISIONS)){
            System.exit(1);
        }

        if(transactionManager.decisions.get(id).get(RM_FLIGHT) == null){
            System.out.println("    Transaction "+id+": Send decision to RM Flight");
            try {
                rmFlight.commit(id);
                transactionManager.decisions.get(id).replace(RM_FLIGHT, true);
            }catch (RemoteException e){
                System.out.println("    Transaction "+id+": RM Flight timeout");
                rmFlight = null;
            }
            MiddlewareBackup.save(transactionManager);
        }

        if(transactionManager.decisions.get(id).get(RM_ROOM) == null){
            System.out.println("    Transaction "+id+": Send decision to RM Room");
            try {
                rmRoom.commit(id);
                transactionManager.decisions.get(id).replace(RM_ROOM, true);
            }catch (RemoteException e){
                System.out.println("    Transaction "+id+": RM Room timeout");
                rmRoom = null;
            }
            MiddlewareBackup.save(transactionManager);
        }

        if(crash && errors.get(CRASH_AFTER_DECISIONS)){
            System.exit(1);
        }

        transactionManager.commit(id);


        System.out.println("######### END 2PC for transaction "+id+" #########\n");
        MiddlewareBackup.save(transactionManager);
        return true;

    }

    @Override
    public void abort(int id) throws RemoteException, InvalidTransactionException {
        if (transactionManager.clientTime.containsKey(id)){
            transactionManager.clientTime.remove(id);
        }

        if(!transactionManager.transactions.contains(id)){
            throw new InvalidTransactionException(id, "Transaction "+id+" does not exist");
        }

        System.out.println("    Abort transaction "+id);
        try {
            if(rmRoom != null){
                rmRoom.abort(id);
            }
        }catch (RemoteException e){
        }
        try {
            if(rmCar != null){
                rmCar.abort(id);
            }
        }catch (RemoteException e){
        }
        try {
            if(rmFlight != null){
                rmFlight.abort(id);
            }
        }catch (RemoteException e){
        }
        transactionManager.abort(id);
        MiddlewareBackup.save(transactionManager);
    }

    private void pingServers(int id) throws InvalidTransactionException, RemoteException {
        try{
            rmFlight.queryCars(id,"azerty");
        } catch (RemoteException e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            rmFlight = null;
            throw e;
        }catch (TransactionAbortedException e) {
        }

        try{
            rmRoom.queryCars(id, "azerty");
        } catch (RemoteException e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            rmRoom = null;
            throw e;
        }catch (TransactionAbortedException e) {
        }
        try{
            rmCar.queryCars(id, "azerty");
        } catch (RemoteException e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            rmCar = null;
            throw e;
        }catch (TransactionAbortedException e) {
        }
    }

    @Override
    public boolean shutdown() throws RemoteException {
        checkOldTransactions();
        MiddlewareBackup.save(transactionManager);
        System.out.println("Shutdown");
        if(transactionManager.stillHasTransaction()){
            System.out.println("Some transactions are not closed!");
            return false;
        }
        try{
            rmCar.shutdown();
        } catch(Exception e){
            System.out.println("RM Car shutdown");
        }
        try{
            rmFlight.shutdown();
        } catch(Exception e){
            System.out.println("RM Flight shutdown");
        }

        try{
            rmRoom.shutdown();
        } catch(Exception e){
            System.out.println("RM Room shutdown");
        }

        System.exit(0);
        return true;
    }

    @Override
    public boolean voteRequest(int id) throws RemoteException {
        return false;
    }

    @Override
    public void crash(String location, int error) throws RemoteException {
        if(location.equals("middleware")){
            crash = true;
            if(errors.containsKey(error)){
                errors.replace(error, true);
            }else{
                System.out.println("Error code "+error+" is not valid");
                return;
            }
        }else if(location.equals("rmcar")){
            rmCar.crash(location, error);
        }else if(location.equals("rmflight")){
            rmFlight.crash(location, error);
        }else if(location.equals("rmroom")){
            rmRoom.crash(location, error);
        }else {
            System.out.println("Crash: location "+location+" is unknown");
        }
    }

    public void resetTime(int id) {
        Date date = new Date();
        long timestamp = date.getTime();
        transactionManager.clientTime.put(id, timestamp);
    }

    public synchronized void checkOldTransactions() {
        Date date = new Date();
        long time = date.getTime();
        // create a list of id to remove from ClientTime
        // and remove them after for loop
        //otherwise, raise ConcurrentModificationException
        List<Integer> oldTransactions = new ArrayList<>();
        for (int id : transactionManager.clientTime.keySet()){
            long timestamp = transactionManager.clientTime.get(id);
            if (time - (timestamp + ttl) > 0) {
                oldTransactions.add(id);
            }
        }
        if(!oldTransactions.isEmpty()){
            lockAbort.lock();
            for (int id : oldTransactions){
                try {
                    abort(id);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (InvalidTransactionException e) {
                    e.printStackTrace();
                }
            }
            lockAbort.unlock();
        }

    }



}