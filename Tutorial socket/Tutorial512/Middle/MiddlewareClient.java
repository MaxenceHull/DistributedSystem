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
    private ReentrantLock lock1;
    private ReentrantLock lock2;
    private ReentrantLock lock3;
    static String serverFlight = "localhost";
    static String serverRoom = "localhost";
    static String serverCar = "localhost";

    static int portFlight = 9000;
    static int portRoom = 9000;
    static int portCar = 9000;

    public MiddlewareClient (String message, Socket socket, String serverName, int port)
    {
        this.message=message;
        this.serverName=serverName;
        this.socket=socket;
        this.port=port;
    }

    public MiddlewareClient (String message, Socket socket, String serverName, int port, ReentrantLock lock)
    {
        this.message=message;
        this.serverName=serverName;
        this.socket=socket;
        this.port=port;
        this.lock1=lock;
        lock1.lock();
    }

    public MiddlewareClient (String message, Socket socket, String serverName, int port, ReentrantLock lock1, ReentrantLock lock2, ReentrantLock lock3)
    {
        this.message=message;
        this.serverName=serverName;
        this.socket=socket;
        this.port=port;
        this.lock1=lock1;
        this.lock2=lock2;
        this.lock3=lock3;
        lock1.lock();
        lock2.lock();
        lock3.lock();
    }
    public void run ()
    {
        try {
            String [] params = message.split(",");
            if (params[0].equals("queryCustomerInfo"))
            {
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
                outToRoom.println(message);
                outToCar.println(message);

                String res = null;
                String temp;
                while ((temp = inFromFlight.readLine()) != null) {
                    res += temp;
                    System.out.println("received from rmFlight: " + temp); // print the server result to the user
                }
                while ((temp = inFromCar.readLine()) != null) {
                    if (!temp.contains("customer"))
                    {
                        res += temp;
                    }
                    System.out.println("received from rmCar: " + temp); // print the server result to the user
                }
                while ((temp = inFromRoom.readLine()) != null) {
                    if (!temp.contains("customer"))
                    {
                        res += temp;
                    }
                    System.out.println("received from rmRoom: " + temp); // print the server result to the user
                }

                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                outToClient.println(res);

                socket.close();
                flightSocket.close();
                carSocket.close();
                roomSocket.close();

            }
            else if (params[0].equals("newCustomer"))
            {

                Socket flightSocket = new Socket(serverFlight, portFlight);
                Socket roomSocket = new Socket(serverRoom, portRoom);
                Socket carSocket = new Socket(serverCar, portCar);

                PrintWriter outToFlight = new PrintWriter(flightSocket.getOutputStream(), true);
                BufferedReader inFromFlight = new BufferedReader(new InputStreamReader(flightSocket.getInputStream()));
                PrintWriter outToCar = new PrintWriter(carSocket.getOutputStream(), true);
                BufferedReader inFromCar = new BufferedReader(new InputStreamReader(carSocket.getInputStream()));
                PrintWriter outToRoom = new PrintWriter(roomSocket.getOutputStream(), true);
                BufferedReader inFromRoom = new BufferedReader(new InputStreamReader(roomSocket.getInputStream()));

                String cid;
                if (params.length == 1)
                {
                    outToFlight.println(message);
                    String msg = message;
                    msg += ",";
                    cid = inFromFlight.readLine();
                    msg += cid;
                    outToRoom.println(msg);
                    outToCar.println(msg);
                } else
                {
                    outToFlight.println(message);
                    outToCar.println(message);
                    outToRoom.println(message);
                    cid = inFromCar.readLine();
                }

                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                outToClient.println(cid);

                socket.close();
                flightSocket.close();
                carSocket.close();
                roomSocket.close();

                lock1.unlock();
                lock2.unlock();
                lock3.unlock();

            }
            else if (params[0].equals("deleteCustomer"))
            {
                Socket flightSocket = new Socket(serverFlight, portFlight);
                Socket roomSocket = new Socket(serverRoom, portRoom);
                Socket carSocket = new Socket(serverCar, portCar);

                PrintWriter outToFlight = new PrintWriter(flightSocket.getOutputStream(), true);
                BufferedReader inFromFlight = new BufferedReader(new InputStreamReader(flightSocket.getInputStream()));
                PrintWriter outToCar = new PrintWriter(carSocket.getOutputStream(), true);
                BufferedReader inFromCar = new BufferedReader(new InputStreamReader(carSocket.getInputStream()));
                PrintWriter outToRoom = new PrintWriter(roomSocket.getOutputStream(), true);
                BufferedReader inFromRoom = new BufferedReader(new InputStreamReader(roomSocket.getInputStream()));

                outToCar.println(message);
                outToFlight.println(message);
                outToRoom.println(message);

                String res = null;
                String temp;
                while ((temp = inFromFlight.readLine()) != null) {
                    res += temp;
                    System.out.println("received: " + temp); // print the server result to the user
                }
                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                outToClient.println(res);

                socket.close();
                flightSocket.close();
                carSocket.close();
                roomSocket.close();

                lock1.unlock();
                lock2.unlock();
                lock3.unlock();

            }
            else if (params[0].equals("itinerary"))
            {
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
                Vector flightResults = new Vector(params.length-5);
                boolean carResult = true;
                boolean roomResult = true;

                for (int i=0; i<params.length-5; i++) // itinerary,custID, flight1..., flightN, location, car?, room?
                {
                    msg = "reserveFlight,";
                    msg += params[1];
                    msg += ",";
                    msg += params[i+2];
                    outToFlight.println(msg);
                    flightResults.add(Boolean.valueOf(inFromFlight.readLine()));

                }
                if (Boolean.valueOf(params[-1])) // Room
                {
                    msg = "reserveRoom,";
                    msg += params[1];
                    msg += ",";
                    msg += params[-3];
                    outToRoom.println(msg);
                    roomResult = Boolean.valueOf(inFromRoom.readLine());
                }
                if (Boolean.valueOf(params[-2])) // Car
                {
                    msg = "reserveCar,";
                    msg += params[1];
                    msg += ",";
                    msg += params[-3];
                    outToCar.println(msg);
                    carResult = Boolean.valueOf(inFromCar.readLine());
                }
                // Verify if a reservation has not been made
                if (flightResults.contains(false) || !carResult || !roomResult)
                {
                    //Cancel all reservation
                    for (int j=0; j<params.length-5; j++)
                    {
                        msg = "cancelFlight,";
                        msg += params[1];
                        msg += ",";
                        msg += params[j+2];
                        outToFlight.println(msg);
                    }
                    if (Boolean.valueOf(params[-1]))
                    {
                        msg = "cancelRoom,";
                        msg += params[1];
                        msg += ",";
                        msg += params[-3];
                        outToRoom.println(msg);
                    }
                    if (Boolean.valueOf(params[-2]))
                    {
                        msg = "cancelCar,";
                        msg += params[1];
                        msg += ",";
                        msg += params[-3];
                        outToCar.println(msg);
                    }
                    res = "false";
                }

                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                outToClient.println(res);

                socket.close();
                flightSocket.close();
                carSocket.close();
                roomSocket.close();

                lock1.unlock();
                lock2.unlock();
                lock3.unlock();

            }
            else
            {

                Socket rmSocket = new Socket(serverName, port);

                PrintWriter outToServer = new PrintWriter(rmSocket.getOutputStream(), true); // open an output stream to the server...
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(rmSocket.getInputStream())); // open an input stream from the server...


                outToServer.println(message);
                String res = null;
                String temp;
                while ((temp = inFromServer.readLine()) != null) {
                    res += temp;
                    System.out.println("received: " + temp); // print the server result to the user
                }

                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                outToClient.println(res);

                socket.close();
                rmSocket.close();
                lock1.unlock();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
