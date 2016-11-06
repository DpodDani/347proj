/**
 * This is a remote interface for the interface between the Primary and Backup nodes. Contains functions that allow the Backup to join the Primary, the Primary to transfer its state to the Backup upon joining, and a function enabling either node to kill the other upon suspicion of a fault.
 *
 * @author Daniel Namu-Fetha
 * @version 1.0
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrimBackup extends Remote {
    void join (String backup) throws RemoteException;
    void stateTransfer() throws RemoteException;
    void kill() throws RemoteException;
}
