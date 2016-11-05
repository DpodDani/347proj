import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TransactionHandler extends Remote {
    boolean write(String word) throws RemoteException;
    String read() throws RemoteException;

}
