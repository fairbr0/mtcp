package venturas.app;

import java.net.*;
import venturas.mtcp.sockets.*;
import venturas.mtcp.io.*;
import java.io.*;

public class ClientCounter {
	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.println("Did not get a port number from script, assuming S1 is on port 9031");
			args = new String[2];
            args[0] = "localhost";
			args[1] ="9031";
		}

        InetSocketAddress a = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
		MSock msocket = new MSock(a.getAddress(), a.getPort());
		MigratoryOutputStream qos = msocket.getOutputStream();
		MigratoryInputStream qis = msocket.getInputStream();
		int i = 0;
		while (true) {
			byte[] b = {(byte)i};
			try {
				qos.writeBytes(b);
				i += 1;
				if (i % 10 == 0) {
					Thread.sleep(MSock.TIMEOUT * 10);
				}
			} catch (MTCPStreamMigratedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void log(String message) {
		System.out.println("<client> " + message);
	}

	private static void logError(String message) {
		System.err.println("<client> " + message);
	}
}
