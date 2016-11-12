import java.io.*;
import java.net.*;
import java.rmi.Naming; 
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
 
public class ClientRMI {
    public static void main(String[] args) throws IOException {
         
        InputStream is = null;
        BufferedReader br = null;
        
        try {

            Registry reg = LocateRegistry.getRegistry(1099);
                // Client looks for the Primary node in the registry
                System.out.println(Arrays.toString(reg.list()));
                Replica object = (Replica) reg.lookup("Primary");
            is = System.in;
            br = new BufferedReader(new InputStreamReader(is));
            System.out.println("Please enter query - 'R:' or 'W:' <item to add>'");
            System.out.print("<client>: ");
            String line = null;
            
            while (true) {
                line = br.readLine();
                if(!line.trim().equalsIgnoreCase("")){
                    if(line.length() <2){
                        throw new Exception("\t<Invalid type entered, please choose from \"R:\" for READ or \"W:\"> for WRITE");
                    }
                    String key = line.substring(0,2);

                    if (line.equalsIgnoreCase("quit")) {
                        break;
                    }

                    switch(key){
                        case "R:":
                            System.out.print("<client>: ");
                            System.out.println(object.read());
                            System.out.print("<client>: ");
                            break;
                        case "W:":
                            System.out.print("<client>: ");
                            object.write(line.substring(3), Values.CLIENT);
                            break;
                        default:
                            throw new Exception("\t<Invalid type entered, please choose from \"R\" for READ or \"W\"> for WRITE");
                    }

                }else{
                    System.out.print("<client>: ");
                }
                
            }
            
        }
        catch (IOException ioe) {
            System.out.println("Exception while reading input " + ioe);
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