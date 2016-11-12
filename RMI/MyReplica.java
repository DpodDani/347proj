/**
 * This is the implementation for the Replica class. The Replica is responsible for creating a Primary node or a Backup node when necessary. The criteria right now: create a Primary node unless a Primary node already exists. This class also contains the fundamental functions for a Primary-Backup protocol.
 *
 * @author Qudus Animashaun, Daniel Namu-Fetha
 * @version 3.0
 */

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.*;
import java.io.File;
import java.io.*;
import java.net.*;
import java.util.*;

public class MyReplica implements Replica {
    
    ArrayList<String> database = new ArrayList<String>();
    static boolean isPrimary = false;
    private static Replica myStub;
    private static Registry exportedRegistry;

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

	

    }


    public static void rebindRegistryEntry(String nodeName) throws RemoteException{
	try {
	    int PORT = (nodeName == "Primary") ? 1099 : 1100;
	    MyReplica node = new MyReplica();
	    RMIClientSocketFactory csf = new ClientSocketFactory();
	    RMIServerSocketFactory ssf = new ServerSocketFactory();
	    Replica stub = (Replica) UnicastRemoteObject.exportObject(node, PORT);
	    myStub = node;
	    exportedRegistry = LocateRegistry.createRegistry(PORT);
	    Registry registry = LocateRegistry.getRegistry(PORT);
	    // Binds it in the registry <-- this is where the Primary or Backup are registered in the registry
	    registry.rebind(nodeName, stub);
	    System.out.println(nodeName + " bound in registry");
	    node.join("Primary");
	 
	}catch(ExportException e){
		System.err.println("The back up is already bounded");
	} catch (Exception e) {
	    System.err.println(nodeName + " is not bound in the registry");
	    e.printStackTrace();
	}
    }    
    
    //TODO: Activate the kill function only when the Primary is dead.
    public boolean write (String data, int sender) {
    	
			
			System.out.println("isPrimary status: " + isPrimary);
			//LocateRegistry.createRegistry();
			
			if(isPrimary){
				database.add(data);
				try{
					Registry reg = LocateRegistry.getRegistry(1100);
					Replica object = (Replica) reg.lookup("Backup");
					if (object != null)object.write(data, Values.PRIMARY);
				}catch(Exception e){
					System.err.println("The back up is not joined yet");
				}
				
			}
			if (!isPrimary && sender == Values.CLIENT) kill();
			

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

    // TODO: Put victim on terminator's port after killing victim
    public void kill() {

	int pPORT = 1099;
	int bPORT = 1100;

	try {
	    Registry registry = LocateRegistry.getRegistry(1100);
	    Replica backupStub = (Replica) registry.lookup("Backup");
	    MyReplica node = this;
	    System.out.println("Obtained registry");
	    UnicastRemoteObject.unexportObject(myStub, true);
	    System.out.println("Unexported registry");
	    Replica stub = (Replica) UnicastRemoteObject.exportObject(node, pPORT);
	    System.out.println("Exported object to primary port");
	    
	    registry.unbind("Backup");
	    UnicastRemoteObject.unexportObject(exportedRegistry, true);
	    LocateRegistry.createRegistry(1099);
	    Registry prim = LocateRegistry.getRegistry(1099);
	    prim.rebind("Primary", stub);
	    System.out.println("New Primary Registry created");
	    System.out.println("Kill list: " + Arrays.toString(prim.list()));
	    isPrimary = true;
	} catch (Exception e) {
	    System.err.println("Kill function error: " + e.getMessage());
	}

    }

}
