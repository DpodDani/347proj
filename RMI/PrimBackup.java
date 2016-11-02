import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrimBackup extends Remote {
    void join (String backup);
    void stateTransfer();
    void kill();
}
