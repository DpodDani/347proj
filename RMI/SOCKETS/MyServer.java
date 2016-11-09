import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

public class MyServer implements Server {

    public MyServer() {
	// empty
    }

    public String sayHello() {
	return "Hello World!";
    }

    public static void main(String args[]) {

	try {
	    MyServer obj = new MyServer();
	    RMIClientSocketFactory csf = new ClientSocketFactory();
	    RMIServerSocketFactory ssf = new ServerSocketFactory();
	    Server stub = (Server) UnicastRemoteObject.exportObject(obj, 0, csf, ssf);

	    LocateRegistry.createRegistry(2002);
	    Registry registry = LocateRegistry.getRegistry(2002);
	    registry.rebind("Hello", stub);
	    System.out.println("MyServer bound in registry");
	} catch (Exception e) {
	    System.out.println("MyServer exception: " + e.getMessage());
	}
    
    }

}
