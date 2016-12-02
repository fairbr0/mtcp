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

		if (args.length != 1) {
			System.err.println("Did not get a port number from script, assuming S1 is on port 9031");
			args = new String[1];
			args[0] ="9031";
		}

		Client c = new Client(new InetSocketAddress("localhost", Integer.parseInt(args[0])));
		log("Started!");
		MigratableSocket msocket = c.socket;
		QueuedObjectOutputStream qos = msocket.getOutputStream();
		QueuedObjectInputStream qis = msocket.getInputStream();
		Integer i = 0;
		while (true) {
			Thread.sleep(3000);
			qos.writeObject(i);
			log("Write " + i + " and wait for read...");
			i = i + 1;
		}
	}

	private static void log(String message) {
		System.out.println("<client> " + message);
	}

	private static void logError(String message) {
		System.err.println("<client> " + message);
	}
}
