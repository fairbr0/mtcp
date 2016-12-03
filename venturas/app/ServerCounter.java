package venturas.app;

import java.net.*;
import java.io.*;
import java.util.*;
import venturas.mtcp.sockets.*;
import venturas.mtcp.io.*;

public class ServerCounter {

	//java Server {me} {everyone}
	//e.g. Two parties:
	//	 java Server public:9030:private:10030 localhost:9030:10030,localhost:9031:10031
	//	 java Server localhost:9031:10031 localhost:9030:10030,localhost:9031:10031

	public static void main(String args[]) throws Exception {

		if (args.length != 2) {
			logError("Need 2-length args, will exit");
			System.exit(1);
		}

		String[] me = args[0].split(":");
		String[] all = args[1].split(",");
		List<AddressMapping> otherServers = new LinkedList<>();
		for (String server : all) {
			String[] addrPort = server.split(":");
			AddressMapping apt = new AddressMapping(addrPort[0], Integer.parseInt(addrPort[1]), addrPort[2], Integer.parseInt(addrPort[3]));
			otherServers.add(apt);
		}
		log("OTHERS:" + otherServers.toString());
		log("ME:" + java.util.Arrays.toString(me));

        MServerSock serverSocket = new MServerSock(Integer.parseInt(me[1]), Integer.parseInt(me[3]), otherServers);
		serverSocket.accept();

		Integer sum = 0;

        while (true) {
			try {
				while (!serverSocket.hasClient()) {
	            	log("waiting on client");
					Thread.sleep(1000);
	        	}
				State<Integer> state = serverSocket.importState();
				System.err.println("re constructed state----------");
				Integer reconstructedState = null;
				if (state.getState() != null) {
					reconstructedState = state.getState();
					Iterator<byte[]> it = state.getBufferIn().iterator();
					while (it.hasNext()) {
						byte[] next = it.next();
						for (int i = 0; i < next.length; i++) {
							reconstructedState += (int)next[i];
						}
					}
					sum = reconstructedState;
				} else {
					sum = 0;
				}
				System.err.println(sum);
				System.err.println("----------");

				log("Got past accept call (remember, is non blocking)");
				MigratoryOutputStream qos = serverSocket.getOutputStream();
				MigratoryInputStream qis = serverSocket.getInputStream();

				log("Entering while...");

				while (true) {
					log("Okay, I'm gonna read something");

					byte[] b = qis.readBytes();
					log("Got " + b[0]);
					sum += (int)b[0];
					Thread.sleep(500);
					if (b[0] % 3 == 0) {
						serverSocket.exportState(new State<Integer>(sum));
					}
				}
			} catch (MTCPStreamMigratedException e) {
				System.err.println("(((SEERVER)))STREAM MIGRATED EXCEPTION!!!!!!!");
			}
		}
	}

	private static void log(String message) {
		System.out.println("<server> " + message);
	}

	private static void logError(String message) {
		System.err.println("<server> " + message);
	}
}
