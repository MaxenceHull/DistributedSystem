import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by pierrehe on 29/09/2017.
 */
public class MiddlewareClient extends Thread {
    private String message;
    private Socket socket;
    private String serverName;
    private int port;

    static private ReentrantLock flightLock = new ReentrantLock();
    static private ReentrantLock roomLock = new ReentrantLock();
    static private ReentrantLock carLock = new ReentrantLock();

    static String serverFlight = "localhost";
    static String serverRoom = "localhost";
    static String serverCar = "localhost";

    static int portFlight = 9001;
    static int portRoom = 9002;
    static int portCar = 9003;



    public MiddlewareClient (String message, Socket socket, String serverName, int port)
    {
        this.message=message;
        this.serverName=serverName;
        this.socket=socket;
        this.port=port;
    }

    public MiddlewareClient (String message, Socket socket)
    {
        this.message=message;
        this.socket=socket;

    }
    public void run ()
    {
        try {
            String [] params = message.split(",");
            if (params[0].equals("queryCustomerInfo"))
            {
                System.out.println("queryCustomerInfo: "+message);
                Socket flightSocket = new Socket(serverFlight, portFlight);
                Socket roomSocket = new Socket(serverRoom, portRoom);
                Socket carSocket = new Socket(serverCar, portCar);

                PrintWriter outToFlight = new PrintWriter(flightSocket.getOutputStream(), true);
                BufferedReader inFromFlight = new BufferedReader(new InputStreamReader(flightSocket.getInputStream()));
                PrintWriter outToCar = new PrintWriter(carSocket.getOutputStream(), true);
                BufferedReader inFromCar = new BufferedReader(new InputStreamReader(carSocket.getInputStream()));
                PrintWriter outToRoom = new PrintWriter(roomSocket.getOutputStream(), true);
                BufferedReader inFromRoom = new BufferedReader(new InputStreamReader(roomSocket.getInputStream()));

                outToFlight.println(message);
                String line;
                String bill = inFromFlight.readLine();
                while (inFromFlight.ready() && (line = inFromFlight.readLine()) != null) {
                    bill += line;
                }

                outToRoom.println(message);
                inFromRoom.readLine();
                while (inFromRoom.ready() && (line = inFromRoom.readLine()) != null) {
                    bill += line;
                }

                outToCar.println(message);
                inFromCar.readLine();
                while (inFromCar.ready() && (line = inFromCar.readLine()) != null) {
                    bill += line;
                }

                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                outToClient.println(bill);

                socket.close();
                flightSocket.close();
                carSocket.close();
                roomSocket.close();

            }
            else if (params[0].equals("newCustomer"))
            {
                System.out.println("Received request: new customer with "+String.valueOf(params.length)+" args");
                lockEverything();
                Socket flightSocket = new Socket(serverFlight, portFlight);
                Socket roomSocket = new Socket(serverRoom, portRoom);
                Socket carSocket = new Socket(serverCar, portCar);

                PrintWriter outToFlight = new PrintWriter(flightSocket.getOutputStream(), true);
                BufferedReader inFromFlight = new BufferedReader(new InputStreamReader(flightSocket.getInputStream()));
                PrintWriter outToCar = new PrintWriter(carSocket.getOutputStream(), true);
                PrintWriter outToRoom = new PrintWriter(roomSocket.getOutputStream(), true);
                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);

                String cid;
                if (params.length == 1)
                {
                    System.out.println("Creating a new cid: "+message);
                    outToFlight.println(message);
                    cid = inFromFlight.readLine();
                    System.out.println("Cid: "+cid);
                    message+= ","+cid;
                    //Send request with the cid given by the first RM
                    outToRoom.println(message);
                    outToCar.println(message);
                    outToClient.println(cid);
                } else
                {
                    outToFlight.println(message);
                    boolean flightRes = Boolean.valueOf(inFromFlight.readLine());
                    if(flightRes){
                        outToCar.println(message);
                        outToRoom.println(message);
                        outToClient.println("true");
                    } else {
                        outToClient.println("false");
                    }
                }

                socket.close();
                flightSocket.close();
                carSocket.close();
                roomSocket.close();

                unlockEverything();
            }
            else if (params[0].equals("deleteCustomer"))
            {
                System.out.println("Received request: delete customer");
                lockEverything();
                Socket flightSocket = new Socket(serverFlight, portFlight);
                Socket roomSocket = new Socket(serverRoom, portRoom);
                Socket carSocket = new Socket(serverCar, portCar);

                PrintWriter outToFlight = new PrintWriter(flightSocket.getOutputStream(), true);
                PrintWriter outToCar = new PrintWriter(carSocket.getOutputStream(), true);
                BufferedReader inFromCar = new BufferedReader(new InputStreamReader(carSocket.getInputStream()));
                PrintWriter outToRoom = new PrintWriter(roomSocket.getOutputStream(), true);
                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);

                outToCar.println(message);
                if(Boolean.valueOf(inFromCar.readLine())){
                    outToFlight.println(message);
                    outToRoom.println(message);
                    outToClient.println("true");
                } else {
                    outToClient.println("false");
                }

                socket.close();
                flightSocket.close();
                carSocket.close();
                roomSocket.close();

                unlockEverything();

            }
            else if (params[0].equals("itinerary"))
            {
                System.out.println("itinerary");
                lockEverything();
                Socket flightSocket = new Socket(serverFlight, portFlight);
                Socket roomSocket = new Socket(serverRoom, portRoom);
                Socket carSocket = new Socket(serverCar, portCar);

                PrintWriter outToFlight = new PrintWriter(flightSocket.getOutputStream(), true);
                BufferedReader inFromFlight = new BufferedReader(new InputStreamReader(flightSocket.getInputStream()));
                PrintWriter outToCar = new PrintWriter(carSocket.getOutputStream(), true);
                BufferedReader inFromCar = new BufferedReader(new InputStreamReader(carSocket.getInputStream()));
                PrintWriter outToRoom = new PrintWriter(roomSocket.getOutputStream(), true);
                BufferedReader inFromRoom = new BufferedReader(new InputStreamReader(roomSocket.getInputStream()));

                String msg;
                String res = "true";
                Vector flightResults = new Vector();
                flightResults.add(new Object());
                flightResults.add(new Object());
                boolean carResult = true;
                boolean roomResult = true;

                for (int i=2; i<params.length-3; i++) // itinerary,custID, flight1..., flightN, location, car?, room?
                {
                    msg = "reserveFlight,"+params[1]+","+params[i];
                    System.out.println(msg);
                    outToFlight.println(msg);
                    flightResults.add(Boolean.valueOf(inFromFlight.readLine()));
                }
                if (Boolean.valueOf(params[params.length-1])) // Room
                {
                    msg = "reserveRoom,"+params[1]+","+params[params.length-3];
                    outToRoom.println(msg);
                    roomResult = Boolean.valueOf(inFromRoom.readLine());
                }
                if (Boolean.valueOf(params[params.length-2])) // Car
                {
                    msg = "reserveCar,"+params[1]+","+params[params.length-3];
                    outToCar.println(msg);
                    carResult = Boolean.valueOf(inFromCar.readLine());
                }
                // Verify if a reservation has not been made
                if (flightResults.contains(false) || !carResult || !roomResult)
                {

                    //Cancel all reservations
                    for (int j=2; j<flightResults.size(); j++)
                    {
                        if((boolean)flightResults.get(j)){
                            msg = "cancelFlight,"+params[1]+","+params[j];
                            outToFlight.println(msg);
                        }
                    }
                    if (Boolean.valueOf(params[params.length-1]))
                    {
                        if(roomResult){
                            msg = "cancelRoom,"+params[1]+","+params[params.length-3];
                            outToRoom.println(msg);
                        }
                    }
                    if (Boolean.valueOf(params[params.length-2]))
                    {
                        if(carResult){
                            msg = "cancelCar,"+params[1]+","+params[params.length-3];
                            outToCar.println(msg);
                        }
                    }
                    res = "false";
                }

                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                outToClient.println(res);

                socket.close();
                flightSocket.close();
                carSocket.close();
                roomSocket.close();

                unlockEverything();

            }
            else
            {
                System.out.println("Middleware thread created for function "+params[0]);
                lock(params[0]);
                Socket rmSocket = new Socket(serverName, port);

                PrintWriter outToServer = new PrintWriter(rmSocket.getOutputStream(), true); // open an output stream to the server...
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(rmSocket.getInputStream())); // open an input stream from the server...

                outToServer.println(message);
                String res = inFromServer.readLine();
                System.out.println("received(final): " + res);
                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                outToClient.println(res);

                socket.close();
                rmSocket.close();
                unlock(params[0]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void lock(String function){
        if (function.contains("flight")){
            flightLock.lock();
        } else if(function.contains("car")){
            carLock.lock();
        } else if(function.contains("room")){
            roomLock.lock();
        }
    }

    private static void unlock(String function){
        if (function.contains("flight")){
            flightLock.unlock();
        } else if(function.contains("car")){
            carLock.unlock();
        } else if(function.contains("room")){
            roomLock.unlock();
        }
    }

    private static void lockEverything(){
        flightLock.lock();
        roomLock.lock();
        carLock.lock();
    }

    private static void unlockEverything(){
        roomLock.unlock();
        flightLock.unlock();
        carLock.unlock();
    }
}
