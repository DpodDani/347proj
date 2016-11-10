/**
 * This is a remote interface for the interface between the Client and the Primary node. The main functions include the ability to read and write data.
 *
 * @author Daniel Namu-Fetha
 * @version 1.0
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TransactionHandler extends Remote {
    boolean write(String word, int sender) throws RemoteException;
    String read() throws RemoteException;

}
