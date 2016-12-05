/**
* This is the implementation for the Replica class. The Replica is responsible for creating a Primary node or a Backup node when necessary. The criteria right now: create a Primary node unless a Primary node already exists. This class also contains the fundamental functions for a Primary-Backup protocol.
*
* @author Qudus Animashaun, Daniel Namu-Fetha
* @version 3.0
*/

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.*;
import java.io.File;
import java.io.*;
import java.net.*;
import java.util.*;

public class MyReplica implements Replica {

  ArrayList<String> database = new ArrayList<String>();
  ArrayList<String> messageQueue = new ArrayList<String>();
  static boolean isPrimary = false;
  private static Replica myStub; // Keeps track of the object exported to the RMI registry (so we can unexport it when need be)
  private static Registry exportedRegistry; // Keeps track of the registry that is exported to the RMI registry (upon its creation)
  private static final int pPORT = Values.PRIMARY.getValue();
  private static final int bPORT = Values.BACKUP.getValue();
  private static FileWriter writeR;
  private static PrintWriter printer;
  private static FileReader readeR;
  private static BufferedReader buffReader;
  private static String writePath = "";

  public MyReplica() throws RemoteException{
    super();
  }

  public static void main(String[] args) throws IOException, InterruptedException {

    // If a Primary node already exists in the registry, a Backup node is created, otherwise a Primary node is created
    // Note: the lookup() function returns an error when it cannot find the specified hostname in the registry. The presence of this error is used as an indicator for whether a Primary node already exists in the registry.
    try {
      Registry registry = LocateRegistry.getRegistry(pPORT);
      registry.lookup("Primary");
      System.out.println("Creating backup");
      bindRegistryEntry("Backup");
      isPrimary = false;
    } catch (Exception e) {
      System.err.println("Primary not found in registry");
      System.out.println("Creating primary");
      isPrimary = true;
      try {
        bindRegistryEntry("Primary");
      } catch (Exception f) {
          System.err.println("Could not create primary");
      }
    } finally {
        sendHeartBeat();
    }

  }

  /**
  * Based on the node being created, a log file is bound to it and the node is bode in the registry.
  * The default ports for Primary and Backup node are handled in Values.java
  *
  * @author Qudus Animashaun, Daniel Namu-Fetha
  * @param nodeName Name of node to be bound in the RMI registry
  */
  public static void bindRegistryEntry(String nodeName) throws RemoteException{
    writePath = (isPrimary) ? "primary_db.txt" : "backup_db.txt";
    try {
      int PORT = (nodeName == "Primary") ? pPORT : bPORT;
      MyReplica node = new MyReplica();
      Replica stub = (Replica) UnicastRemoteObject.exportObject(node, PORT);
      System.out.println("PORT: " + PORT);
      myStub = node;
      exportedRegistry = LocateRegistry.createRegistry(PORT);
      Registry registry = LocateRegistry.getRegistry(PORT);
      // Binds it in the registry <-- this is where the Primary or Backup are registered in the registry
      registry.rebind(nodeName, stub);
      System.out.println(nodeName + " bound in registry");
      if(!isPrimary) node.join("Primary");
    }catch(ExportException e){
      System.err.println("bindRegistryEntry error: " + e.getMessage());
    } catch (Exception e) {
      System.err.println(nodeName + " is not bound in the registry");
      e.printStackTrace();
    }
  }

  /**
  * Responsible for sending a heartbeat to both node by both nodes every 10 seconds. If the Primary node misses 3 heartbeats, the Backup assumes it is dead and will take over as the new Primary.
  *
  * @author Daniel Namu-Fetha
  */
  public static void sendHeartBeat() {

    String receiverName = "";
    int receiverPort = 0;
    int beatMissCounter = 0;

    if (isPrimary) {
      receiverName = "Backup";
      receiverPort = bPORT;
    } else {
      receiverName = "Primary";
      receiverPort = pPORT;
    }

    // continuously sends out a heartbeat
    while (true){
      try {
        Registry reg = LocateRegistry.getRegistry(receiverPort);
        Replica receiverObj = (Replica) reg.lookup(receiverName);
        System.out.println("Sending heartbeat to: " + receiverName);
        if (receiverObj.heartBeat()) System.out.println("Heartbeat to " + receiverName + " succeeded!");
      } catch (Exception e) {
        System.err.println("Error sending heart to: " + receiverName);
        System.err.println("Sending heartbeat error: " + e.getMessage());
        beatMissCounter += 1;
      } finally {
        try {
          Thread.sleep(10000); // wait for 10 seconds
        } catch (Exception e) {
          System.err.println("Thread sleep error :" + e.getMessage());
        }
      }
      if (beatMissCounter == 3){ // 3 heartbeats missed
        if (!isPrimary){
          try{
            Registry reg = LocateRegistry.getRegistry(bPORT);
            Replica backup = (Replica) reg.lookup("Backup");
            backup.kill();
            beatMissCounter = 0;
          } catch (Exception e) {
              System.err.println("Lack of heartbeat kill error: " + e.getMessage());
          } finally {
            sendHeartBeat();
          }
        }
      }
    }

  }

  // simply returns true to confirm liveliness
  public boolean heartBeat() {
    return true;
  }

