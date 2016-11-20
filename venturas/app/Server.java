package venturas.app;

import java.net.*;
import java.io.*;
import venturas.mtcp.sockets.*;
import venturas.mtcp.io.*;

public class Server {
	private MigratableServerSocket serverSocket;

	public Server(int portA, int portB) throws IOException, ClassNotFoundException, MTCPHandshakeException {
		serverSocket = new MigratableServerSocket(portA, portB);
		serverSocket.acceptClient();
	}

	public static void main(String args[]) throws IOException, ClassNotFoundException, MTCPHandshakeException, InterruptedException {

		System.err.println("<server> Started...");
		Server s = new Server(9030, 10030);
		MigratableServerSocket mserversckt = s.serverSocket;

		QueuedObjectOutputStream qos = mserversckt.getOutputStream();
		QueuedObjectInputStream qis = mserversckt.getInputStream();

		Integer i;
		while (true) {
			i = (Integer)qis.readObject();
			System.err.println("<server> Got " + i);
			i++;
			Thread.sleep(500);
			qos.writeObject(i);
		}
	}
}
