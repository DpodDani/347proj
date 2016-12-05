import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class ClientRMI {

    private static String logged=null;
    private static Replica object;
    private static String line;
    private static InputStream is = null;
    private static BufferedReader br = null;

    public static void main(String[] args) throws IOException {
       inputMethod(true, "");
   }

    /**
     *  Description: Parses the command from the client and sends it to Primary
     *
     *  @author Qudus Animashaun
     *  @param  line  Client's input in the command line
     */
    public static void transaction(String line){
        try{
            String key = line.substring(0,2);
            switch(key){
                case "R:":
                System.out.print("<client>: ");
                System.out.println(object.read());
                System.out.print("<client>: ");
                break;
                case "W:":
                System.out.print("<client>: ");
                logged = line.substring(3);
                object.write(line.substring(3), Values.CLIENT);
                break;
                default:
                throw new Exception("\t<Invalid type entered, please choose from \"R\" for READ or \"W\"> for WRITE");
            }
        }catch(Exception e){
            try{
                Registry reg = LocateRegistry.getRegistry(1100);
                object = (Replica) reg.lookup("Backup");
                if(object != null) object.write(logged, Values.CLIENT);
                inputMethod(false, line);
            }catch(Exception p){

            }
        }
    }

    /**
     *  Description: Parses the command from the client and sends it to Primary
     *
     *  @author Qudus Animashaun
     *  @param  missedLine  Stores the line that did not proprogate to new Primary
     *  @param  token  Turns false if there was a switch and last command did not proprogate
     */
    public static void inputMethod(boolean token, String missedLine){
        try {
            Registry reg = LocateRegistry.getRegistry(1099);
            // Client looks for the Primary node in the registry
            object = (Replica) reg.lookup("Primary");
            is = System.in;
            if(br== null) br = new BufferedReader(new InputStreamReader(is));
            if(token) System.out.println("Please enter query - 'R:' or 'W:' <item to add>'");
            System.out.print("<client>: ");
            line = null;
            if(!token){
                transaction(missedLine);
            }
            while (true) {
                line = br.readLine();
                if(!line.trim().equalsIgnoreCase("")){
                    if(line.length() <2){
                        throw new Exception("\t<Invalid type entered, please choose from \"R:\" for READ or \"W:\"> for WRITE");
                    }
                    if (line.equalsIgnoreCase("quit")) {
                        break;
                    }
                    transaction(line);
                }else{
                    System.out.print("<client>: ");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            // close the streams using close method
            try {
                if (br != null) {
                    br.close();
                }
            }
            catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
    }
}
