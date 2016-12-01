package venturas.app;

import venturas.mtcp.io.*;
import venturas.mtcp.sockets.*;
import java.net.*;
import venturas.mtcp.io.*;
import java.util.*;
import java.util.LinkedList;

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
    LinkedList<String> str = new LinkedList<String>();
    str.add("Jarred");
    str.add("is");
    str.add("a");
    str.add("massive");
    str.add("lovely person");
	str.add("but");
	str.add("actually");
	str.add("Jake");
	str.add("is");
	str.add("the");
	str.add("biggest");
	str.add("cunt");
	str.add("ever");
	str.add("and");
	str.add("Scambault");
	str.add("is");
	str.add("a");
	str.add("cuntier");
	str.add("cunt");
	str.add("than");
	str.add("Jake");
	str.add("which");
	str.add("is");
	str.add("hard");
	str.add("because");
	str.add("`Jake`");
	str.add("is");
	str.add("a");
	str.add("mere");
	str.add("synonym");
	str.add("of");
	str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");
	str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");str.add("cunt");

	System.err.println("Sending");
    MigratoryObjectOutputStream sout = new MigratoryObjectOutputStream(s.getOutputStream());
	System.err.println("Sent! Now writing");
    sout.writeObject(str);
	System.err.println("Wrote! Bye");
  }
}
