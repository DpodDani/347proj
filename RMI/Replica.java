import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
//import java.rmi.RemoteException;
import java.rmi.server.*;
import java.util.*;

public class Replica extends UnicastRemoteObject
	 implements TransactionHandler, PrimBackup {
    
    ArrayList<String> database = new ArrayList<String>();

    public Replica() throws RemoteException{
	super();
    }

    public static void main(String[] args){

	int port = Integer.parseInt(args[0]);
	String serverName = (port == 9001) ? "Primary" : "Backup";
	
	try {
	    startServer(port, serverName);
	} catch (Exception e) {
	    System.err.println(serverName + " start up error: " + e);
	    e.printStackTrace();
	} finally {
	    System.out.println(serverName + " successfully started");
	}

    }


    public static void startServer(int port, String serverName) throws RemoteException{
	try {
	    Registry reg = LocateRegistry.createRegistry(port);
	    Replica server = new Replica();
	    reg.rebind(serverName, server);	    
	} catch (Exception e) {
	    System.err.println(serverName + " is not free in the registry");
	    e.printStackTrace();
	}
    }    
    
    public boolean write (String data) {
	database.add(data);
	return true;
    }

    public String read() {
	StringBuilder sb = new StringBuilder();
	sb.append("[");
	for(String s: database){
	    sb.append(s +", ");
	}
	return (sb.length() > 0) ? sb.substring(0, sb.length() - 2) + "]" : "";
 
    }

    public void join(String backup) {
	
    }

    public void stateTransfer() {
	
    }

    public void kill() {
	
    }

}
