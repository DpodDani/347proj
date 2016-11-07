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

public class Server {

	private static Todo list;

	public static void main(String[] args) throws IOException {
		
		if (args.length != 2) {
			System.err.println("Usage: java Server <port number> <database filename>");
			System.exit(1);
		}
		
		list = new Todo(args[1]);

		int portNumber = Integer.parseInt(args[0]);
		switch (portNumber) { //right now port number defines the server role
			case 9001: System.out.println("<primary> Starting as Primary"); primaryMain(args); break;
			case 9002: System.out.println("<backup> Starting as Backup"); backupMain(args); break;
			default: System.out.println("Port unrecognised! (varying ports to be included later?)");
		}
	}

	private static void primaryMain(String[] args) throws IOException {
		int portNumber = Integer.parseInt(args[0]);
		//set up channel to backup
		Socket backupSocket = new Socket("localhost", 9002); //assumes location and port of backup
		ThreadPool.executor.execute(new PrimaryThread(list, portNumber, backupSocket)); //begin a thread to accept messages

	}

	private static void backupMain(String[] args) throws IOException {
		int portNumber = Integer.parseInt(args[0]);
		System.out.println("<backup> Waiting for primary");
		ServerSocket serverSocket = new ServerSocket(portNumber);
		try {
			Socket clientSocket = serverSocket.accept();
			System.out.println("<backup> Connected to primary!");
			ThreadPool.executor.execute(new BackupThread(clientSocket, list)); //begin a thread to accept messages
		} catch (IOException e) {
			System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
			//clientSocket.close();
			serverSocket.close();
		}
	}
}


class BackupThread implements Runnable { //thread used by the server
	private Socket socket;
	private Todo list;

	public BackupThread(Socket socket, Todo list) {
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
				System.out.println("<backup> Recieved: "+message);
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


class PrimaryThread implements Runnable { //parent thread to accept client requests
	private int portNumber;
	private Todo list;
	private Socket backupSocket;

	public PrimaryThread(Todo list, int port, Socket backupSocket) {
		this.portNumber = port;
		this.list = list;
		this.backupSocket = backupSocket;
	}

	public void run() {
		
		try {
			backupSocket.setSoTimeout(2000);
			ObjectOutputStream backupOut = new ObjectOutputStream(backupSocket.getOutputStream());
			ObjectInputStream backupIn = new ObjectInputStream(backupSocket.getInputStream());
			ServerSocket serverSocket = new ServerSocket(portNumber);
			while (true) {
				System.out.println("<primary> Waiting for client");
				//wait for connection from client
				Socket clientSocket = serverSocket.accept();
				ThreadPool.executor.execute(new ClientHandleThread(list, clientSocket, backupOut, backupIn)); //begin a thread to accept messages

			}
		} catch (IOException e) {
			System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		} 
	}
}

class ClientHandleThread implements Runnable { //child thread to deal with client requests
	private Todo list;
	private Socket clientSocket;
	private ObjectOutputStream backupOut;
	private ObjectInputStream backupIn;

	public ClientHandleThread(Todo list, Socket clientSocket, ObjectOutputStream backupOut, ObjectInputStream backupIn) {
		this.clientSocket = clientSocket;
		this.list = list;
		this.backupOut = backupOut;
		this.backupIn = backupIn;
	}

	public void run() {

		try{
			System.out.println("<primary> Client connected!");

			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

			String message = (String) in.readObject();
			System.out.println("<primary> Recieved: "+message);
			try {
				//Send the same message to backup and wait for response
				System.out.println("<primary> Forwarding message to backup");
				backupOut.writeObject(message);

				String response = (String) backupIn.readObject();
				System.out.println("<primary> Backup response: "+response+" [should be ACK]");
			} catch (SocketTimeoutException e) {
				System.out.println("<primary> Backup timed out! Better start a log...");
			}
			

			//give response to client when finished with the backup
			switch (message.charAt(0)) { 
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

		} catch (ClassNotFoundException e) {
			System.out.println("Read object was not of the expected class");
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println("Exception caught when trying to listen on port or listening for a connection");
			System.out.println(e.getMessage());
		}
	}
}

class ThreadPool {
	public static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
}