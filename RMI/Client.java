import java.rmi.Naming; 
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Client{
    public static void main(String[] args){

//	System.setSecurityManager(new SecurityManager());
	try{
	    Registry reg = LocateRegistry.getRegistry(9001);
	    TransactionHandler object = (TransactionHandler) reg.lookup("Primary");
	    object.write("Daniel ");
	    object.write("is ");
	    object.write("tall");
	    System.out.println(object.read());
	}catch(Exception e){
	    e.printStackTrace();
	}
    }
}
