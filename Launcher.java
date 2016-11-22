import venturas.app.*;
import venturas.mtcp.sockets.*;
import venturas.mtcp.io.*;
import java.net.*;
import java.util.*;
import java.io.*;

public class Launcher {
	public static void main(String args[]) throws InterruptedException, IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		// java Launcher Server localhost:9030:10030,localhost:
		// java Launcher Client localhost:9030
		if (args.length != 2) {
			kill();
		}

		switch (args[0]) {
		case "-s": runServer(args); break;
		case "-c": runClient(args); break;
		default: kill();
		}
	}

	private static void runServer(String[] args) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		String[] single = args[1].split(",");
		List<AddressPortTuple> locations = new LinkedList<AddressPortTuple>();
		for (int i = 0; i < single.length; i++) {
			String[] addressAndPorts = single[i].split(":");
			InetAddress address = (new InetSocketAddress(addressAndPorts[0], 0)).getAddress();
			int portA = Integer.parseInt(addressAndPorts[1]);
			int portB = Integer.parseInt(addressAndPorts[2]);
			AddressPortTuple apt = new AddressPortTuple(address, portA, portB);
			locations.add(apt);
		}
		for (int i = 0; i < locations.size(); i++) {
			Server s = new Server(i, locations);
			s.start();
		}
	}

	private static void runClient(String[] args) throws InterruptedException, IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		String[] split = args[1].split(":");
		InetSocketAddress addressPort = new InetSocketAddress(split[0], Integer.parseInt(split[1]));
		System.err.println(addressPort.toString());
		Client c = new Client(addressPort);
		c.start();
	}

	private static void kill() {
		System.err.println("Usage:");
		System.err.println("For server: java Launcher -s address1:port1a:port1b,address2:port2a:port2b,...");
		System.err.println("For client: java Launcher -c addressToServer:portToConnectOn");
		System.exit(1);
	}
}
