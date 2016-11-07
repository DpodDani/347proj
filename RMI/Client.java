/**
 * This is the implementation for the Client class. The Client will be expected to perform read/write transactions with the Primary node. If the Primary node is unavailable, the Client will forward the transaction request to the Backup node.
 *
 * @author Qudus Animashaun, Daniel Namu-Fetha
 * @version 2.0
 */

import java.rmi.Naming; 
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.*;


public class Client{

    public static void main(String[] args){

	try{
	    // The idea is that the registry will be located at one port and all clients will accessing this one port
	    // Let me know if this is the wrong idea
	    Registry reg = LocateRegistry.getRegistry(1099);
	    // Client looks for the Primary node in the registry
	    Replica object = (Replica) reg.lookup("Primary");
	    // Client performs a couple of write/read transactions
	    object.write("Daniel ");
	    object.write("is ");
	    object.write("tall");
	    System.out.println(object.read());
	}catch(Exception e){
	    System.err.println("Client couldn't connect Primary node in registry");
	    // If the Client cannot find the Primary node in the registry, it assumes the Primary node is down and therefore forwards the request to the Backup node
	    try{
		Registry reg = LocateRegistry.getRegistry(1099);
		Replica object = (Replica) reg.lookup("Backup");
		object.write("Daniel ");
		object.write("is ");
		object.write("tall");
		System.out.println(object.read());
	    }catch(Exception p){
		System.err.println("Client couldn't connect to Backup node in registry");
	    }
	}
    }

}
