import java.net.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
 
public class BadBackup {

	/*	
		Works as a normal backup but takes a while to respond
	*/

	private static Todo list;

    public static void main(String[] args) throws IOException {
         
        if (args.length != 1) {
            System.err.println("Usage: java BadBackup <port number>");
            System.exit(1);
        }
 		
 		list = new Todo();

        int portNumber = Integer.parseInt(args[0]);
        switch (portNumber) { //right now port number defines the server role
        	case 9002: System.out.println("<badbackup> Starting as Bad Backup"); backupMain(args); break;
        	default: System.out.println("Port unrecognised! (varying ports to be included later)");
        }
    }

    private static void backupMain(String[] args) throws IOException {
    	int portNumber = Integer.parseInt(args[0]);
        System.out.println("<badbackup> Waiting for primary");
        while (true) {
    	    try {
    	    	//wait for connection from primary
				ServerSocket serverSocket = new ServerSocket(portNumber);
				Socket clientSocket = serverSocket.accept();
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			    TimeUnit.SECONDS.sleep(5); //Wait for 5 seconds, or over whatever the wait period is

        	    String message = (String) in.readObject();
				System.out.println("<badbackup> Recieved: "+message);
				switch (message.charAt(0)) {
					case 'W': list.add(message.substring(2,message.length())); out.writeObject("ACK"); break;
					case 'R': out.writeObject("ACK"); break;
					default: System.out.println("<badbackup> Unrecognised query"); out.writeObject("Unrecognised query!");
				}
				System.out.println("<badbackup> Current state: "+list.getList());
				clientSocket.close();
				serverSocket.close();
			} catch (ClassNotFoundException e) {
				System.out.println("Read object was not of the expected class");
        	    System.out.println(e.getMessage());
        	} catch (IOException e) {
    	        System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
        	    System.out.println(e.getMessage());
    	    } catch (InterruptedException e) {
    	    	System.out.println("Interrupted, probably while waiting");
        	    System.out.println(e.getMessage());
    	    }
	    }
    }
}