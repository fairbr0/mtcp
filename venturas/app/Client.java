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

		Client c = new Client(new InetSocketAddress("localhost", 9030));
		log("Started!");
		MigratableSocket msocket = c.socket;
		QueuedObjectOutputStream qos = msocket.getOutputStream();
		QueuedObjectInputStream qis = msocket.getInputStream();
		Integer i = 0;
		while (true) {
			Thread.sleep(500);
			qos.writeObject(i);
			log("Write " + i + " and wait for read...");
			int j = (Integer)qis.readObject();
			log("Got " + j);
			i = j + 1;
		}
	}

	private static void log(String message) {
		System.out.println("<client> " + message);
	}

	private static void logError(String message) {
		System.err.println("<client> " + message);
	}
}
