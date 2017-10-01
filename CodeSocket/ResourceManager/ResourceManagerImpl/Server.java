package ResourceManagerImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server{

    private ResourceManagerImpl resourceManager = new ResourceManagerImpl();
    private int port;

    public static void main(String args[])
    {
        Server server= new Server(Integer.parseInt(args[0]));
        try {
            server.runServerThread();
        }
        catch (IOException e) {

        }
    }

    public Server(int port){
        this.port = port;
    }


    public void runServerThread() throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server ready...");
        while (true) {
            Socket socket=serverSocket.accept();
            new ServerThread(socket, resourceManager).start();
        }
    }

}

