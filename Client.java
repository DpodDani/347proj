import java.io.*;
import java.net.*;
 
public class Client {
    public static void main(String[] args) throws IOException {
         
        if (args.length != 2) {
            System.err.println(
                "Usage: java Client <host name> <primary port number>");
            System.exit(1);
        }
 
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
 
        try {
            Socket kkSocket = new Socket(hostName, portNumber);
            ObjectOutputStream out = new ObjectOutputStream(kkSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(kkSocket.getInputStream());
            
            //Message either "R" or "W <item>"
            out.writeObject("R");
            String message = (String) in.readObject();
            System.out.println("Read: "+message);

        }   catch (ClassNotFoundException e) {
            System.out.println("Read object was not of the expected class");
            System.out.println(e.getMessage());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        }
    }
}