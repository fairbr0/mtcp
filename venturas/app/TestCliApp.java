package venturas.app;

import venturas.mtcp.io.*;
import venturas.mtcp.sockets.*;

public class TestCliApp {
  public static void main(String[] args) throws Exception {
    SerializedShellSocket c = new SerializedShellSocket(9030, false);
    log("did socket");
    MigratoryObjectInputStream ois = new MigratoryObjectInputStream(c.getInputStream());
    log("got in stream");
    String result = (String) ois.readObject();
    log("reult found!");
    System.err.println(result);
  }

  private static void logError(Object o) {
    System.err.println(o.toString());
  }

  private static void log(Object o) {
    System.err.println(o.toString());
  }
}
