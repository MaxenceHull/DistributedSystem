import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by pierrehe on 29/09/2017.
 */
public class MiddlewareServer {

    protected Middleware middleware = new Middleware();

    public static void main(String args[])
    {

        MiddlewareServer  server = new MiddlewareServer();
        try
        {
            server.forward();
        }
        catch (IOException e)
        {

        }
    }

    public void forward() throws IOException {
        ServerSocket serverSocket = new ServerSocket(9000);
        while (true) {

            Socket socket = serverSocket.accept();
            try {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = inFromClient.readLine();

                    System.out.println("message:" + message);
                    String[] params = message.split(",");
                    if (params[0].equals("addFlight")) {
                        middleware.addFlight(message, socket);
                    } else if (params[0].equals("deleteFlight")) {
                        middleware.deleteFlight(message, socket);
                    } else if (params[0].equals("addRooms")) {
                        middleware.addRooms(message, socket);
                    } else if (params[0].equals("deleteRooms")) {
                        middleware.deleteRooms(message, socket);
                    } else if (params[0].equals("addCars")) {
                        middleware.addCars(message, socket);
                    } else if (params[0].equals("deleteCars")) {
                        middleware.deleteCars(message, socket);
                    } else if (params[0].equals("queryFlight")) {
                        middleware.queryFlight(message, socket);
                    } else if (params[0].equals("queryFlightPrice")) {
                        middleware.queryFlightPrice(message, socket);
                    } else if (params[0].equals("queryRooms")) {
                        middleware.queryRooms(message, socket);
                    } else if (params[0].equals("queryRoomsPrice")) {
                        middleware.queryRoomsPrice(message, socket);
                    } else if (params[0].equals("queryCars")) {
                        middleware.queryCars(message, socket);
                    } else if (params[0].equals("queryCarsPrice")) {
                        middleware.queryCarsPrice(message, socket);
                    } else if (params[0].equals("queryCustomerInfo")) {
                        middleware.queryCustomerInfo(message, socket);
                    } else if (params[0].equals("newCustomer")) {
                        middleware.newCustomer(message,socket);
                    } else if (params[0].equals("deleteCustomer")) {
                        middleware.deleteCustomer(message, socket);
                    } else if (params[0].equals("reserveCar")) {
                        middleware.reserveCar(message, socket);
                    } else if (params[0].equals("reserveRoom")) {
                        middleware.reserveRoom(message, socket);
                    } else if (params[0].equals("reserveFlight")) {
                        middleware.reserveFlight(message, socket);
                    } else if (params[0].equals("itinerary")) {
                        middleware.itinerary(message, socket);
                    } else {
                        //Function unknown
                        message = "Error: Function unknown";
                    }


                System.out.println("Request done:" + message);
            } catch (IOException e) {

            }


        }
    }
}
