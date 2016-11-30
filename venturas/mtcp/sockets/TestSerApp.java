package venturas.mtcp.sockets;


import venturas.mtcp.io.*;

public class TestSerApp {
  public static void main(String[] args) throws Exception {
    SerializedShellSocket s = new SerializedShellSocket(9030, true);
    byte[] b = {34,6,3,5,5,7,33,67,43,7,4,78,4,-8,7,0};
    QueuedByteArrayOutputStream sout = s.getOutputStream();
    sout.writeBytes(b);
  }
}
