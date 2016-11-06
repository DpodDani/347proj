/**
 * This is the implementation for the Replica class. The Replica is responsible for creating a Primary node or a Backup node when necessary. The criteria right now: create a Primary node unless a Primary node already exists. This class also contains the fundamental functions for a Primary-Backup protocol.
 *
 * @author Qudus Animashaun, Daniel Namu-Fetha
 * @version 2.0
 */

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.*;

public class MyReplica extends UnicastRemoteObject implements Replica {
    
    ArrayList<String> database = new ArrayList<String>();
    static boolean isPrimary = false;

    public MyReplica() throws RemoteException{
	super();
    }

    public static void main(String[] args){

	// If a Primary node already exists in the registry, a Backup node is created, otherwise a Primary node is created
	// Note: the lookup() function returns an error when it cannot find the specified hostname in the registry. The presence of this error is used as an indicator for whether a Primary node already exists in the registry.
    	try {
	    Registry registry = LocateRegistry.getRegistry(1099);
	    registry.lookup("Primary");
	    System.out.println("Creating backup");
	    createRegistryEntry("Backup");
	    isPrimary = false;
	} catch (Exception e) {
	    System.err.println("Primary not found in registry");
	    System.out.println("Creating primary");
	    isPrimary = true;
	    try {
		createRegistryEntry("Primary");
	    } catch (Exception f) {
		System.err.println("Could not create primary");
	    }
	}

	System.out.println("Service started");
	System.out.println("Primary status: " + isPrimary);

    }


    public static void createRegistryEntry(String nodeName) throws RemoteException{
	try {
	    Registry registry = LocateRegistry.getRegistry(1099);
	    // Creates a remote interface object
	    MyReplica node = new MyReplica();
	    // Binds it in the registry <-- this is where the Primary or Backup are registered in the registry
	    registry.rebind(nodeName, node);	    
	} catch (Exception e) {
	    System.err.println(nodeName + " is not free in the registry");
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
	try {
	   Registry reg = LocateRegistry.getRegistry(1099);
	    reg.unbind("Primary");
	    reg.unbind("Backup");
	} catch (Exception e) {
	    System.err.println("kill function not working");
	}
    System.out.println("Primary and backup have been killed in the registry");
    }

}
