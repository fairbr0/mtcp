package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public class MigratableServerSocket extends AbstractMigratableParentSocket {
	// TODO check that only 1 conn exists XXX
	private Socket client;
	private ServerSocket clientListener;
	private ServerSocket serverListener;
	private List<AddressPortPair> otherServers;

	public MigratableServerSocket(int portA, int portB) throws IOException, ClassNotFoundException {
		super();
		this.serverListener = new ServerSocket(portB);
		this.clientListener = new ServerSocket(portA);
		log("Constructor finished, but object initialisation itself must be finished by the programmer making a call to acceptClient()");
	}

	public void acceptClient() throws IOException, ClassNotFoundException, MTCPHandshakeException {
		log("clientListener.accept about to be called");
		this.client = clientListener.accept();
		log("accepted client");

		super.os = new ObjectOutputStream(client.getOutputStream());
		log("created out stream");

		super.is = new ObjectInputStream(client.getInputStream());
		log("created in stream");

		this.performInitialHandshake();

		//each of the following will run in loop in own thread
		super.handleIncomingPackets();
		super.handleOutgoingPackets();

	}

	protected void performInitialHandshake() throws MTCPHandshakeException, IOException, ClassNotFoundException {
		this.client.setSoTimeout(5000);
		log("Waiting on an initial read");
		Packet<String> response = (Packet<String>) super.is.readObject();
		if (response.getFlags().length == 1) {
			if (response.getFlag(0).equals(Flag.SYN)) {
				log("Received SYN");
			} else {
				logError("Did NOT receive SYN! Instead:" + response.getFlag(0));
				throw new MTCPHandshakeException();
			}
		} else  {
			logError("Flags not even length 1");
			throw new MTCPHandshakeException();
		}

		Flag[] flags = { Flag.SYN, Flag.ACK };
		log("Writing SYN ACK, plus our server list");
		super.os.writeObject(new Packet<List<AddressPortPair>>(flags, this.otherServers));
		super.os.flush();
		log("Now waiting on a read");
		Packet<String> resp = (Packet<String>) super.is.readObject();
		if (resp.getFlags().length == 1) {
			if (resp.getFlag(0).equals(Flag.ACK)) {
				log("Received an ACK");
			} else {
				logError("Read message but did NOT get ACK, instead: " + resp.getFlag(0));
				throw new MTCPHandshakeException();
			}
		} else {
			logError("Flags not even length 1");
			throw new MTCPHandshakeException();
		}
		log("Got to the end of the handshake!");
		// this.client.setSoTimeout(10000);
	}

	public void exportState(Object state) {
		throw new UnsupportedOperationException("Implement me!");
	}

	public Object importState() {
		throw new UnsupportedOperationException("Implement me!");
	}
}
