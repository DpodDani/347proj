import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Backup implements PrimBackup {

    public Backup() {
	// constructor will be empty for now
    }

    public void join (String backup) {
	// Enable backup to join primary after successfull reboot
    }

    public void stateTransfer() {
	// Enable primary to transfer state to backup when it successfully joins
	// Maybe call this function inside the join() function?
    }

    public void kill() {
	// Enable primary or backup to kill the other when they're suspected or failing
    }

}
