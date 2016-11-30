package venturas.mtcp.sockets;

import venturas.mtcp.io.*;

public class TestCliApp {
  public static void main(String[] args) throws Exception {
    SerializedShellSocket c = new SerializedShellSocket(9030, false);
    log("did socket");
    QueuedByteArrayInputStream cin = c.getInputStream();
    log("got in stream");
    byte[] result = (byte[])cin.readBytes();
    log("reult found!");
    System.err.println(java.util.Arrays.toString(result));
  }

  private static void logError(Object o) {
    System.err.println(o.toString());
  }

  private static void log(Object o) {
    System.err.println(o.toString());
  }
}
