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
	private List<AddressPortTuple> otherServers;

	public MigratableServerSocket(int clientPort, int serverPort) throws IOException, ClassNotFoundException {
		this(clientPort, serverPort, null);
	}

	public MigratableServerSocket(int clientPort, int serverPort, List<AddressPortTuple> otherServers) throws IOException, ClassNotFoundException {
		super();
		log("!!!!!" + clientPort + "," + serverPort + "!!!!!");
		this.otherServers = otherServers;
		log("otherServers size ========= " + this.otherServers.size());
		for (AddressPortTuple t : otherServers) {
			log(t.getAddress() + "," + t.getPorts()[0] + "," + t.getPorts()[1]);
		}
		this.serverListener = new ServerSocket(serverPort);
		this.clientListener = new ServerSocket(clientPort);
		log("Constructor finished, but object initialisation itself must be finished by the programmer making a call to acceptClient()");
	}

	public void acceptClient() throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		log("clientListener.accept about to be called");
		this.client = clientListener.accept();
		log("accepted client");

		super.os = new ObjectOutputStream(client.getOutputStream());
		log("created out stream");

		super.is = new ObjectInputStream(client.getInputStream());
		log("created in stream");

		this.performInitialHandshake();

		//each of the following will run in loop in own thread
		log("Calling packet listeners");
		this.incomingPacketsListener();
		this.outgoingPacketsListener();

	}

	//TODO stop throwing Exception!!!!!
	public void acceptServer() throws Exception {
		log("Waiting for my serverside accept now!");
		Socket s = serverListener.accept();
		log("Accepted my other server! Reading from him");
		ObjectOutputStream otherServerOS = new ObjectOutputStream(s.getOutputStream());
		ObjectInputStream otherServerIS = new ObjectInputStream(s.getInputStream());
		Packet packet = (Packet)otherServerIS.readObject();
		if (packet.getFlags().length == 1) {
			if (packet.getFlag(0) != Flag.REQ_STATE) {
				logError("Flags length 1 but not got REQ_STATE, instead: " + packet.getFlag(0));
				throw new MTCPMigrationException();
			}
		} else {
			logError("Flags not even length 1");
			throw new MTCPMigrationException();
		}
		//if got here, all worked!
		log("Got REQ_STATE, see:: " + packet.getFlag(0));

		log("in.peek:" + inMessageQueue.peek());
		log("out.peek:" + inMessageQueue.peek());

		Flag[] ack = { Flag.ACK };
		log("Will now write some state");
		//need to put the real state in here
		otherServerOS.writeObject(new Packet<Integer>(ack, 10));
		log("Wrote ACK with hardcoded state of Integer(10)");
	}

	protected void handleIncomingPacket(Packet packet) {

	}

	protected void performInitialHandshake() throws MTCPHandshakeException, MTCPMigrationException, IOException, ClassNotFoundException {
		this.client.setSoTimeout(5000);
		log("Waiting on an initial read");
		Packet<Object> response = (Packet<Object>) super.is.readObject();

		/* TODO REENGINEER THE SWITCH STATEMENT INTO FUNCTIONS, IT IS HORRID */

		switch (response.getFlags().length) {
		case 1: //standard handshake
			log("One flags, will perform the standard MTCP handshake");
			if (response.getFlag(0).equals(Flag.SYN)) {
				log("Received SYN");
			} else {
				logError("Did NOT receive SYN! Instead:" + response.getFlag(0));
				throw new MTCPHandshakeException();
			}
			Flag[] flags = { Flag.SYN, Flag.ACK };
			log("Writing SYN ACK, plus our server list");
			//Alter so only a list of public server ip/ports are sent, not the private ones.
			super.os.writeObject(new Packet<List<AddressPortTuple>>(flags, this.otherServers));
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
			break;
		case 2: //migration request
			log("Two flags, trying to handle a migration request");
			if (response.getFlag(0) != Flag.SYN || response.getFlag(1) != Flag.MIG) {
				logError("Two flags but not (SYN, MIG); instead: (" + response.getFlag(0) + "," + response.getFlag(1) + ")");
				throw new MTCPMigrationException();
			}
			AddressPortTuple s1Addr = (AddressPortTuple)response.getPayload();
			log("Got two flags (SYN, MIG) plus payload (" + ((AddressPortTuple)(response.getPayload())).toString() + ")");
			flags = new Flag[2];
			flags[0] = Flag.ACK;
			flags[1] = Flag.MIG;
			log("Writing ACK,MIG to client");
			super.os.writeObject(new Packet<String>(flags));
			log("Write done");
			super.os.flush();
			log("s1Addr.getAddress()" + s1Addr.getAddress() + "||||" + s1Addr.getPorts()[0]);
			//TODO might want index 1 (even though it throws exception right now. Maybe need to change usage of Tuples in init funcs?)








			log("Flushed, opening the socket now!");
			Socket s1 = new Socket(s1Addr.getAddress(), s1Addr.getPorts()[0]);

			log("Opened socket to port " + s1Addr.getPorts()[0] + " now opening s1os");
			ObjectOutputStream s1os = new ObjectOutputStream(s1.getOutputStream());
			log("Opened s1os, now trying s1is");
			ObjectInputStream s1is = new ObjectInputStream(s1.getInputStream());
			log("Opened s1is, now continuing");
			flags = new Flag[1];
			flags[0] = Flag.REQ_STATE;
			log("Writing REQ_STATE");
			s1os.writeObject(new Packet(flags));
			log("Now reading the response which should be an ACK with the state payload");
			Packet ack = (Packet)s1is.readObject();
			log("Managed to read the state!");
			//TODO better logic: Throw exceptions!
			Integer state = null;
			if (ack.getFlags().length == 1 && ack.getFlag(0) == Flag.ACK) {
				state = (Integer)ack.getPayload();
				log("got state of: " + state.toString() + ", will now close socket");
			}
			s1.close();
			log("socket shut");
			flags = new Flag[2];
			flags[0] = Flag.ACK;
			flags[1] = Flag.MIG;
			log("will now write (ACK, MIG) on super.os");
			super.os.writeObject(new Packet(flags));
			log("write success, looks like I'm done migrating now");





			log("Will now spam (state++) over");
			Flag[] f = {Flag.SPAMSPAMSPAMSPAMBACONANDSPAM};
			super.os.writeObject(new Packet(f, state + 1));


			// try {
			// 	super.outMessageQueue.put(state + 1);
			// } catch (InterruptedException e) {
			// 	e.printStackTrace();
			// }
			break;
		default:
			logError("Flags not even length 1 or 2, instead: (" + response.getFlags().length + ")");
			throw new MTCPHandshakeException();
		}
	}

	protected final void incomingPacketsListener() {
		(new Thread(() -> {
			while (true) {
				try {
					if (!queueStreamsLocked) {
						log("Looking for input");
						try {
							Packet p = (Packet)is.readObject();
							super.inMessageQueue.put(p.getPayload());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (SocketTimeoutException e) {
					logError("Timed out, trying again!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		})).start();
	}

	public void exportState(Object state) {
		throw new UnsupportedOperationException("Implement me!");
	}

	public Object importState() {
		throw new UnsupportedOperationException("Implement me!");
	}
}
