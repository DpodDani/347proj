import java.io.*;
import java.net.*;
import java.rmi.server.*;

public class ServerSocketFactory implements RMIServerSocketFactory {

    public ServerSocketFactory() {
	// empty
    }

    public ServerSocket createServerSocket(int port) throws IOException {
	return new ServerSocket(port);
    }

    public boolean equals(Object obj) {
	return getClass() == obj.getClass();
    }

}
