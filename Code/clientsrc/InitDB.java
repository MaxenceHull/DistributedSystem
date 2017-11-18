import ResInterface.*;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.*;


public class InitDB
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
        if (args.length > 3) {
            System.out.println("Usage: java client [rmihost [rmiport] [load: double]]");
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
            System.setSecurityManager(new RMISecurityManager());
        }

        try {
            int idTransaction = rm.start();
            rm.addFlight(idTransaction, 777, 10000,300);
            rm.addFlight(idTransaction, 888, 10000,300);
            rm.addFlight(idTransaction, 999, 10000,300);
            rm.addFlight(idTransaction, 111, 10000,300);
            rm.addRooms(idTransaction, "Paris", 10000, 100);
            rm.addRooms(idTransaction, "Montreal", 10000, 100);
            rm.addRooms(idTransaction, "Rome", 10000, 100);
            rm.addRooms(idTransaction, "Chicago", 10000, 100);
            rm.addCars(idTransaction, "Paris", 10000, 400);
            rm.addCars(idTransaction, "Montreal", 10000, 400);
            rm.addCars(idTransaction, "Rome", 10000, 400);
            rm.addCars(idTransaction, "Chicago", 10000, 400);
            rm.newCustomer(idTransaction, 20);
            rm.newCustomer(idTransaction, 30);
            rm.newCustomer(idTransaction, 40);
            rm.newCustomer(idTransaction, 50);
            rm.commit(idTransaction);
        }catch (Exception e){
            System.out.println("DB cannot be init");
        }


    }
}
