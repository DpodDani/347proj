/**
 * This is the interface for Replica. The main purpose of this interface is to combine the functions from PrimBackup and TransactionHandler into one interface which can be implemented by a Primary or Backup node.
 *
 * @author Daniel Namu-Fetha
 * @version 1.0
 */

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.*;
import java.rmi.Remote;

public interface Replica extends PrimBackup, TransactionHandler {
    
}
