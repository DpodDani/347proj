import java.net.*;
import java.io.*;
 
public class Server {

	private static Todo list;

    public static void main(String[] args) throws IOException {
         
        if (args.length != 1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }
 		
 		list = new Todo();

        int portNumber = Integer.parseInt(args[0]);
        switch (portNumber) {
        	case 9001: System.out.println("<primary> Starting as Primary"); primaryMain(args); break;
        	case 9002: System.out.println("<backup> Starting as Backup"); backupMain(args); break;
        	default: System.out.println("Port unrecognised! (varying ports to be included later)");
        }
    }

    private static void primaryMain(String[] args) throws IOException {
    	int portNumber = Integer.parseInt(args[0]);
        System.out.println("<primary> Waitin for client");
        while (true) {
    	    try {
				ServerSocket serverSocket = new ServerSocket(portNumber);
				Socket clientSocket = serverSocket.accept();
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			
        	    String message = (String) in.readObject();
				System.out.println("<primary> Read: "+message);
				switch (message.charAt(0)) {
					case 'W': {
						out.writeObject("Added \""+message.substring(2,message.length())+"\""); 
						list.add(message.substring(2,message.length())); 

						Socket backupSocket = new Socket("localhost", 9002); //assumes location and port of backup
						ObjectOutputStream backupOut = new ObjectOutputStream(backupSocket.getOutputStream());
            			ObjectInputStream backupIn = new ObjectInputStream(backupSocket.getInputStream());
            
            			backupOut.writeObject(message);
            			String response = (String) backupIn.readObject();
            			System.out.println("<primary> Backup response: "+response+" [should be ACK]");
            			backupSocket.close();
            			System.out.println("Current state: "+list.getList());
						break;
					}
					case 'R': {
						out.writeObject(list.getList()); 

						Socket backupSocket = new Socket("localhost", 9002); //assumes location and port of backup
						ObjectOutputStream backupOut = new ObjectOutputStream(backupSocket.getOutputStream());
            			ObjectInputStream backupIn = new ObjectInputStream(backupSocket.getInputStream());
            
            			backupOut.writeObject(message);
            			String response = (String) backupIn.readObject();
            			System.out.println("<primary> Backup response: "+response+" [should be ACK]");
            			backupSocket.close();
            			System.out.println("Current state: "+list.getList());

						break;
					}
					default: out.writeObject("Unrecognised query!");
				}
				clientSocket.close();
				serverSocket.close();
			} catch (ClassNotFoundException e) {
				System.out.println("Read object was not of the expected class");
        	    System.out.println(e.getMessage());
        	} catch (IOException e) {
    	        System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
        	    System.out.println(e.getMessage());
    	    }
	    }
    }

    private static void backupMain(String[] args) throws IOException {
    	int portNumber = Integer.parseInt(args[0]);
        System.out.println("<backup> Waitin for primary");
        while (true) {
    	    try {
				ServerSocket serverSocket = new ServerSocket(portNumber);
				Socket clientSocket = serverSocket.accept();
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			
        	    String message = (String) in.readObject();
				System.out.println("<backup> Read: "+message);
				switch (message.charAt(0)) {
					case 'W': out.writeObject("ACK"); list.add(message.substring(2,message.length())); System.out.println("Current state: "+list.getList()); break;
					case 'R': out.writeObject("ACK"); System.out.println("Current state: "+list.getList()); break;
					default: out.writeObject("Unrecognised query!");
				}
				clientSocket.close();
				serverSocket.close();
			} catch (ClassNotFoundException e) {
				System.out.println("Read object was not of the expected class");
        	    System.out.println(e.getMessage());
        	} catch (IOException e) {
    	        System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
        	    System.out.println(e.getMessage());
    	    }
	    }
    }
}