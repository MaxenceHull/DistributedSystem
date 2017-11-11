import ResInterface.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.*;


public class ClientTestTransaction {

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

        //Test transaction
        int idTransaction = -1;
        int idTransaction2 = -1;

        try {
            idTransaction = rm.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            rm.addFlight(idTransaction, 777, 50, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            idTransaction2 = rm.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //Test 1: Try to get a read lock but another transaction has a write lock
        try {
            rm.queryFlight(idTransaction2, 777);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        } catch (TransactionAbortedException e) {
            e.printStackTrace();
            System.out.println("Test 1-1 passed ");
        }

        try {
            rm.commit(idTransaction);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        } catch (TransactionAbortedException e) {
            e.printStackTrace();
        }

        try {
            System.out.println(rm.queryFlight(idTransaction2, 777));
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        } catch (TransactionAbortedException e) {
            e.printStackTrace();
        }
        System.out.println("Test 1-2 passed");
        try {
            rm.commit(idTransaction2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Test 2: Try to get a read lock while another transaction already has a read lock
        idTransaction = -1;
        try {
            idTransaction = rm.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        idTransaction2 = -1;
        try {
            idTransaction2 = rm.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            rm.queryFlight(idTransaction, 777);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test 2 -1 failed");
        }

        try {
            rm.queryFlight(idTransaction2, 777);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test 2 -2 failed");
        }
        System.out.println("Test 2 passed");

        try {
            rm.commit(idTransaction);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            rm.commit(idTransaction2);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //Test 3: Upgrade from read to write lock
        idTransaction = -1;
        try {
            idTransaction = rm.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        idTransaction2 = -1;
        try {
            idTransaction2 = rm.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            rm.queryFlight(idTransaction, 777);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test 3 - 1 failed");
        }

        try {
            rm.addFlight(idTransaction, 777, 10, 124);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test 3 - 2 failed");
        }

        try {
            idTransaction2 = rm.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            rm.queryFlight(idTransaction2, 777);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        } catch (TransactionAbortedException e) {
            e.printStackTrace();
            System.out.println("Test 3 passed");
        }

        try {
            rm.commit(idTransaction);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            rm.commit(idTransaction2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Test 4: Try to get a write lock but another transaction already has a read lock
        idTransaction = -1;
        try {
            idTransaction = rm.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        idTransaction2 = -1;
        try {
            idTransaction2 = rm.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            rm.queryFlight(idTransaction, 777);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            rm.addFlight(idTransaction2, 777, 134, 23400);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        } catch (TransactionAbortedException e) {
            e.printStackTrace();
            System.out.println("Test 4 passed");
        }

        try {
            rm.commit(idTransaction);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            rm.commit(idTransaction2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Test 5: Rollback
        idTransaction = -1;
        try {
            idTransaction = rm.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        try {
            rm.addFlight(idTransaction, 900, 50, 345);
            rm.addCars(idTransaction, "Montreal", 23, 145);
            rm.addRooms(idTransaction, "Toronto", 2, 123);
            int idCustomer = rm.newCustomer(idTransaction);
            rm.reserveFlight(idTransaction, idCustomer, 900);
            rm.reserveCar(idTransaction, idCustomer, "Montreal");
            rm.reserveRoom(idTransaction, idCustomer, "Toronto");
            rm.abort(idTransaction);
            idTransaction = rm.start();
            if(rm.queryFlightPrice(idTransaction, 900)!= 0 || rm.queryFlight(idTransaction, 900) != 0){
                System.out.println("Test 5-1 failed");
            }

            if(rm.queryCars(idTransaction, "Montreal")!= 0 || rm.queryCarsPrice(idTransaction, "Montreal") != 0){
                System.out.println("Test 5-2 failed");
            }

            if(rm.queryRooms(idTransaction, "Toronto")!= 0 || rm.queryRoomsPrice(idTransaction, "Toronto") != 0){
                System.out.println("Test 5-3 failed");
            }

            System.out.println(rm.queryCustomerInfo(idTransaction, idCustomer));
            if(!rm.queryCustomerInfo(idTransaction, idCustomer).equals("")){
                System.out.println("Test 5-4 failed");
            }
            rm.commit(idTransaction);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        } catch (TransactionAbortedException e) {
            e.printStackTrace();
        }
        System.out.println("Test 5 passed");

        //Test 6: Rollback customer
        try {
            idTransaction = rm.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            idTransaction = rm.start();
            rm.addFlight(idTransaction, 900, 50, 345);
            rm.addCars(idTransaction, "Montreal", 23, 145);
            rm.addRooms(idTransaction, "Toronto", 2, 123);
            int idCustomer = rm.newCustomer(idTransaction);
            rm.reserveFlight(idTransaction, idCustomer, 900);
            rm.reserveCar(idTransaction, idCustomer, "Montreal");
            rm.reserveRoom(idTransaction, idCustomer, "Toronto");
            rm.commit(idTransaction);
            idTransaction = rm.start();
            rm.deleteCustomer(idTransaction, idCustomer);
            rm.abort(idTransaction);
            idTransaction = rm.start();
            System.out.println(rm.queryCustomerInfo(idTransaction, idCustomer));
            if(rm.queryCustomerInfo(idTransaction, idCustomer).equals("")){
                System.out.println("Test 6 failed");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        } catch (TransactionAbortedException e) {
            e.printStackTrace();
        }
        System.out.println("Test 6 passed");
    }
}