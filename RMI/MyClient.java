import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MyClient {

    private MyClient() {}

    public static void main(String args[]) {

	String host = (args.length < 1) ? null : args[0];
	System.err.println("Host: " + host);

	try {
	    Registry registry = LocateRegistry.getRegistry(host);
	    MyRemote stub = (MyRemote) registry.lookup("myRemote");
	    String response = stub.sayHello();
	    System.out.println("Response: " + response);
	} catch (Exception e) {
	    System.err.println("Client exception: " + e.toString());
	    e.printStackTrace();
	}

    }

}
