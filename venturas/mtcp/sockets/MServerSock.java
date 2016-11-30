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
	super((new ServerSocket(port)).accept());
  }

  protected void initialHandshake() {
	//wait for SYN
	//send SYN,ACK
	//wait for ACK
  }
}
