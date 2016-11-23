package venturas.app;

import java.net.*;
import java.io.*;
import java.util.*;
import venturas.mtcp.sockets.*;
import venturas.mtcp.io.*;

public class Server {
	private MigratableServerSocket serverSocket;

	public Server(String address, int clientPort, int serverPort, List<AddressPortTuple> otherServers) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
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




	//java Server {me} {everyone}
	//e.g. Two parties:
	//	 java Server localhost:9030:10030 localhost:9030:10030,localhost:9031:10031
	//	 java Server localhost:9031:10031 localhost:9030:10030,localhost:9031:10031



	public static void main(String args[]) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException, InterruptedException {

		if (args.length != 2) {
			logError("Need 2-length args, will exit");
			System.exit(1);
		}
		String[] me = args[0].split(":");
		String[] all = args[1].split(",");
		List<AddressPortTuple> otherServers = new LinkedList<>();
		for (String server : all) {
			String[] addrPort = server.split(":");
			boolean thisIsMe = true;
			for (int i = 0; i < addrPort.length; i++) {
				if (!addrPort[i].equals(me[i])) {
					thisIsMe = false;
				}
			}
			if (!thisIsMe) {
				AddressPortTuple apt = new AddressPortTuple(addrPort[0], Integer.parseInt(addrPort[1]), Integer.parseInt(addrPort[2]));
				otherServers.add(apt);
			}
		}
		log("OTHERS:" + otherServers.toString());
		log("ME:" + java.util.Arrays.toString(me));
		Server s = new Server(me[0], Integer.parseInt(me[1]), Integer.parseInt(me[2]), otherServers);
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









		// List<AddressPortPair> l = new LinkedList<>();
		// for (int i = 0; i < numOtherServers; i++) {
		// 	if (i != myServerNumber) {
		// 		l.add(new AddressPortPair(new InetSocketAddress("localhost", 9030 + i).getAddress(), 9030 + i));
		// 	}
		// }
		//
		// Server s = new Server(9030 + myServerNumber, 10030 + myServerNumber, l);
		// log("Started...");
		// MigratableServerSocket mserversckt = s.serverSocket;
		//
		// QueuedObjectOutputStream qos = mserversckt.getOutputStream();
		// QueuedObjectInputStream qis = mserversckt.getInputStream();
		//
		// Integer i;
		// while (true) {
		// 	i = (Integer)qis.readObject();
		// 	log("<server> Got " + i);
		// 	i++;
		// 	Thread.sleep(500);
		// 	if (i > 10) {
		// 		log("Forcing MEGA-long sleep on i > 10");
		// 		Thread.sleep(500000);
		// 	}
		// 	qos.writeObject(i);
		// }
	}

	private static void log(String message) {
		System.out.println("<server> " + message);
	}

	private static void logError(String message) {
		System.err.println("<server> " + message);
	}
}
