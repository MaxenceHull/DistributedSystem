import java.net.Socket;

/**
 * Created by pierrehe on 29/09/2017.
 */
public class Middleware {

    String serverFlight = "localhost";
    String serverRoom = "localhost";
    String serverCar = "localhost";

    int portFlight = 9001;
    int portRoom = 9002;
    int portCar = 9003;

    public void addFlight(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverFlight, portFlight).start();
    }

    public void deleteFlight(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverFlight, portFlight).start();
    }

    public void addRooms(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverRoom, portRoom).start();
    }

    public void deleteRooms(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverRoom, portRoom).start();
    }

    public void addCars(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverCar, portCar).start();
    }

    public void deleteCars(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverCar, portCar).start();
    }

    public void queryFlight(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverFlight, portFlight).start();
    }

    public void queryFlightPrice(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverFlight, portFlight).start();
    }

    public void queryRooms(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverRoom, portRoom).start();
    }

    public void queryRoomsPrice(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverRoom, portRoom).start();
    }

    public void queryCars(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverCar, portCar).start();
    }

    public void queryCarsPrice(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverCar, portCar).start();
    }

    public void queryCustomerInfo(String message,Socket socket)
    {
        new MiddlewareClient(message, socket).start();
    }

    public void newCustomer(String message,Socket socket)
    {
        new MiddlewareClient(message, socket).start();
    }

    public void deleteCustomer(String message,Socket socket)
    {
        new MiddlewareClient(message, socket).start();
    }

    public void reserveCar(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverCar, portCar).start();
    }

    public void reserveRoom(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverRoom, portRoom).start();
    }

    public void reserveFlight(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverFlight, portFlight).start();
    }

    public void itinerary(String message,Socket socket)
    {
        new MiddlewareClient(message, socket).start();
    }


}
