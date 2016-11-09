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
import java.io.File;
import java.io.*;

public class MyReplica implements Replica {
    
    ArrayList<String> database = new ArrayList<String>();
    static boolean isPrimary = false;

    public MyReplica() throws RemoteException{
	super();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

	// If a Primary node already exists in the registry, a Backup node is created, otherwise a Primary node is created
	// Note: the lookup() function returns an error when it cannot find the specified hostname in the registry. The presence of this error is used as an indicator for whether a Primary node already exists in the registry.
    	try {
	    Registry registry = LocateRegistry.getRegistry(1099);
	    registry.lookup("Primary");
	    System.out.println("Creating backup");
	    rebindRegistryEntry("Backup");
	    isPrimary = false;
	} catch (Exception e) {
	    System.err.println("Primary not found in registry");
	    System.out.println("Creating primary");
	    isPrimary = true;
	    try {
		rebindRegistryEntry("Primary");
	    } catch (Exception f) {
		System.err.println("Could not create primary");
	    }
	}
	//System.out.println(Integer.parseInt(new File("/proc/self").getCanonicalFile().getName()));
	System.out.println("Service started");
	System.out.println("Primary status: " + isPrimary);

    }


    public static void rebindRegistryEntry(String nodeName) throws RemoteException{
	try {
	    int PORT = (nodeName == "Primary") ? 1099 : 1100;
	    MyReplica node = new MyReplica();
	    RMIClientSocketFactory csf = new ClientSocketFactory();
	    RMIServerSocketFactory ssf = new ServerSocketFactory();
	    Replica stub = (Replica) UnicastRemoteObject.exportObject(node, 0, csf, ssf);

	    LocateRegistry.createRegistry(PORT);
	    Registry registry = LocateRegistry.getRegistry(PORT);
	    // Binds it in the registry <-- this is where the Primary or Backup are registered in the registry
	    registry.rebind(nodeName, node);
	    System.out.println(nodeName + " bound in registry");
	    //node.join("Primary");
	} catch (Exception e) {
	    System.err.println(nodeName + " is not bound in the registry");
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

    public void join(String primary){
    	if(!isPrimary){
    		try{
	    		Registry reg = LocateRegistry.getRegistry(1099);
		    	Replica object = (Replica) reg.lookup(primary);
		    	object.stateTransfer();
		    }catch(Exception e){

		    }
    	}
	
    }

    public void stateTransfer() {
			if (isPrimary) {
				try{
					Registry reg = LocateRegistry.getRegistry(1099);
		    	Replica object = (Replica) reg.lookup("Primary");
		    	reg.rebind("Backup", object);	
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
			}
    }

    public void kill() {
	try {
	   Registry reg = LocateRegistry.getRegistry(1099);
	    if (!isPrimary){
		Replica backup = (Replica) reg.lookup("Backup");
		reg.rebind("Primary", backup);
		System.out.println("Primary has been killed");
		System.out.println("Backup has become the new Primary");
	    } else {
		reg.unbind("Backup");
		System.out.println("Backup has been killed");
	    }
	} catch (Exception e) {
	    System.err.println("Kill function not working");
	}
    System.out.println("Primary and backup have been killed in the registry");
    }

}
