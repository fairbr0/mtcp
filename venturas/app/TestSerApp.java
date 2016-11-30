package venturas.app;

import venturas.mtcp.io.*;
import venturas.mtcp.sockets.*;

import venturas.mtcp.io.*;

public class TestSerApp {
  public static void main(String[] args) throws Exception {
    SerializedShellSocket s = new SerializedShellSocket(9030, true);
    String str = "Hello there!";
    MigratoryObjectOutputStream sout = new MigratoryObjectOutputStream(s.getOutputStream());
    sout.writeObject(str);
  }
}
