// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
//
package MiddlewareImpl;

import ResInterface.*;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public class MiddlewareManagerImpl implements ResourceManager
{
    static ResourceManager rmFlight = null;
    static ResourceManager rmCar = null;
    static ResourceManager rmRoom = null;

    private ReentrantLock lockFlight = new ReentrantLock();
    private ReentrantLock lockRoom = new ReentrantLock();
    private ReentrantLock lockCar = new ReentrantLock();


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
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        boolean result = false;
        try{
            result = rmFlight.addFlight(id,flightNum,flightSeats,flightPrice);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean addCars(int id, String location, int numCars, int price) throws RemoteException {
        boolean result = false;
        lockCar.lock();
        try{
            result = rmCar.addCars(id,location,numCars,price);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            lockCar.unlock();
        }
        return result;
    }

    @Override
    public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException {
        boolean result = false;
        lockRoom.lock();
        try{
            result = rmRoom.addRooms(id,location,numRooms,price);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            lockRoom.unlock();
        }
        return result;
    }

    @Override
    public int newCustomer(int id) throws RemoteException {
        int customerId = -1;
        lockEverything();
        try{
            customerId =rmFlight.newCustomer(id);
            rmRoom.newCustomer(id, customerId);
            rmCar.newCustomer(id, customerId);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            unlockEverything();
        }
        return customerId;
    }

    @Override
    public boolean newCustomer(int id, int cid) throws RemoteException {
        boolean result = false;
        lockEverything();
        try {
            result = rmFlight.newCustomer(id, cid) &&
                    rmRoom.newCustomer(id, cid) &&
                    rmCar.newCustomer(id, cid);
        } catch(Exception e) {
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            unlockEverything();
        }
        return result;
    }

    @Override
    public boolean deleteFlight(int id, int flightNum) throws RemoteException {
        boolean result = false;
        lockFlight.lock();
        try{
            result = rmFlight.deleteFlight(id,flightNum);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            lockFlight.unlock();
        }
        return result;
    }

    @Override
    public boolean deleteCars(int id, String location) throws RemoteException {
        boolean result = false;
        lockCar.lock();
        try{

            result = rmCar.deleteCars(id,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            lockCar.unlock();
        }
        return result;
    }

    @Override
    public boolean deleteRooms(int id, String location) throws RemoteException {
        boolean result = false;
        lockRoom.lock();
        try{
            result = rmRoom.deleteRooms(id,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            lockRoom.unlock();
        }
        return result;
    }

    @Override
    public boolean deleteCustomer(int id, int customer) throws RemoteException {
        boolean result = false;
        lockEverything();
        try{
            result = rmFlight.deleteCustomer(id, customer) &&
                    rmCar.deleteCustomer(id, customer) &&
                    rmRoom.deleteCustomer(id, customer);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }finally {
            unlockEverything();
        }
        return result;
    }

    @Override
    public int queryFlight(int id, int flightNumber) throws RemoteException {
        int seats = -1;
        try{
            seats=rmFlight.queryFlight(id,flightNumber);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return seats;
    }

    @Override
    public int queryCars(int id, String location) throws RemoteException {
        int numCars = -1;
        try{
            numCars=rmCar.queryCars(id,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return numCars;
    }

    @Override
    public int queryRooms(int id, String location) throws RemoteException {
        int numRooms = -1;
        try{
            numRooms=rmRoom.queryRooms(id,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return numRooms;
    }

    @Override
    public String queryCustomerInfo(int id, int customer) throws RemoteException {
        String bill = "";
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
        //If the bill is not empty, then add the header
        return bill;
    }

    @Override
    public int queryFlightPrice(int id, int flightNumber) throws RemoteException {
        int price = -1;
        try{
            price=rmFlight.queryFlightPrice(id, flightNumber);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return price;
    }

    @Override
    public int queryCarsPrice(int id, String location) throws RemoteException {
        int price = -1;
        try{
            price=rmCar.queryCarsPrice(id,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return price;
    }

    @Override
    public int queryRoomsPrice(int id, String location) throws RemoteException {
        int price = -1;
        try{
            price=rmRoom.queryRoomsPrice(id,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return price;
    }

    @Override
    public boolean reserveFlight(int id, int customer, int flightNumber) throws RemoteException {
        boolean result = false;
        lockFlight.lock();
        try{
            result = rmFlight.reserveFlight(id,customer,flightNumber);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }finally {
            lockFlight.unlock();
        }
        return result;
    }

    @Override
    public boolean reserveCar(int id, int customer, String location) throws RemoteException {
        boolean result = false;
        lockCar.lock();
        try{
            result = rmCar.reserveCar(id,customer,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }finally {
            lockCar.unlock();
        }
        return result;
    }

    @Override
    public boolean reserveRoom(int id, int customer, String locationd) throws RemoteException {
        boolean result = false;
        lockRoom.lock();
        try{
            result = rmRoom.reserveRoom(id, customer, locationd);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }finally {
            lockRoom.unlock();
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
    public boolean itinerary(int id, int customer, Vector flightNumbers, String location, boolean Car, boolean Room) throws RemoteException {
        lockEverything();
        try {
            Vector flightResults = new Vector(flightNumbers.size());
            boolean carResult = true, roomResult = true;
            for(Object flightNumber: flightNumbers){
                flightResults.add(rmFlight.reserveFlight(id, customer, (int)flightNumber));
            }
            if(Car){
                carResult = rmCar.reserveCar(id, customer, location);
            }
            if(Room){
                roomResult = rmRoom.reserveRoom(id, customer, location);
            }

            // Verify if a reservation has not been made
            if (flightResults.contains(false) || !carResult || !roomResult) {
                // Cancel all reservations
                for(int i=0; i < flightNumbers.size(); i++){
                    if((boolean)flightResults.get(i)){
                        rmFlight.cancelFlight(id, customer, (int)flightNumbers.get(i));
                    }
                }
                if(Car && carResult){
                    rmCar.cancelCar(id, customer, location);
                }
                if(Room && roomResult){
                    rmRoom.cancelRoom(id, customer, location);
                }

                return false;
            }
            return true;
        } finally {
            unlockEverything();
        }

    }

    private void lockEverything(){
        lockRoom.lock();
        lockCar.lock();
        lockFlight.lock();
    }

    private void unlockEverything(){
        lockRoom.unlock();
        lockCar.unlock();
        lockFlight.unlock();
    }
}