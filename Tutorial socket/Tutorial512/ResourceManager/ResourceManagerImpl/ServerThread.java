package ResourceManagerImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class ServerThread extends Thread
{
    Socket socket;
    ResourceManagerImpl resourceManager;
    ServerThread (Socket socket, ResourceManagerImpl resourceManager)
    {
        this.socket=socket;
        this.resourceManager = resourceManager;
    }

    public void run()
    {

        try
        {
            BufferedReader inFromClient= new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
            String message = null;
            while ((message = inFromClient.readLine())!=null)
            {

                System.out.println("message:"+message);
                String[] params =  message.split(",");
                if(params[0].equals("addFlight")){
                    message = String.valueOf(resourceManager.addFlight(1, Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3])));
                } else if(params[0].equals("deleteFlight")){
                    message = String.valueOf(resourceManager.deleteFlight(1, Integer.parseInt(params[1])));
                } else if(params[0].equals("addRooms")){
                    message = String.valueOf(resourceManager.addRooms(1, params[1], Integer.parseInt(params[2]), Integer.parseInt(params[3])));
                } else if(params[0].equals("deleteRooms")){
                    message = String.valueOf(resourceManager.deleteRooms(1, params[1]));
                } else if(params[0].equals("addCars")){
                    message = String.valueOf(resourceManager.addCars(1, params[1], Integer.parseInt(params[2]), Integer.parseInt(params[3])));
                } else if(params[0].equals("deleteCars")){
                    message = String.valueOf(resourceManager.deleteCars(1, params[1]));
                } else if(params[0].equals("queryFlight")){
                    message = String.valueOf(resourceManager.queryFlight(1, Integer.parseInt(params[1])));
                } else if(params[0].equals("queryFlightPrice")){
                    message = String.valueOf(resourceManager.queryFlightPrice(1, Integer.parseInt(params[1])));
                } else if(params[0].equals("queryRooms")){
                    message = String.valueOf(resourceManager.queryRooms(1, params[1]));
                } else if(params[0].equals("queryRoomsPrice")){
                    message = String.valueOf(resourceManager.queryRoomsPrice(1, params[1]));
                } else if(params[0].equals("queryCars")){
                    message = String.valueOf(resourceManager.queryCars(1, params[1]));
                } else if(params[0].equals("queryCarsPrice")){
                    message = String.valueOf(resourceManager.queryCarsPrice(1, params[1]));
                } else if(params[0].equals("queryCustomerInfo")){
                    message = resourceManager.queryCustomerInfo(1, Integer.parseInt(params[1]));
                } else if(params[0].equals("newCustomer")){
                    if(params.length == 1){
                        message = String.valueOf(resourceManager.newCustomer(1));
                    }else {
                        message = String.valueOf(resourceManager.newCustomer(1, Integer.parseInt(params[1])));
                    }
                } else if(params[0].equals("deleteCustomer")){
                    message = String.valueOf(resourceManager.deleteCustomer(1, Integer.parseInt(params[1])));
                } else if(params[0].equals("reserveCar")){
                    message = String.valueOf(resourceManager.reserveCar(1, Integer.parseInt(params[1]), params[2]));
                } else if(params[0].equals("reserveRoom")){
                    message = String.valueOf(resourceManager.reserveRoom(1, Integer.parseInt(params[1]), params[2]));
                }else if(params[0].equals("cancelRoom")){
                    message = String.valueOf(resourceManager.cancelRoom(1, Integer.parseInt(params[1]), params[2]));
                } else if(params[0].equals("cancelFlight")){
                    message = String.valueOf(resourceManager.cancelFlight(1, Integer.parseInt(params[1]), Integer.parseInt(params[2])));
                } else if(params[0].equals("cancelCar")){
                    message = String.valueOf(resourceManager.cancelCar(1, Integer.parseInt(params[1]), params[2]));
                } else if(params[0].equals("reserveFlight")){
                    message = String.valueOf(resourceManager.reserveFlight(1, Integer.parseInt(params[1]), Integer.parseInt(params[2])));
                } else {
                    //Function unknown
                    message = "Error: Function unknown";
                }

                outToClient.println(message);

            }
            socket.close();
        }
        catch (IOException e)
        {

        }

    }
}




