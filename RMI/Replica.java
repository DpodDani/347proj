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

	/*if (args.length != 1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }*/
	
	try{
		startServer("9001");  
	}catch(Exception e){
		try{
			startServer("9002");
		}catch(Exception p){

		}
		
	}
	    
	}


    public static void startServer(String port) throws RemoteException{	
    	if(port.equals("9001")){
			try{
			    Registry reg = LocateRegistry.createRegistry(Integer.parseInt(port));
			    Replica server = new Replica();
			    reg.rebind("Primary", server);
			}catch(ExportException e){
			    e.printStackTrace();
			    try{
				    Registry reg = LocateRegistry.createRegistry(9002);
				    Replica server = new Replica();
				    reg.rebind("Backup", server);
				}catch(Exception p){
				    p.printStackTrace();
				}
			}
		}else{
			try{
			    Registry reg = LocateRegistry.createRegistry(Integer.parseInt(port));
			    Replica server = new Replica();
			    reg.rebind("Backup", server);
			}catch(Exception e){
			    e.printStackTrace();
			}
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
	return (sb.length() > 0) ? sb.substring(0, sb.length() - 3) + "]" : "";
 
    }

    public void join(String backup) {
	
    }

    public void stateTransfer() {
	
    }

    public void kill() {
	
    }

}

