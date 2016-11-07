import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
 
public class BadBackup {

	/*	
		Works as a normal backup but doesn't respond
        TimeUnit.SECONDS.sleep(5); //Wait for 5 seconds, or over whatever the wait period is

	*/

	private static Todo list;

    public static void main(String[] args) throws IOException {
         
        if (args.length != 2) {
            System.err.println("Usage: java BadBackup <port number> <database filename>");
            System.exit(1);
        }
 		
 		list = new Todo(args[1]);

        int portNumber = Integer.parseInt(args[0]);
        switch (portNumber) { //right now port number defines the server role
        	case 9002: System.out.println("<badbackup> Starting as Bad Backup"); backupMain(args); break;
        	default: System.out.println("Port unrecognised! (varying ports to be included later)");
        }
    }

    private static void backupMain(String[] args) throws IOException {
        int portNumber = Integer.parseInt(args[0]);
        System.out.println("<badbackup> Waiting for primary");
        ServerSocket serverSocket = new ServerSocket(portNumber);
        try {
            Socket clientSocket = serverSocket.accept();
            System.out.println("<badbackup> Connected to primary!");
            ThreadPool.executor.execute(new BadBackupThread(clientSocket, list)); //begin a thread to accept messages
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
            //clientSocket.close();
            serverSocket.close();
        }
    }
}

class BadBackupThread implements Runnable { //thread used by the server
    private Socket socket;
    private Todo list;

    public BadBackupThread(Socket socket, Todo list) {
        this.socket = socket;
        this.list = list;
    }

    public void run() {
        try{
            //set up I/O streams
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            while(true) { //repeatedly wait for a message to arrive and take action based upon what the message is
                String message = (String) in.readObject();
                TimeUnit.SECONDS.sleep(5);
                System.out.println("<badbackup> Recieved: "+message);
                //Same process as primary, but returns ACK instead
                switch (message.charAt(0)) {
                    case 'W': list.add(message.substring(2,message.length())); out.writeObject("ACK"); break;
                    case 'R': out.writeObject("ACK"); break;
                    default: System.out.println("<backup> Unrecognised query"); out.writeObject("Unrecognised query!");
                }
                System.out.println("<backup> Current state: "+list.getList());
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Unrecognised message recieved!");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Some error occured:");
            e.printStackTrace();
        }
    }
}

class BadThreadPool {
    public static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
}