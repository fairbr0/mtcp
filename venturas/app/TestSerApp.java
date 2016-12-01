package venturas.app;

import venturas.mtcp.io.*;
import venturas.mtcp.sockets.*;
import java.net.*;
import venturas.mtcp.io.*;
import java.util.*;

public class TestSerApp {
  public static void main(String[] args) throws Exception {
    MServerSock s = new MServerSock(9030, 10030, new LinkedList<AddressPortTuple>());
	s.accept();
	System.err.println("Constructed an MSS, now waiting for a client");
	while (!s.hasClient()) {
		//block
		System.err.println(s.hasClient());
		try { Thread.sleep(3000); } catch (InterruptedException e) {}
	}
	System.err.println("Got my client!");
    String str = "Hello there!";
	System.err.println("Sending");
    MigratoryObjectOutputStream sout = new MigratoryObjectOutputStream(s.getOutputStream());
	System.err.println("Sent! Now writing");
    sout.writeObject(str);
	System.err.println("Wrote! Bye");
  }
}
