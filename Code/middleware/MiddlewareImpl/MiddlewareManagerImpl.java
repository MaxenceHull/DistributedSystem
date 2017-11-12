// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
//
package MiddlewareImpl;

import LockManager.DataObj;
import LockManager.TimeObj;
import ResInterface.*;
import TransactionManager.TransactionManager;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

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


    private HashMap<Integer, Stack<Action>> actions;
    private HashMap<Integer, Boolean> isRollback;

    public MiddlewareManagerImpl() throws RemoteException {
        actions = new HashMap<>();
        isRollback = new HashMap<>();
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
                int oldPrice = rmFlight.queryFlightPrice(id, flightNum);
                int oldSeats = rmFlight.queryFlight(id, flightNum);
                result = rmFlight.addFlight(id,flightNum,flightSeats,flightPrice);
                if(result && !isRollback.get(id)){
                    if(oldPrice == 0 && oldSeats == 0){
                        Object[] parameters = new Object[2];
                        parameters[0] = id;
                        parameters[1] = flightNum;
                        Action action = new Action(
                                this.getClass().getMethod("deleteFlight", int.class, int.class),
                                parameters);
                        actions.get(id).push(action);
                    } else {
                        Object[] parameters = new Object[4];
                        parameters[0] = id;
                        parameters[1] = flightNum;
                        parameters[2] = -flightSeats;
                        parameters[3] = oldPrice;
                        Action action = new Action(
                                this.getClass().getMethod("addFlight", int.class, int.class, int.class, int.class),
                                parameters);
                        actions.get(id).push(action);
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
            } catch (NoSuchMethodException e){

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
                int oldPrice = rmCar.queryCarsPrice(id, location);
                int oldCars = rmCar.queryCars(id, location);
                result = rmCar.addCars(id,location,numCars,price);
                if(result && !isRollback.get(id)){
                    if(oldPrice == 0 && oldCars == 0){
                        Object[] parameters = new Object[2];
                        parameters[0] = id;
                        parameters[1] = location;
                        Action action = new Action(
                                this.getClass().getMethod("deleteCars", int.class, String.class),
                                parameters);
                        actions.get(id).push(action);
                    } else {
                        Object[] parameters = new Object[4];
                        parameters[0] = id;
                        parameters[1] = location;
                        parameters[2] = -numCars;
                        parameters[3] = oldPrice;
                        Action action = new Action(
                                this.getClass().getMethod("addCars", int.class, String.class, int.class, int.class),
                                parameters);
                        actions.get(id).push(action);

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
            } catch (NoSuchMethodException e){

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
                int oldPrice = rmRoom.queryRoomsPrice(id, location);
                int oldRooms = rmRoom.queryRooms(id, location);
                result = rmRoom.addRooms(id,location,numRooms,price);
                if(result && !isRollback.get(id)){
                    if(oldPrice == 0 && oldRooms == 0){
                        Object[] parameters = new Object[2];
                        parameters[0] = id;
                        parameters[1] = location;
                        Action action = new Action(
                                this.getClass().getMethod("deleteRooms", int.class, String.class),
                                parameters);
                        actions.get(id).push(action);

                    } else {
                        Object[] parameters = new Object[4];
                        parameters[0] = id;
                        parameters[1] = location;
                        parameters[2] = -numRooms;
                        parameters[3] = oldPrice;
                        Action action = new Action(
                                this.getClass().getMethod("addRooms", int.class, String.class, int.class, int.class),
                                parameters);
                        actions.get(id).push(action);
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
            } catch (NoSuchMethodException e){

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

        if(customerId != -1 && !isRollback.get(id)){
            Object[] parameters = new Object[2];
            parameters[0] = id;
            parameters[1] = customerId;
            Action action = null;
            try {
                action = new Action(
                        this.getClass().getMethod("deleteCustomer", int.class, int.class),
                        parameters);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            actions.get(id).push(action);
        }

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
                if(result && !isRollback.get(id)){
                    Object[] parameters = new Object[2];
                    parameters[0] = id;
                    parameters[1] = cid;
                    Action action = new Action(
                            this.getClass().getMethod("deleteCustomer", int.class, int.class),
                            parameters);
                    actions.get(id).push(action);

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
            } catch (NoSuchMethodException e){

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
                int oldSeats = queryFlight(id,flightNum);
                int oldPrice = queryFlightPrice(id, flightNum);
                result = rmFlight.deleteFlight(id,flightNum);
                if(result && !isRollback.get(id)){
                    Object[] parameters = new Object[4];
                    parameters[0] = id;
                    parameters[1] = flightNum;
                    parameters[2] = oldSeats;
                    parameters[3] = oldPrice;
                    Action action = new Action(
                            this.getClass().getMethod("addFlight", int.class, int.class, int.class, int.class),
                            parameters);
                    actions.get(id).push(action);

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
            } catch (NoSuchMethodException e){

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
                int oldCars = queryCars(id, location);
                int oldPrice = queryCarsPrice(id, location);
                result = rmCar.deleteCars(id,location);
                if(result && !isRollback.get(id)){
                    Object[] parameters = new Object[4];
                    parameters[0] = id;
                    parameters[1] = location;
                    parameters[2] = oldCars;
                    parameters[3] = oldPrice;
                    Action action = new Action(
                            this.getClass().getMethod("addCars", int.class, String.class, int.class, int.class),
                            parameters);
                    actions.get(id).push(action);

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
            } catch (NoSuchMethodException e){

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
                int oldRooms = queryRooms(id, location);
                int oldPrice = queryRoomsPrice(id, location);
                result = rmRoom.deleteRooms(id,location);
                if(result && !isRollback.get(id)){
                    Object[] parameters = new Object[4];
                    parameters[0] = id;
                    parameters[1] = location;
                    parameters[2] = oldRooms;
                    parameters[3] = oldPrice;
                    Action action = new Action(
                            this.getClass().getMethod("addRooms", int.class, String.class, int.class, int.class),
                            parameters);
                    actions.get(id).push(action);

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
            } catch (NoSuchMethodException e){

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
                if(!isRollback.get(id)){
                    prepareRollbackCustomer(id, customer);
                }
                result = rmFlight.deleteCustomer(id, customer) &&
                        rmCar.deleteCustomer(id, customer) &&
                        rmRoom.deleteCustomer(id, customer);
                if(result && !isRollback.get(id)){
                    Object[] parameters = new Object[2];
                    parameters[0] = id;
                    parameters[1] = customer;
                    Action action = new Action(
                            this.getClass().getMethod("newCustomer", int.class, int.class),
                            parameters);
                    actions.get(id).push(action);

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
            } catch (NoSuchMethodException e){

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
        return bill;
    }

    private void prepareRollbackCustomer(int id, int customer){
        try {
            String[] flights = rmFlight.queryCustomerInfo(id, customer).split("\n");
            for(int i=1; i<flights.length; i++){
                String[] data = flights[i].split(" ");
                int flightNumber = Integer.parseInt(data[1].split("-")[1]);
                Object[] parameters = new Object[3];
                parameters[0] = id;
                parameters[1] = customer;
                parameters[2] = flightNumber;
                Action action = new Action(
                        this.getClass().getMethod("reserveFlight", int.class, int.class, int.class),
                        parameters);
                actions.get(id).push(action);
                System.out.println(action.method.toString());
            }

            String[] rooms = rmRoom.queryCustomerInfo(id, customer).split("\n");
            for(int i=1; i<rooms.length; i++){
                String[] data = rooms[i].split(" ");
                String location = data[1].split("-")[1];
                Object[] parameters = new Object[3];
                parameters[0] = id;
                parameters[1] = customer;
                parameters[2] = location;
                Action action = new Action(
                        this.getClass().getMethod("reserveRoom", int.class, int.class, String.class),
                        parameters);
                actions.get(id).push(action);
                System.out.println(action.method.toString());
            }

            String[] cars = rmCar.queryCustomerInfo(id, customer).split("\n");
            for(int i=1; i<cars.length; i++){
                String[] data = cars[i].split(" ");
                String location = data[1].split("-")[1];
                Object[] parameters = new Object[3];
                parameters[0] = id;
                parameters[1] = customer;
                parameters[2] = location;
                Action action = new Action(
                        this.getClass().getMethod("reserveCar", int.class, int.class, String.class),
                        parameters);
                actions.get(id).push(action);
                System.out.println(action.method.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
                if(result && !isRollback.get(id)){
                    Object[] parameters = new Object[3];
                    parameters[0] = id;
                    parameters[1] = customer;
                    parameters[2] = flightNumber;
                    Action action = new Action(
                            this.getClass().getMethod("cancelFlight", int.class, int.class, int.class),
                            parameters);
                    actions.get(id).push(action);

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
            } catch (NoSuchMethodException e){

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
                if(result && !isRollback.get(id)){
                    Object[] parameters = new Object[3];
                    parameters[0] = id;
                    parameters[1] = customer;
                    parameters[2] = location;
                    Action action = new Action(
                            this.getClass().getMethod("cancelCar", int.class, int.class, String.class),
                            parameters);
                    actions.get(id).push(action);

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
            } catch (NoSuchMethodException e){

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
                if(result && !isRollback.get(id)){
                    Object[] parameters = new Object[3];
                    parameters[0] = id;
                    parameters[1] = customer;
                    parameters[2] = locationd;
                    Action action = new Action(
                            this.getClass().getMethod("cancelRoom", int.class, int.class, String.class),
                            parameters);
                    actions.get(id).push(action);

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
            } catch (NoSuchMethodException e){

            }
        }
        return result;
    }

    @Override
    public boolean cancelRoom(int id, int customer, String location) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        return rmRoom.cancelRoom(id, customer, location);
        //return false;
    }

    @Override
    public boolean cancelFlight(int id, int customer, int flightNumber) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        return rmFlight.cancelFlight(id, customer, flightNumber);
    }

    @Override
    public boolean cancelCar(int id, int customer, String location) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        return rmCar.cancelCar(id, customer, location);
    }

    @Override
    public boolean itinerary(int id, int customer, Vector flightNumbers, String location, boolean Car, boolean Room) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        checkOldTransactions();
        if (clientTime.containsKey(id)){
            resetTime(id);
        } // the case where the transaction has been aborted due to Client Timeout is
        //notified to the client by the exception thrown when tries to lock with invalid Xid

        //Lock everything
        for (Object flightNumber : flightNumbers) {
            try{
                transactionManager.lock(id, TransactionManager.getKeyFlight((int)flightNumber), DataObj.WRITE);
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
        try{
            for (Object flightNumber : flightNumbers) {
                String flightNumberString = String.valueOf(flightNumber);
                if (rmFlight.reserveFlight(id, customer, Integer.parseInt(flightNumberString)) && !isRollback.get(id)) {
                    Object[] parameters = new Object[3];
                    parameters[0] = id;
                    parameters[1] = customer;
                    parameters[2] = Integer.parseInt(flightNumberString);
                    Action action = new Action(
                            this.getClass().getMethod("cancelFlight", int.class, int.class, int.class),
                            parameters);
                    actions.get(id).push(action);
                }

            }
            if (Car) {
                if (rmCar.reserveCar(id, customer, location) && !isRollback.get(id)) {
                    Object[] parameters = new Object[3];
                    parameters[0] = id;
                    parameters[1] = customer;
                    parameters[2] = location;
                    Action action = new Action(
                            this.getClass().getMethod("cancelCar", int.class, int.class, String.class),
                            parameters);
                    actions.get(id).push(action);
                }
            }
            if (Room) {
                if (rmRoom.reserveRoom(id, customer, location) && !isRollback.get(id)) {
                    Object[] parameters = new Object[3];
                    parameters[0] = id;
                    parameters[1] = customer;
                    parameters[2] = location;
                    Action action = new Action(
                            this.getClass().getMethod("cancelRoom", int.class, int.class, String.class),
                            parameters);
                    actions.get(id).push(action);
                }
            }
        }catch (NoSuchMethodException e){

        }

        return true;

    }

    @Override
    public int start() throws RemoteException {
        checkOldTransactions();
        int idTransaction = transactionManager.start();
        actions.put(idTransaction, new Stack<>());
        isRollback.put(idTransaction, false);
        resetTime(id);
        return idTransaction;
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
            clientTime.remove(id);
        }
        
        isRollback.replace(id, true);
        System.out.println("Rollback for transaction: "+id);
        while (!actions.get(id).empty()){
            Action action = actions.get(id).pop();
            System.out.println("    "+action.method.toString());
            try {
                action.method.invoke(this, action.parameters);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        transactionManager.abort(id);
    }

    @Override
    public void shutdown() throws RemoteException {
        if(transactionManager.stillHasTransaction()){
            return;
        }

        rmCar.shutdown();
        rmFlight.shutdown();
        rmRoom.shutdown();
        System.exit(0);
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