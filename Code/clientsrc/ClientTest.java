import ResInterface.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RMISecurityManager;

import java.util.*;
import java.io.*;


public class ClientTest
{

    static ResourceManager rm = null;

    public static void main(String args[]) {

        String server = "localhost";
        int port = 1099;
        if (args.length > 0) {
            server = args[0];
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            System.out.println("Usage: java client [rmihost [rmiport]]");
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


        //Test 1: Add and delete a flight
        addAFlight(1, 777, 12, 1000);
        if(queryAFlight(1, 777) != 12) {
            System.out.println("Test 1 - 1 failed");
        }
        if(queryAFlightPrice(1,777) != 1000){
            System.out.println("Test 1 - 2 failed");
        }
        deleteAFlight(1,777);
        if(queryAFlight(1, 777) != 0) {
            System.out.println("Test 1 - 3 failed");
        }
        System.out.println("Test 1 passed");

        //Test 2: Add and delete a car
        addACar(2,"Montreal",2, 125);
        if(queryACarPrice(2, "Montreal") != 125){
            System.out.println("Test 2 - 1 failed");
        }
        if(queryACarLocation(2, "Montreal") != 2){
            System.out.println("Test 2 - 2 failed");
        }
        deleteACar(2, "Montreal");
        if(queryACarLocation(2, "Montreal") != 0){
            System.out.println("Test 2 - 3 failed");
        }
        System.out.println("Test 2 passed");

        //Test 3: Add and delete a room
        addARoom(1, "Toronto",10, 369);
        if(queryARoomPrice(1, "Toronto") != 369){
            System.out.println("Test 3 - 1 failed");
        }
        if(queryARoomLocation(1, "Toronto")!= 10){
            System.out.println("Test 3 - 2 failed");
        }
        deleteARoom(1, "Toronto");
        if(queryARoomLocation(1, "Toronto") != 0){
            System.out.println("Test 3 - 3 failed");
        }
        System.out.println("Test 3 passed");

        //Test 4: Add and remove a customer
        int customer = addACustomer(1);
        if(!queryCustomer(2, customer).contains(Integer.toString(customer))){
            System.out.println("Test 4 - 1 failed");
        }
        deleteACustomer(1, customer);
        System.out.print(queryCustomer(2, customer));
        if(queryCustomer(2, customer).contains(Integer.toString(customer))){
            System.out.println("Test 4 - 2 failed");
        }
        System.out.println("Test 4 passed");

        //Test 5: Add a customer, book a car, a room and flight
        customer = addACustomer(1);
        addACar(1, "Montreal", 2, 50);
        reserveACar(1, customer, "Montreal");
        if(queryACarLocation(1, "Montreal") != 1){
            System.out.println("Test 5 - 1 failed");
        }
        addAFlight(1, 778, 34, 1234);
        reserveAFlight(1, customer, 778);
        if(queryAFlight(1, 778) != 33){
            System.out.println("Test 5 - 2 failed");
        }
        addARoom(1, "Toronto", 5, 55);
        reserveARoom(1, customer, "Toronto");
        if(queryARoomLocation(3, "Toronto")!= 4){
            System.out.println("Test 5 - 3 failed");
        }
        System.out.println(queryCustomer(2, customer));
        deleteACustomer(2, customer);
        if(queryAFlight(1, 778) != 34){
            System.out.println("Test 5 - 4 failed");
        }
        if(queryARoomLocation(3, "Toronto")!= 5){
            System.out.println("Test 5 - 5 failed");
        }
        if(queryACarLocation(1, "Montreal") != 2){
            System.out.println("Test 5 - 6 failed");
        }
        deleteARoom(2, "Toronto");
        deleteACar(2, "Montreal");
        deleteAFlight(3, 778);
        System.out.println("Test 5 passed");

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
}