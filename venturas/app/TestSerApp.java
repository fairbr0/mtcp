package venturas.app;

import java.net.*;
import java.io.*;
import java.util.*;
import venturas.mtcp.sockets.*;
import venturas.mtcp.io.*;

public class TestSerApp {

	//java Server {me} {everyone}
	//e.g. Two parties:
	//	 java Server localhost:9030:10030 localhost:9030:10030,localhost:9031:10031
	//	 java Server localhost:9031:10031 localhost:9030:10030,localhost:9031:10031

	public static void main(String args[]) throws Exception {

		if (args.length != 2) {
			logError("Need 2-length args, will exit");
			System.exit(1);
		}

		String[] me = args[0].split(":");
		String[] all = args[1].split(",");
		List<AddressPortTuple> otherServers = new LinkedList<>();
		for (String server : all) {
			String[] addrPort = server.split(":");
			AddressPortTuple apt = new AddressPortTuple(addrPort[0], Integer.parseInt(addrPort[1]), Integer.parseInt(addrPort[2]));
			otherServers.add(apt);
		}
		log("OTHERS:" + otherServers.toString());
		log("ME:" + java.util.Arrays.toString(me));

        MServerSock serverSocket = new MServerSock(Integer.parseInt(me[1]), Integer.parseInt(me[2]), otherServers);
		serverSocket.accept();

        while (!serverSocket.hasClient()) {
            log("waiting on client");
			Thread.sleep(1000);
        }

		log("Got past accept call (remember, is non blocking)");
		MigratoryOutputStream qos = serverSocket.getOutputStream();
		MigratoryInputStream qis = serverSocket.getInputStream();

		log("Entering while...");


		while (true) {
			log("Okay, I'm gonna read something");
			byte[] b = qis.readBytes();
			log("Got " + b[0]);
			Thread.sleep(1000);
			// if (b[0] == 5) {
			// 	log("Forcing MEGA-long sleep on i == 5");
			// 	Thread.sleep(1000);
			// }

			// qos.writeBytes(b);
		}
	}

	private static void log(String message) {
		System.out.println("<server> " + message);
	}

	private static void logError(String message) {
		System.err.println("<server> " + message);
	}
}
