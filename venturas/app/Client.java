package venturas.app;

import java.net.*;
import venturas.mtcp.sockets.*;
import venturas.mtcp.io.*;
import java.io.*;

public class Client {
	private MigratableSocket socket;

	public Client(InetSocketAddress socketAddress) throws MTCPHandshakeException, InterruptedException, ClassNotFoundException, IOException {
		socket = new MigratableSocket(socketAddress);
	}

	public static void main(String[] args) throws MTCPHandshakeException, InterruptedException, ClassNotFoundException, IOException {
		System.err.println("<client> Started!");
		Client c = new Client(new InetSocketAddress("localhost", 9030));
		MigratableSocket msocket = c.socket;

		QueuedObjectOutputStream qos = msocket.getOutputStream();
		QueuedObjectInputStream qis = msocket.getInputStream();
		Integer i = 0;
		while (true) {
			Thread.sleep(500);
			qos.writeObject(i);
			i = (Integer)qis.readObject();
			System.err.println("<client> Got " + i);
			i++;
		}

	}
}
