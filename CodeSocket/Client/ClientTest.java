import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;


public class ClientTest
{

    private static String serverName = "localhost";
    private static int port = 9000;

    public static void main(String args[]) {
        if(args.length > 0){
            serverName = args[0];
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
        addACustomer(1, 23);
        if(!queryCustomer(2, 23).contains(Integer.toString(23))){
            System.out.println("Test 4 - 3 failed");
        }
        if(addACustomer(1, 23)){
            System.out.println("Test 4 - 4 failed");
        }
        deleteACustomer(1, 23);
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

        //Test 6: itinerary
        customer = addACustomer(1);
        addAFlight(1, 1, 1, 1230);
        addAFlight(1, 2, 10, 879);
        addARoom(1, "Calgary", 3, 55);
        addACar(1, "Calgary", 2, 430);
        Vector flightNumbers = new Vector();
        flightNumbers.add(1);
        flightNumbers.add(2);
        reserveItinerary(1, customer, flightNumbers, "Calgary", true, true);
        if(queryAFlight(1, 1) != 0){
            System.out.println("Test 6 - 1 failed");
        }
        if(queryAFlight(1, 2) != 9){
            System.out.println("Test 6 - 2 failed");
        }
        if(queryARoomLocation(3, "Calgary")!= 2){
            System.out.println("Test 6 - 3 failed");
        }
        if(queryACarLocation(1, "Calgary") != 1){
            System.out.println("Test 6 - 4 failed");
        }

        reserveItinerary(1, customer, flightNumbers, "Calgary", true, true);
        if(queryAFlight(1, 1) != 0){
            System.out.println("Test 6 - 5 failed");
        }
        if(queryAFlight(1, 2) != 9){
            System.out.println("Test 6 - 6 failed");
        }
        if(queryARoomLocation(3, "Calgary")!= 2){
            System.out.println("Test 6 - 7 failed");
        }
        if(queryACarLocation(1, "Calgary") != 1){
            System.out.println("Test 6 - 8 failed");
        }
        System.out.println("Test 6 passed");

        deleteACustomer(1, customer);
        deleteAFlight(1, 1);
        deleteAFlight(1,2);
        deleteARoom(1, "Calgary");
        deleteACar(1, "Calgary");

    }


    private static void addAFlight(int id, int flightNum, int flightSeats, int flightPrice){
        try{
            sendRequestToMiddleware(
                    "addFlight,"+String.valueOf(flightNum)+","+String.valueOf(flightSeats)+","+String.valueOf(flightPrice));
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
            seats = Integer.parseInt(sendRequestToMiddleware(
                    "queryFlight,"+String.valueOf(flightNum)));
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
            price = Integer.parseInt(sendRequestToMiddleware(
                    "queryFlightPrice,"+String.valueOf(flightNum)));
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
            sendRequestToMiddleware(
                    "deleteFlight,"+String.valueOf(flightNum));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addACar(int id, String location, int numCars, int price){
        try{
            sendRequestToMiddleware(
                    "addCars,"+location+","+String.valueOf(numCars)+","+String.valueOf(price));
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
            numCars = Integer.parseInt(sendRequestToMiddleware(
                    "queryCars,"+location));
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
            price = Integer.parseInt(sendRequestToMiddleware(
                    "queryCarsPrice,"+location));
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
            sendRequestToMiddleware(
                    "deleteCars,"+location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addARoom(int id, String location, int numRooms, int price){
        try{
            sendRequestToMiddleware(
                    "addRooms,"+location+","+String.valueOf(numRooms)+","+String.valueOf(price));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteARoom(int id, String location){
        try{
            sendRequestToMiddleware(
                    "deleteRooms,"+location);
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
            numRooms = Integer.parseInt(sendRequestToMiddleware(
                    "queryRooms,"+location));
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
            price = Integer.parseInt(sendRequestToMiddleware(
                    "queryRoomsPrice,"+location));
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
            customerId = Integer.parseInt(sendRequestToMiddleware(
                    "newCustomer"));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return customerId;
    }

    private static boolean addACustomer(int id, int cid){
        boolean result = false;
        try{
            result = Boolean.valueOf(sendRequestToMiddleware(
                    "newCustomer,"+String.valueOf(cid)));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    private static void deleteACustomer(int id, int customer){
        try{
            sendRequestToMiddleware(
                    "deleteCustomer,"+String.valueOf(customer));
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
            bill = sendRequestToMiddleware(
                    "queryCustomerInfo,"+String.valueOf(customer));
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
            sendRequestToMiddleware(
                    "reserveFlight,"+String.valueOf(customer)+","+String.valueOf(flightNum));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void reserveACar(int id, int customer, String location){
        try{
            sendRequestToMiddleware(
                    "reserveCar,"+String.valueOf(customer)+","+location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void reserveARoom(int id, int customer, String location){
        try{
            sendRequestToMiddleware(
                    "reserveRoom,"+String.valueOf(customer)+","+location);
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void reserveItinerary(int id, int customer, Vector flightNumbers, String location, boolean hasCar, boolean hasRoom){
        try{
            String request = "itinerary,"+String.valueOf(customer)+",";
            for(Object flightNumber: flightNumbers){
                request += flightNumber+",";
            }
            request += location +","+ String.valueOf(hasCar)+","+String.valueOf(hasRoom);
            sendRequestToMiddleware(request);
        }catch (Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static String sendRequestToMiddleware(String request){
        PrintWriter outToServer;
        BufferedReader inFromServer;
        String res = "";
        try {
            Socket socket = new Socket(serverName, port);
            outToServer = new PrintWriter(socket.getOutputStream(), true);
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToServer.println(request);
            res = inFromServer.readLine();
        } catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return res;
    }
}
