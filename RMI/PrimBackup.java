import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrimBackup extends Remote {
    void join (String backup) throws RemoteException;
    void stateTransfer() throws RemoteException;
    void kill() throws RemoteException;
}
