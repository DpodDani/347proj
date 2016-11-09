/**
 * The client socket returns an appropriate client socket instance.
 *
 * @author Daniel Name-Fetha
 * @version 1.0
 */

import java.io.*;
import java.net.*;
import java.rmi.server.*;

public class ClientSocketFactory implements RMIClientSocketFactory, Serializable {

    public ClientSocketFactory() {
	// empty
    }

    public Socket createSocket(String host, int port) throws IOException {
	return new Socket(host, port);
    }

    public boolean equals(Object obj) {
	return getClass() == obj.getClass();
    }

}
