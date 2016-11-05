import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Server {

	private static Todo list;

	public static void main(String[] args) throws IOException {
		
		if (args.length != 1) {
			System.err.println("Usage: java Server <port number>");
			System.exit(1);
		}
		
		list = new Todo();

		int portNumber = Integer.parseInt(args[0]);
		switch (portNumber) { //right now port number defines the server role
			case 9001: System.out.println("<primary> Starting as Primary"); primaryMain(args); break;
			case 9002: System.out.println("<backup> Starting as Backup"); backupMain(args); break;
			default: System.out.println("Port unrecognised! (varying ports to be included later?)");
		}
	}

	private static void primaryMain(String[] args) throws IOException {
		ArrayList<String> recoverLog = new ArrayList<String>();
		boolean backupdown = false;
		int portNumber = Integer.parseInt(args[0]);
		System.out.println("<primary> Waiting for client");
		while (true) {
			try {
				//wait for connection from client
				ServerSocket serverSocket = new ServerSocket(portNumber);
				Socket clientSocket = serverSocket.accept();
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			
				String message = (String) in.readObject();
				System.out.println("<primary> Recieved: "+message);

				//Send the same message to backup and wait for response
				System.out.println("<primary> Forwarding message to backup");
				System.out.println("<primary> Assuming backup to be "+(backupdown ? "DOWN" : "UP"));
				Socket backupSocket = new Socket("localhost", 9002); //assumes location and port of backup
				try {
					backupSocket.setSoTimeout(2000); //timeout set at 2 seconds for now to avoid waiting for ages
					ObjectOutputStream backupOut = new ObjectOutputStream(backupSocket.getOutputStream());
					ObjectInputStream backupIn = new ObjectInputStream(backupSocket.getInputStream());
					
					backupOut.writeObject(message);
					String response = (String) backupIn.readObject();
					System.out.println("<primary> Backup response: "+response+" [should be ACK]");

					if (backupdown) { //if the backup was previously down but recovered, it reaches this stage
						//This assumes the backup won't go down while the log is being transferred
						System.out.println("<primary> Backup recovered! Passing log");

						//Send all items on the log
						for (String s : recoverLog) {
							System.out.println("\t"+s);
							//reset the connection, there's other ways to do this but idk what is best
							//note - last client request gets placed before the added log items
							//also, state prior to backup is lost, I'll fix this later
							backupSocket.close(); 
							backupSocket = new Socket("localhost", 9002);
							backupOut = new ObjectOutputStream(backupSocket.getOutputStream());
							backupIn = new ObjectInputStream(backupSocket.getInputStream());
							
							backupOut.writeObject(s);
							response = (String) backupIn.readObject(); 
							System.out.println("\t"+response);
						} 
						//Backup is no longer down; reset the log
						backupdown = false;
						recoverLog.clear();
					}
					
				} catch (SocketTimeoutException e) { //backup timed out
					if (!backupdown) { //first time
						System.out.println("<primary> Backup timed out! Assuming it is down - starting a log");
						backupdown = true;
					} else { //consecutive times
						System.out.println("<primary> Backup still down.");
					}
					recoverLog.add(message);
				} finally {
					backupSocket.close();
				}

				switch (message.charAt(0)) { //give response to client when finished with the backup
					case 'W': { //Write value to list
						System.out.println("<primary> Appending item and sending response to client");

						list.add(message.substring(2,message.length())); //This can throw an error, I'll add validation later
						out.writeObject("Added \""+message.substring(2,message.length())+"\""); 

						System.out.println("<primary> Current state: "+list.getList());
						break;
					}
					case 'R': { //Read list
						System.out.println("<primary> Returning list state to client");

						out.writeObject(list.getList()); 

						System.out.println("<primary> Current state: "+list.getList());
						break;
					}
					default: System.out.println("<primary> Unrecognised query"); out.writeObject("Unrecognised query!");
				}
				System.out.println();
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
		System.out.println("<backup> Waiting for primary");
		while (true) {
			try {
				//wait for connection from primary
				ServerSocket serverSocket = new ServerSocket(portNumber);
				Socket clientSocket = serverSocket.accept();
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			
				String message = (String) in.readObject();
				System.out.println("<backup> Recieved: "+message);
				//Same process as primary, but returns ACK instead
				switch (message.charAt(0)) {
					case 'W': list.add(message.substring(2,message.length())); out.writeObject("ACK"); break;
					case 'R': out.writeObject("ACK"); break;
					default: System.out.println("<backup> Unrecognised query"); out.writeObject("Unrecognised query!");
				}
				System.out.println("<backup> Current state: "+list.getList());
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