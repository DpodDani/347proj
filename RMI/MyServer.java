// This is the server. Will create an instance of the remote object implementation, export the remote object and bind the instance to a name in a Java RMI registry.

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MyServer implements MyRemote {

    public MyServer() {}

    public String sayHello() {
	return "World";
    }

    public static void main(String args[]) {

	try{
	    // Creates remote object that provides service
	    // Then exports remote object to Java RMI runtime so it can receive incoming remote calls
	    MyServer obj = new MyServer();
	    MyRemote stub = (MyRemote) UnicastRemoteObject.exportObject(obj, 0);

	    // Obtains stub for registry on local host and default registry port
	    // Then uses registry stub to bind name "myRemote" to remote object's stub in registry
	    Registry registry = LocateRegistry.getRegistry();
	    registry.rebind("myRemote", stub);

	    System.err.println("Server ready");
	} catch (Exception e) {
	    System.err.println("Server exception: " + e.toString());
	    e.printStackTrace();
	}

    }

}
