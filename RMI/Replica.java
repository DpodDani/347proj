import java.rmi.*;
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
	try{
	   // LocateRegistry.createRegistry(7000);
	    Replica server = new Replica();
	    Naming.rebind("ServerImpl", server);
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

