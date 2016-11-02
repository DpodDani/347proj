import java.rmi.RemoteException;

public interface Remote extends java.rmi.Remote {
    String sayHello() throws RemoteException;
}
