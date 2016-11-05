import java.rmi.Naming; 
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.*;


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
	    try{
		    Registry reg = LocateRegistry.getRegistry(9002);
		    TransactionHandler object = (TransactionHandler) reg.lookup("Backup");
		    object.write("Daniel ");
		    object.write("is ");
		    object.write("tall");
		    System.out.println(object.read());
		}catch(Exception p){
		    p.printStackTrace();
	}
	}
    }
}
