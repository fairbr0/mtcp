package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public class MigratableSocket extends AbstractMigratableParentSocket {

	private Socket active;
	private List<AddressPortPair> serverList;

	public MigratableSocket(InetSocketAddress socketAddress) throws IOException, ClassNotFoundException, MTCPHandshakeException {
		super();

		log("creating socket");
		this.active = new Socket(socketAddress.getAddress(), socketAddress.getPort());
		log("have created a socket");

		super.os = new ObjectOutputStream(active.getOutputStream());
		log("have created out stream");

		super.is = new ObjectInputStream(active.getInputStream());
		log("have created in stream");

		this.performInitialHandshake();

		//each of the following will run in loop in own thread
		super.handleIncomingPackets();
		super.handleOutgoingPackets();
	}

	///all below methods are private to the class and the client should not be using them.
	protected void performInitialHandshake() throws MTCPHandshakeException, IOException, ClassNotFoundException {
		this.active.setSoTimeout(5000);

		//send first SYN to the server.
		Flag[] flags = new Flag[1];
		flags[0] = Flag.SYN;
		log("Started handhake: Sending SYN");
		super.os.writeObject(new Packet<String>(flags));
		super.os.flush();

		//wait for response of SYN plus ACK, and payload of Server list
		log("Waiting for read");
		Packet<ArrayList<AddressPortPair>> response = (Packet<ArrayList<AddressPortPair>>) super.is.readObject();
		log("I have now read the input");
		if (response.getFlags().length == 2) {
			if (response.getFlags()[0].equals(Flag.SYN) && response.getFlags()[1].equals(Flag.ACK)) {
				log("Got SYN ACK, getting payload (server list) of (" + response.getPayload() + ")");
				this.serverList = response.getPayload();
			} else {
				logError("Not SYN ACK, actually got " + response.getFlags()[0] + " " + response.getFlags()[1]);
				throw new MTCPHandshakeException();
			}
		} else {
			String errorMessage = "Not length 2, will print flags: ";
			for (int i = 0; i < response.getFlags().length; i++) {
				errorMessage += response.getFlags()[i] + ",";
			}
			logError(errorMessage);
			throw new MTCPHandshakeException();
		}

		//Writing back a single ACK
		flags = new Flag[1]; //Must recreate the object, else Java's shitty sockets resend the old copy
		flags[0] = Flag.ACK;
		log("Writing " + flags[0]);
		super.os.writeObject(new Packet<String>(flags));
		super.os.flush();
		// this.active.setSoTimeout(10000);
		log("Got to the end of my handshake!!!!1");
	}

	private void migrate() {
		throw new UnsupportedOperationException("Implement me!");
	}
}
