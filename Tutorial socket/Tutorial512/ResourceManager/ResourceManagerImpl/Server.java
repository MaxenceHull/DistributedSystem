package ResourceManagerImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server{

    private ResourceManagerImpl resourceManager = new ResourceManagerImpl();

    public static void main(String args[])
    {
        Server server= new Server();
        try {
            server.runServerThread();
        }
        catch (IOException e) {

        }
    }


    public void runServerThread() throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(9090);
        System.out.println("Server ready...");
        while (true) {
            Socket socket=serverSocket.accept();
            new ServerThread(socket, resourceManager).start();
        }
    }

}

