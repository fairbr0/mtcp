// package venturas.app;
//
// import venturas.mtcp.io.*;
// import venturas.mtcp.sockets.*;
//
// import java.util.LinkedList;
// import java.net.*;
//
// public class TestCliApp {
//
//     public static void main(String[] args) throws Exception {
//         InetAddress address = (new InetSocketAddress(args[0], Integer.parseInt(args[1]))).getAddress();
//         MSock c = new MSock(address, Integer.parseInt(args[1]));
//         log("did socket");
//
//
//
//     	System.err.println("Got my client!");
//         LinkedList<String> str = new LinkedList<String>();
//         str.add("Jarred");
//         str.add("is");
//         str.add("a");
//         str.add("massive");
//         str.add("lovely person");
//     	str.add("but");
//     	str.add("actually");
//     	str.add("Jake");
//     	str.add("is");
//     	str.add("the");
//     	str.add("biggest");
//     	str.add("person");
//     	str.add("ever");
//     	str.add("and");
//     	str.add("Scambault");
//     	str.add("is");
//     	str.add("a");
//     	str.add("personier");
//     	str.add("person");
//     	str.add("than");
//     	str.add("Jake");
//     	str.add("which");
//     	str.add("is");
//     	str.add("hard");
//     	str.add("because");
//     	str.add("`Jake`");
//     	str.add("is");
//     	str.add("a");
//     	str.add("mere");
//     	str.add("synonym");
//     	str.add("of");
//     	str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");
//     	str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");str.add("person");
//         str.add("Jake");
//         str.add("appreciation");
//         str.add("group");
//         str.add("project");
//     	System.err.println("Sending");
//         MigratoryObjectOutputStream sout = new MigratoryObjectOutputStream(c.getOutputStream());
//     	System.err.println("Sent! Now writing");
//         sout.writeObject(str);
//     	System.err.println("Wrote! Bye");
//
//
//
//
//
//     }
//
//     private static void logError(Object o) {
//         System.err.println(o.toString());
//     }
//
//     private static void log(Object o) {
//         System.err.println(o.toString());
//     }
// }

package venturas.app;

import java.net.*;
import venturas.mtcp.sockets.*;
import venturas.mtcp.io.*;
import java.io.*;

public class TestCliApp {
	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.println("Did not get a port number from script, assuming S1 is on port 9031");
			args = new String[2];
            args[0] = "localhost";
			args[1] ="9031";
		}

        InetSocketAddress a = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
		MSock msocket = new MSock(a.getAddress(), a.getPort());
		MigratoryObjectOutputStream qos = new MigratoryObjectOutputStream(msocket.getOutputStream());
		MigratoryObjectInputStream qis = new MigratoryObjectInputStream(msocket.getInputStream());
		Integer i = 0;
		while (true) {
			Thread.sleep(3000);
			qos.writeObject(i);
			log("Write " + i + " and wait for read...");
			i = i + 1;
			if (i == 10) {
				msocket.migrate();
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
