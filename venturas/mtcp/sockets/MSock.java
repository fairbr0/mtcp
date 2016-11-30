package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public class MSock extends AbstractMSock {

  public MSock(InetAddress address, int port) throws Exception {
    super(new Socket(address, port));
  }

  protected void initialHandshake()
  throws IOException, ClassNotFoundException, MTCPHandshakeException {
	Flag[] syn = {Flag.SYN};
	oos.writeObject(new Packet(syn, null));
	oos.flush();
	Flag[] response = ((Packet)ois.readObject()).getFlags();
	if (containsFlag(Flag.SYN, response) && containsFlag(Flag.ACK, response)) {
		if (response.length != 2) {
			throw new MTCPHandshakeException("SYN,ACK, but wrong length");
		}
	} else {
		throw new MTCPHandshakeException("Did not get SYN,ACK");
	}
	Flag[] ack = {Flag.ACK};
	oos.writeObject(new Packet(ack, null));
  }
}
