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
    private static Replica myStub; // Keeps track of the object exported to the RMI registry (so we can unexport it when need be)
    private static Registry exportedRegistry; // Keeps track of the registry that is exported to the RMI registry (upon its creation)
	private static final int pPORT = Values.PRIMARY.getValue();
	private static final int bPORT = Values.BACKUP.getValue();

    public MyReplica() throws RemoteException{
	super();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

	// If a Primary node already exists in the registry, a Backup node is created, otherwise a Primary node is created
	// Note: the lookup() function returns an error when it cannot find the specified hostname in the registry. The presence of this error is used as an indicator for whether a Primary node already exists in the registry.
    	try {
	    Registry registry = LocateRegistry.getRegistry(pPORT);
	    registry.lookup("Primary");
	    System.out.println("Creating backup");
	    bindRegistryEntry("Backup");
	    isPrimary = false;
	} catch (Exception e) {
	    System.err.println("Primary not found in registry");
	    System.out.println("Creating primary");
	    isPrimary = true;
	    try {
		bindRegistryEntry("Primary");
		
		// WIP - Heartbeat functionality
		/*try {
		    while (true) {
			Thread.sleep(5000);
			System.out.println("Sending heartbeat");
		    }
		} catch (InterruptedException f) {
		    System.err.println("Thread error: " +f.getMessage());
		}*/

	    } catch (Exception f) {
		System.err.println("Could not create primary");
	    }
	}

	

    }


    public static void bindRegistryEntry(String nodeName) throws RemoteException{
	try {
	    int PORT = (nodeName == "Primary") ? pPORT : bPORT;
	    MyReplica node = new MyReplica();
	    Replica stub = (Replica) UnicastRemoteObject.exportObject(node, PORT);
	    System.out.println("PORT: " + PORT);
	    myStub = node;
	    exportedRegistry = LocateRegistry.createRegistry(PORT);
	    Registry registry = LocateRegistry.getRegistry(PORT);
	    // Binds it in the registry <-- this is where the Primary or Backup are registered in the registry
	    registry.rebind(nodeName, stub);
	    System.out.println(nodeName + " bound in registry");

	    //node.join("Primary");
	 
	}catch(ExportException e){
		System.err.println("The back up is already bounded");
	} catch (Exception e) {
	    System.err.println(nodeName + " is not bound in the registry");
	    e.printStackTrace();
	}
    }    
    
    public boolean write (String data, Values  sender) {
			if(isPrimary){
				System.out.println("<Primary>: Received "+data);
			}else{
				System.out.println("<Backup>: Received "+data);
			}
			
	String writer = (isPrimary) ? "Primary" : "Backup";
	System.out.print("<" + writer + ">: ");
	System.out.println(writer + " writing to database: " + data);
	
	// This checks whether the call to the write function was made by the Primary node to propogate the Client's request
	if(!isPrimary && sender == Values.PRIMARY) database.add(data);

	// This ensures that the Client's request is handled by the Primary node and propogated to the Backup node
	if(isPrimary){
	    database.add(data);
	    Boolean propSuccess = propogate(data);
	    System.out.println("Propogate success: " + propSuccess);
	}

	// If the Client speaks directly with the Backup node, it assumes that the Primary has crashed, therefore begins to replace it
	if (!isPrimary && sender == Values.CLIENT) kill();

	return true;
    }

    private boolean propogate(String data) {
	try {
	    Registry reg = LocateRegistry.getRegistry(bPORT);
	    Replica backupObj = (Replica) reg.lookup("Backup");
	    if (backupObj != null) backupObj.write(data, Values.PRIMARY);
	} catch (Exception e) {
	    System.err.println("Propogation error: " + e.getMessage());
	    return false;
	}
	return true;
    }

    public String read() {
	return Arrays.toString(database.toArray());
    }

    // WIP - Function for enabling Backup node to join Primary after restart
    public void join(String primary){
    	if(!isPrimary){
    		try{
	    		Registry reg = LocateRegistry.getRegistry(pPORT);
		    	Replica object = (Replica) reg.lookup(primary);
		    	object.stateTransfer();
		    }catch(Exception e){

		    }
    	}
	
    }

    // WIP - Function for Primary node to transfer its current state to a newly joined Backup node
    public void stateTransfer() {
			if (isPrimary) {
				try{
					Registry reg = LocateRegistry.getRegistry(pPORT);
		    	Replica object = (Replica) reg.lookup("Primary");
		    	reg.rebind("Backup", object);	
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
			}
    }

    // TODO: If the Primary calls kill, that's a signal for it to start logging its messages for when the backup is restarted
    public void kill() {

	try {
	    Registry registry = LocateRegistry.getRegistry(bPORT);
	    Replica backupStub = (Replica) registry.lookup("Backup");
	    System.out.println("Obtained Backup registry");
	    UnicastRemoteObject.unexportObject(myStub, true); // unexport backup stub from old RMI port <-- prevents "ObjID already taken" error
	    System.out.println("Unexported Backup stub from RMI");
	    Replica stub = (Replica) UnicastRemoteObject.exportObject(this, pPORT); // export backup stub to new RMI port
	    myStub = stub;
	    System.out.println("Exported Backup to primary port");
	    
	    UnicastRemoteObject.unexportObject(exportedRegistry, true); // unexport backup registry from old RMI port <-- allows a new Backup registry to be created later (after our current Backup becomes the new Primary)
	    exportedRegistry = LocateRegistry.createRegistry(pPORT);
	    Registry prim = LocateRegistry.getRegistry(pPORT);
	    prim.rebind("Primary", stub);
	    System.out.println("New Primary Registry created");
	    isPrimary = true; // the backup node is now a primary node
	} catch (Exception e) {
	    System.err.println("Kill function error: " + e.getMessage());
	}

    }

}
