// package venturas.app;
//
// import venturas.mtcp.io.*;
// import venturas.mtcp.sockets.*;
// import java.net.*;
// import venturas.mtcp.io.*;
// import java.util.*;
// import java.util.LinkedList;
//
// public class TestSerApp {
//   public static void main(String[] args) throws Exception {
//     MServerSock s = new MServerSock(Integer.parseInt(args[0]), Integer.parseInt(args[1]), new LinkedList<AddressPortTuple>());
// 	s.accept();
// 	System.err.println("Constructed an MSS, now waiting for a client");
// 	while (!s.hasClient()) {
// 		//block
// 		System.err.println(s.hasClient());
// 		try { Thread.sleep(3000); } catch (InterruptedException e) {}
// 	}
//     MigratoryObjectInputStream ois = new MigratoryObjectInputStream(s.getInputStream());
//     log("got in stream");
//     LinkedList<String> result = (LinkedList<String>) ois.readObject();
//     log("result found!");
//     System.err.println(result.toString());
//   }
//
//   private static void logError(Object o) {
//       System.err.println(o.toString());
//   }
//
//   private static void log(Object o) {
//       System.err.println(o.toString());
//   }
// }

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

        MServerSock serverSocket = new MServerSock(Integer.parseInt(me[1]), Integer.parseInt(me[2]), otherServers);
		serverSocket.accept();

        while (!serverSocket.hasClient()) {
            log("FUCK");
			Thread.sleep(500);
        }

		log("Got past accept call (remember, is non blocking)");
		MigratoryObjectOutputStream qos = new MigratoryObjectOutputStream(serverSocket.getOutputStream());
		MigratoryObjectInputStream qis = new MigratoryObjectInputStream(serverSocket.getInputStream());

		log("Entering while...");

		Integer i;
		while (true) {
			log("Okay, I'm gonna read something");
			i = (Integer)qis.readObject();
			log("Server YEEEEESSSSS app did a read");
			log("<server> Got " + i);
			i += 2;
			Thread.sleep(500);
			if (i == 5) {
				log("Forcing MEGA-long sleep on i == 5");
				Thread.sleep(10000);
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
