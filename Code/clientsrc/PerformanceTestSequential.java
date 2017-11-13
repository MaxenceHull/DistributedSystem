import ResInterface.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.*;


public class PerformanceTestSequential {

    static ResourceManager rm = null;
    static Random rand = null;

    public static void main(String args[]) {
        int _case = 0;
        String server = "localhost";
        int port = 1099;
        if (args.length > 0) {
            server = args[0];
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            _case = Integer.parseInt(args[1]);
        }
        if (args.length > 3) {
            System.out.println("Usage: java client [rmihost] [rmiport] [oneRM?]]");
            System.exit(1);
        }

        try {
            // get a reference to the rmiregistry
            System.out.println(server);
            System.out.println(port);
            Registry registry = LocateRegistry.getRegistry(server, port);
            // get the proxy and the remote reference by rmiregistry lookup
            rm = (ResourceManager) registry.lookup("Group4MiddlewareManager");
            if (rm != null) {
                System.out.println("Successful");
                System.out.println("Connected to RM");
            } else {
                System.out.println("Unsuccessful");
            }
            // make call on remote method
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }


        if (System.getSecurityManager() == null) {
            //System.setSecurityManager(new RMISecurityManager());
        }

        rand = new Random();

        long sum = 0;
        int inc = 0;
        switch (_case){
            case 0:
                while (true){
                    int choice = rand.nextInt(10);
                    long timestamp = System.currentTimeMillis();
                    inc++;
                    try{
                        if(choice < 2){
                            transaction_1();
                        }else{
                            transaction_2();
                        }
                    }catch (Exception e){

                    }
                    timestamp = System.currentTimeMillis() - timestamp;
                    sum = sum + timestamp;
                    if(inc % 50000 == 0){
                        System.out.println(sum/inc);
                    }
                }

            case 1:
                while (true){
                    int choice = rand.nextInt(10);
                    long timestamp = System.currentTimeMillis();
                    inc++;
                    try{
                        if(choice < 2){
                            transaction_4();
                        }else{
                            transaction_3();
                        }
                    }catch (Exception e){

                    }
                    timestamp = System.currentTimeMillis() - timestamp;
                    sum = sum + timestamp;
                    if(inc % 50000 == 0){
                        System.out.println(sum/inc);
                    }
                }

        }

    }

    static List<Integer> flights = new ArrayList<Integer>() {{
        add(777);
        add(888);
        add(999);
        add(111);
    }};

    static List<String> rooms = new ArrayList<String>(){{
        add("Rome");
        add("Paris");
        add("Montreal");
        add("Chicago");
    }};
    static List<String> cars = new ArrayList<String>(){{
        add("Rome");
        add("Paris");
        add("Montreal");
        add("Chicago");
    }};

    static List<Integer> customers = new ArrayList<Integer>(){{
        add(20);
        add(30);
        add(40);
        add(50);
    }};

    // 3 writes, 3RM
    private static void transaction_1() throws TransactionAbortedException, RemoteException, InvalidTransactionException {
        int indexFlight = rand.nextInt(flights.size());
        int indexRoom = rand.nextInt(rooms.size());
        int indexCar = rand.nextInt(cars.size());
        int indexCustomer = rand.nextInt(customers.size());

        int idTransaction = rm.start();
        reserveAFlight(idTransaction, customers.get(indexCustomer), flights.get(indexFlight));
        reserveACar(idTransaction, customers.get(indexCustomer), cars.get(indexCar));
        reserveARoom(idTransaction, customers.get(indexCustomer), rooms.get(indexRoom));
        rm.commit(idTransaction);
    }

    // 3 reads, 3 RM
    private static void transaction_2() throws TransactionAbortedException, RemoteException, InvalidTransactionException {
        int indexFlight = rand.nextInt(flights.size());
        int indexRoom = rand.nextInt(rooms.size());
        int indexCar = rand.nextInt(cars.size());
        int idTransaction = rm.start();
        queryAFlight(idTransaction, flights.get(indexFlight));
        queryACarPrice(idTransaction, cars.get(indexCar));
        queryARoomLocation(idTransaction, rooms.get(indexRoom));
        rm.commit(idTransaction);
    }

