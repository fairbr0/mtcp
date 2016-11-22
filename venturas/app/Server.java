package venturas.app;

import java.net.*;
import java.io.*;
import java.util.*;
import venturas.mtcp.sockets.*;
import venturas.mtcp.io.*;

public class Server {
	private MigratableServerSocket serverSocket;

	public Server(int clientPort, int serverPort) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		this(clientPort, serverPort, null);
	}

	public Server(int clientPort, int serverPort, List<AddressPortPair> otherServers) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		serverSocket = new MigratableServerSocket(clientPort, serverPort, otherServers);
		(new Thread(() -> {
			try {
				serverSocket.acceptClient();
			} catch (Exception e) {
				e.printStackTrace();
			}
		})).start();
		(new Thread(() -> {
			try {
				serverSocket.acceptServer();
			} catch (Exception e) {
				e.printStackTrace();
			}
		})).start();
	}

	public static void main(String args[]) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException, InterruptedException {

		if (args.length != 2) {
			System.exit(1);
		}
		int numOtherServers = Integer.parseInt(args[0]);
		int myServerNumber = Integer.parseInt(args[1]);
		List<AddressPortPair> l = new LinkedList<>();
		for (int i = 0; i < numOtherServers; i++) {
			if (i != myServerNumber) {
				l.add(new AddressPortPair(new InetSocketAddress("localhost", 9030 + i).getAddress(), 9030 + i));
			}
		}

		Server s = new Server(9030 + myServerNumber, 10030 + myServerNumber, l);
		log("Started...");
		MigratableServerSocket mserversckt = s.serverSocket;

		QueuedObjectOutputStream qos = mserversckt.getOutputStream();
		QueuedObjectInputStream qis = mserversckt.getInputStream();

		Integer i;
		while (true) {
			i = (Integer)qis.readObject();
			log("<server> Got " + i);
			i++;
			Thread.sleep(500);
			if (i > 10) {
				log("Forcing MEGA-long sleep on i > 10");
				Thread.sleep(500000);
			}
			qos.writeObject(i);
		}
	}

	private static void log(String message) {
		System.out.println("<server> " + message);
	}

	private static void logError(String message) {
		System.err.println("<server> " + message);
	}
}