  /**
  * Writes data to a local ArrayList and to a text file for the respective node.
  *
  * @author Qudus Animashaun, Daniel Namu-Fetha
  * @param data   The data to be written
  * @param sender The node that is sending this write request
  */
  public boolean write (String data, Values  sender) {

    boolean appendToFile = true;

    if(isPrimary){
      System.out.println("<Primary>: Received "+data);
    }else{
      System.out.println("<Backup>: Received "+data);
    }

    String writer = (isPrimary) ? "Primary" : "Backup";
    System.out.print("<" + writer + ">: ");
    System.out.println(writer + " writing to database: " + data);

    // This checks whether the call to the write function was made by the Primary node to propogate the Client's request
    if(!isPrimary && sender == Values.PRIMARY){
      database.add(data);
      System.out.println("Write path: " + writePath);

      try {
        writeR = new FileWriter(writePath, appendToFile);
        printer = new PrintWriter(writeR);
        printer.print(data + "\n");
		//printer.flush();
        System.out.println("Successfully wrote to file".toUpperCase());
      } catch (Exception e) {
        System.err.println("Error writing to db file: " + e.getMessage());
      } finally {
        printer.close();

      }

    }

    // This ensures that the Client's request is handled by the Primary node and propogated to the Backup node
    if(isPrimary){
      database.add(data);

      try {
        writeR = new FileWriter(writePath, appendToFile);
        printer = new PrintWriter(writeR);
        printer.print(data + "\n");
        System.out.println("Successfully wrote to file".toUpperCase());
      } catch (Exception e) {
        System.err.println("Error writing to db file: " + e.getMessage());
      } finally {

        printer.close();
      }

      Boolean propSuccess = false;
      propSuccess = propogate(data);
      if (!propSuccess) messageQueue.add(data);
      System.out.println("Propogate success: " + propSuccess);
    }

    // If the Client speaks directly with the Backup node, it assumes that the Primary has crashed, therefore begins to replace it
    if (!isPrimary && sender == Values.CLIENT) kill();

    return true;
  }

  /**
  * Propogates any write statements received by the Primary node to the Backup node
  *
  * @author Qudus Animashaun, Daniel Namu-Fetha
  * @param data   The data to be propogated
  */
  private boolean propogate(String data) {
    try {
      Registry reg = LocateRegistry.getRegistry(bPORT);
      Replica backupObj = (Replica) reg.lookup("Backup");
      if (backupObj != null) backupObj.write(data, Values.PRIMARY);
    } catch (Exception e) {
      System.err.println("Propogation error: " + e.getMessage());
      // messageQueue.add(data);
      return false;
    }
    return true;
  }

  /**
  * Data is read from the Primary node's text file
  *
  * @author Qudus Animashaun, Daniel Namu-Fetha
  */
  public String read() {

    try{
        readeR = new FileReader(writePath);
    } catch (Exception e) {
      System.err.println("Could find file to read from.");
    }
    buffReader = new BufferedReader(readeR);
    ArrayList<String> buffer = new ArrayList<String>();

    String data = "";

    while (data != null) {
      if (data != "") buffer.add(data);
      try{
        data = buffReader.readLine();
      } catch (Exception e) {
        System.err.println("Couldn't read line from file.");
      }
    }

    return Arrays.toString(buffer.toArray());
  }

  /**
  * This function is used by the Backup to tell the Primary that it has successfully restarted. Upon calling this function the Primary transfers a messageQueue (containing transactions missed by the Backup during down-time) to the Backup for it to catch up to the current state of the Primary node.
  *
  * @author Qudus Animashaun, Daniel Namu-Fetha
  * @param joinWithWho The node that should handle the join function next
  */
  public void join(String joinWithWho){

    if (isPrimary) {
      try {
        Registry reg = LocateRegistry.getRegistry(bPORT);
        Replica backup = (Replica) reg.lookup("Backup");
        boolean stateTransferSuccess = backup.stateTransfer(messageQueue);
        System.out.println("State transfer success: " + stateTransferSuccess);
		if (stateTransferSuccess) messageQueue.clear();
      } catch (Exception e) {
        System.err.println("Primary couldn't transfer state");
      }
    } else {
      try {
        // Gets Primary remote object and "joins" it
        Registry reg = LocateRegistry.getRegistry(pPORT);
        Replica prim = (Replica) reg.lookup("Primary");
        prim.join("Backup");
      } catch (Exception e) {
        System.err.println("Backup join error: " + e.getMessage());
      }
    }

  }

  // NOTE: This function is called on an instance of the Backup node
  public boolean stateTransfer(ArrayList<String> messageQueue) {

    System.out.println("Message queue to be transferred: " + Arrays.toString(messageQueue.toArray()));

    // iterates through messageQueue and processes misssed transactions
    for (String transaction : messageQueue) {
      write(transaction, Values.PRIMARY);
    }

    return true;

  }

  /**
  * This function is used by Backup to replace the Primary node. The Primary node is unbinded from its registry and then the Backup binds itself to that "primary" registry.
  *
  * @author Daniel Namu-Fetha
  */
  public void kill() {

    try {
      Registry registry = LocateRegistry.getRegistry(bPORT);
      Replica backupStub = (Replica) registry.lookup("Backup");
      System.out.println("Obtained Backup registry");
      UnicastRemoteObject.unexportObject(myStub, true); // unexport backup stub from old RMI port <-- prevents "ObjID already taken" error
      System.out.println("Unexported Backup stub from RMI");
      Replica stub = (Replica) UnicastRemoteObject.exportObject(this, pPORT); // export backup stub to new RMI port
      myStub = stub;
      System.out.println("Exported Backup to primary port");

      UnicastRemoteObject.unexportObject(exportedRegistry, true); // unexport backup registry from old RMI port <-- allows a new Backup registry to be created later (after our current Backup becomes the new Primary)
      exportedRegistry = LocateRegistry.createRegistry(pPORT);
      Registry prim = LocateRegistry.getRegistry(pPORT);
      prim.rebind("Primary", stub);
      System.out.println("New Primary Registry created");
      isPrimary = true; // the backup node is now a primary node
    } catch (Exception e) {
      System.err.println("Kill function error: " + e.getMessage());
    }

  }

}
