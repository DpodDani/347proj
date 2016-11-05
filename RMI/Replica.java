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

	if (args.length != 1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }
	if(args[0].equals("9001")) {
	  startServer("9001");  
	}else{
	    startServer("9002");
	}

    }

    public static void startServer(String port){	
	try{
	    Registry reg = LocateRegistry.createRegistry(Integer.parseInt(port));
	    Replica server = new Replica();
	    reg.rebind("Primary", server);
	}catch(Exception e){
	    e.printStackTrace();
	}
    }    
    
    public boolean write (String data)  {
	database.add(data);
	return true;
    }

    public String read() {
	StringBuilder sb = new StringBuilder();
	sb.append("[");
	for(String s: database){
	    sb.append(s +", ");
	}
	sb.append("]");
	return sb.toString();
    }

    public void join(String backup) {
	
    }

    public void stateTransfer() {
	
    }

    public void kill() {
	
    }

}

