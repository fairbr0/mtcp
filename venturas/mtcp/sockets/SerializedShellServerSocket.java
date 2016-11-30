package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public class SerializedShellServerSocket extends AbstractSerializedShellSocket {

  private Socket socket;

  public SerializedShellServerSocket(int port) throws Exception {
    socket = (new ServerSocket(port)).accept();
    oos = new ObjectOutputStream(socket.getOutputStream());
    outByteMessages = new LinkedBlockingQueue<byte[]>();
    os = new MigratoryOutputStream(outByteMessages);

    ois = new ObjectInputStream(socket.getInputStream());
    inByteMessages = new LinkedBlockingQueue<byte[]>();
    is = new MigratoryInputStream(inByteMessages);

    handleIncomingPacket();
    handleOutgoingPacket();
  }
}
