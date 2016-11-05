import java.rmi.Naming; 
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;


public class Client{
    public static void main(String[] args){

//	System.setSecurityManager(new SecurityManager());
	try{
	    TransactionHandler object = (TransactionHandler) Naming.lookup("ServerImpl");
	    object.write("Daniel ");
	    object.write("is ");
	    object.write("tall");
	    System.out.println(object.read());
	}catch(Exception e){
	    e.printStackTrace();
	}
    }
}
