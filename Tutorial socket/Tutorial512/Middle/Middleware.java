import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by pierrehe on 29/09/2017.
 */
public class Middleware {

    private ReentrantLock lockFlight = new ReentrantLock();
    private ReentrantLock lockRoom = new ReentrantLock();
    private ReentrantLock lockCar = new ReentrantLock();

    String serverFlight = "localhost";
    String serverRoom = "localhost";
    String serverCar = "localhost";

    int portFlight = 9000;
    int portRoom = 9000;
    int portCar = 9000;

    public void addFlight(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverFlight, portFlight, lockFlight).start();
    }

    public void deleteFlight(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverFlight, portFlight, lockFlight).start();
    }

    public void addRooms(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverRoom, portRoom, lockRoom).start();
    }

    public void deleteRooms(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverRoom, portRoom, lockRoom).start();
    }

    public void addCars(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverCar, portCar, lockCar).start();
    }

    public void deleteCars(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverCar, portCar, lockCar).start();
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
        new MiddlewareClient(message, socket, serverFlight, portFlight).start();
    }

    public void newCustomer(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverFlight, portFlight, lockFlight, lockRoom, lockCar).start();
    }

    public void deleteCustomer(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverFlight, portFlight, lockFlight, lockRoom, lockCar).start();
    }

    public void reserveCar(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverCar, portCar, lockCar).start();
    }

    public void reserveRoom(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverRoom, portRoom, lockRoom).start();
    }

    public void reserveFlight(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverFlight, portFlight, lockFlight).start();
    }

    public void itinerary(String message,Socket socket)
    {
        new MiddlewareClient(message, socket, serverFlight, portFlight, lockFlight, lockRoom, lockCar).start();
    }


}