    //3 Reads, 1 RM
    private static void transaction_3() throws TransactionAbortedException, RemoteException, InvalidTransactionException {
        int idTransaction = rm.start();
        for(int i=0; i < 3; i++){
            int indexFlight = rand.nextInt(flights.size());
            queryAFlight(idTransaction, flights.get(indexFlight));
        }
        rm.commit(idTransaction);
    }

    // 3 writes, 1 rm
    private static void transaction_4() throws TransactionAbortedException, RemoteException, InvalidTransactionException {
        int idTransaction = rm.start();
        int choice = rand.nextInt(3);
        for(int i=0; i< 3; i++){
            if(choice == 0){
                int indexFlight = rand.nextInt(flights.size());
                addAFlight(idTransaction, flights.get(indexFlight), 200, 1000);
            }else if(choice == 1){
                int indexRoom = rand.nextInt(rooms.size());
                addARoom(idTransaction, rooms.get(indexRoom), 30,3444);
            } else if(choice == 2){
                int indexCar = rand.nextInt(cars.size());
                addACar(idTransaction, cars.get(indexCar), 344, 785);
            }
        }

        rm.commit(idTransaction);
    }


    private static void addAFlight(int id, int flightNum, int flightSeats, int flightPrice){
        try{
            rm.addFlight(id,flightNum,flightSeats,flightPrice);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static int queryAFlight(int id, int flightNum){
        int seats = -1;
        try{
            seats=rm.queryFlight(id,flightNum);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return seats;
    }

    private static int queryAFlightPrice(int id, int flightNum){
        int price = -1;
        try{
            price=rm.queryFlightPrice(id,flightNum);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return price;
    }

    private static void deleteAFlight(int id, int flightNum){
        try{
            rm.deleteFlight(id,flightNum);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addACar(int id, String location, int numCars, int price){
        try{
            rm.addCars(id,location,numCars,price);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static int queryACarLocation(int id, String location){
        int numCars = -1;
        try{
            numCars=rm.queryCars(id,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return numCars;
    }

    private static int queryACarPrice(int id, String location){
        int price = -1;
        try{
            price=rm.queryCarsPrice(id,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return price;
    }

    private static void deleteACar(int id, String location){
        try{

            rm.deleteCars(id,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addARoom(int id, String location, int numRooms, int price){
        try{
            rm.addRooms(id,location,numRooms,price);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteARoom(int id, String location){
        try{
            rm.deleteRooms(id,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static int queryARoomLocation(int id, String location) {
        int numRooms = -1;
        try{
            numRooms=rm.queryRooms(id,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return numRooms;
    }

    private static int queryARoomPrice(int id, String location){
        int price = -1;
        try{
            price=rm.queryRoomsPrice(id,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return price;
    }

    private static int addACustomer(int id){
        int customerId = -1;
        try{
            customerId =rm.newCustomer(id);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return customerId;
    }

    private static void deleteACustomer(int id, int customer){
        try{
            rm.deleteCustomer(id, customer);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static String queryCustomer(int id, int customer){
        String bill = "";
        try{
            bill=rm.queryCustomerInfo(id, customer);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return bill;
    }

    private static void reserveAFlight(int id, int customer, int flightNum){
        try{
            rm.reserveFlight(id,customer,flightNum);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void reserveACar(int id, int customer, String location){
        try{
            rm.reserveCar(id,customer,location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void reserveARoom(int id, int customer, String location){
        try{
            rm.reserveRoom(id, customer, location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void reserveItinerary(int id, int customer, Vector flightNumbers, String location, boolean hasCar, boolean hasRoom){
        try{
            rm.itinerary(id, customer, flightNumbers, location, hasCar, hasRoom);
        }catch (Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}