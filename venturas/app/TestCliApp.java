package venturas.app;

import venturas.mtcp.io.*;
import venturas.mtcp.sockets.*;

import java.util.LinkedList;
import java.net.*;

public class TestCliApp {

    public static void main(String[] args) throws Exception {
        InetAddress address = (new InetSocketAddress("localhost", 9030)).getAddress();
        MSock c = new MSock(address, 9030);
        log("did socket");
        MigratoryObjectInputStream ois = new MigratoryObjectInputStream(c.getInputStream());
        log("got in stream");
        LinkedList<String> result = (LinkedList<String>) ois.readObject();
        log("result found!");
        System.err.println(result.toString());
    }

    private static void logError(Object o) {
        System.err.println(o.toString());
    }

    private static void log(Object o) {
        System.err.println(o.toString());
    }
}
