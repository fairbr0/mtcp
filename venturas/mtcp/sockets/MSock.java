package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public class MSock extends AbstractMSock {

	private List<AddressPortTuple> otherServers;

	public MSock(InetAddress address, int port) throws Exception {
		super(new Socket(address, port));
		//initialHandshake will be called by super, amongst others
	}

	protected void initialHandshake()
	throws IOException, ClassNotFoundException, MTCPHandshakeException {
		ackLock.set(true);
		Flag[] syn = {Flag.SYN};
		oos.writeObject(new InternalPacket(syn, null));
		oos.flush();
		InternalPacket<List<AddressPortTuple>> response = (InternalPacket<List<AddressPortTuple>>)ois.readObject();
		Flag[] responseFlags = response.getFlags();
		if (containsFlag(Flag.SYN, responseFlags) && containsFlag(Flag.ACK, responseFlags)) {
			if (responseFlags.length != 2) {
				throw new MTCPHandshakeException("SYN,ACK, but wrong length");
			}
		} else {
			throw new MTCPHandshakeException("Did not get SYN,ACK");
		}
		this.otherServers = response.getPayload();
		Flag[] ack = {Flag.ACK};
		oos.writeObject(new InternalPacket(ack, null));
		oos.flush();
		ackLock.set(false);
		log("Handshake complete");
	}

	protected String getLabel() {
		return "<MSock>";
	}

}
