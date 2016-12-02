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
	private InetAddress s1Address;
	private int s1Port;

	public MSock(InetAddress address, int port) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		super(new Socket(address, port));
		this.s1Address = address;
		this.s1Port = port;

		//initialHandshake will be called by super, amongst others
	}

	protected void initialHandshake() throws IOException, ClassNotFoundException, MTCPHandshakeException {
		ackLock.set(true);
		Flag[] syn = {Flag.SYN};
		oos.writeObject(new InternalPacket(syn, null));
		oos.flush();
		log("SYN sent");
		InternalPacket<List<AddressPortTuple>> response = (InternalPacket<List<AddressPortTuple>>)ois.readObject();
		log("Got response. SYN, ACK Expected:");
		Flag[] responseFlags = response.getFlags();
		if (containsFlag(Flag.SYN, responseFlags) && containsFlag(Flag.ACK, responseFlags)) {
			if (responseFlags.length != 2) {
				throw new MTCPHandshakeException("SYN,ACK, but wrong length");
			}
		} else {
			throw new MTCPHandshakeException("Did not get SYN,ACK");
		}
		log("Got SYN, ACK");
		this.otherServers = response.getPayload();
		Flag[] ack = {Flag.ACK};
		oos.writeObject(new InternalPacket(ack, null));
		oos.flush();
		log("Sent ACK");
		log("Handshake complete");
		ackLock.set(false);
		log("Socket Ready.");
	}

	protected String getLabel() {
		return "<MSock>";
	}

	/* Pre: C is connected to S1, but connected has already degraded
	 * Post: C is connected to S2 */
	public void migrate() throws IOException, ClassNotFoundException, MTCPMigrationException {
		log("MIGRATION CALLED");
		ackLock.set(true);
		//Pick S2 from list in FIFO strategy
		if (otherServers == null || otherServers.isEmpty()) {
			throw new MTCPMigrationException("No servers to migrate to");
		}
		//construct socket and streams to S2
		AddressPortTuple s2AddressPort = otherServers.remove(0);
		Socket s2Socket = new Socket(s2AddressPort.getAddress(), s2AddressPort.getPort(0));
		log("Created s2Socket");
		ObjectOutputStream s2oos = new ObjectOutputStream(s2Socket.getOutputStream());
		ObjectInputStream s2ois = new ObjectInputStream(s2Socket.getInputStream());
		log("we reated the s2 streams");

		//write SYN MIG to S2
		Flag[] synMig = {Flag.SYN, Flag.MIG};
		s2oos.writeObject(new InternalPacket(synMig, new AddressPortTuple(s1Address, s1Port)));
		log("wrote SYN MIG");

		// Wait for expected ACK, MIG
		log("now gonna read and hope it is an ACK MIG");
		Flag[] ackMig = ((InternalPacket)s2ois.readObject()).getFlags();
		log("got a packet, now gonna check if it was ACK MIG");
		if (ackMig.length != 2) {
			throw new MTCPMigrationException("Got array of wrong length when expecting ACK,MIG (should be 2)");
		}
		if (!containsFlag(Flag.ACK, ackMig) || !containsFlag(Flag.MIG, ackMig)) {
			throw new MTCPMigrationException("Did not get ACK,MIG despite expecting only that");
		}
		log("got an ACK MIG!");
		/* got ACK,MIG we now know that S2 is available to assume server
		 * responsibilities for C, and will have a chat with S1 to sort this out */

		//Must now wait until S2 is ready for migration
		log("now reading, hopefully an ACK MIG_READY");
		Flag[] ackReady = ((InternalPacket)s2ois.readObject()).getFlags();
		log("got reply, let's check if it is ACK");
		if (ackReady.length != 2) {
			throw new MTCPMigrationException("Got array of wrong length when expecting ACK,MIG_READY (should be 2)");
		}
		if (!containsFlag(Flag.ACK, ackReady) || !containsFlag(Flag.MIG_READY, ackReady)) {
			throw new MTCPMigrationException("Did not get ACK,MIG_READY despite expecting only that");
		}
		log("yay got an ACK");

		/* got ACK,MIG_READY we now know that S2 is ready to replace S1 in the
		 * communication */
		log("Changing socket over now");
		log("s2Socket port number = " + s2Socket.getPort());
		super.socket = s2Socket;
		super.oos = s2oos;
		super.ois = s2ois;
		ackLock.set(false);
		log("Socket changed now. Lock state: " + ackLock.get());
	}

	protected void handleOutgoingPacket() {
		(new Thread(() -> {
			try {
				while(true) {
					while (ackLock.get()) {
						//block
					}
					ackLock.set(true);
					Flag[] flags = {Flag.MESSAGE};
					log("soTimeOut outgoing listener: " + socket.getSoTimeout());
					oos.writeObject(new Packet(flags, outByteMessages.take()));
					log("Wrote Packet");
				}
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		})).start();
	}

	protected void handleIncomingPacket() throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		(new Thread(() -> {
			while(true) {
				try {
					log("soTimeOut incomingListener: " + socket.getSoTimeout());
					Packet p = (Packet)ois.readObject();
					Flag[] f = p.getFlags();
					if (containsFlag(Flag.MESSAGE, f)) {
						log("Got MESSAGE packet");
						inByteMessages.put(p.getPayload());
						log("Wrote ACK");
						Flag[] flags = {Flag.ACK};
						oos.writeObject(new Packet(flags, null));
					} else if (containsFlag(Flag.ACK, f)) {
						ackLock.set(false);
						log("ACK");


					}
				} catch (SocketTimeoutException e) {
					logError("======Client side timeout, now forcing migration=====");
					try {
						log("lock state: " + ackLock.get());
						migrate();
					} catch (MTCPMigrationException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		})).start();
	}
}
