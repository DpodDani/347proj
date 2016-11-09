import java.rmi.*;
import java.rmi.registry.*;

public class Client {

    public static void main(String args[]) {

	try {
	    Registry registry = LocateRegistry.getRegistry(2002);
	    Server obj = (Server) registry.lookup("Hello");
	    String message = obj.sayHello();
	    System.out.println(message);
	} catch (Exception e) {
	    System.out.println("Client exception: " + e.getMessage());
	}

    }

}
