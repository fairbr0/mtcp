package venturas.app;

import venturas.mtcp.io.*;
import venturas.mtcp.sockets.*;
import java.net.*;

import venturas.mtcp.io.*;

public class TestSerApp {
  public static void main(String[] args) throws Exception {
    SerializedShellServerSocket s = new SerializedShellServerSocket(9030);
    String str = "Hello there!";
    MigratoryObjectOutputStream sout = new MigratoryObjectOutputStream(s.getOutputStream());
    sout.writeObject(str);
  }
}
