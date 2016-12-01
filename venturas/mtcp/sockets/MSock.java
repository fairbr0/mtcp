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

	/* Pre: C is connected to S1, but connected has already degraded
	 * Post: C is connected to S2 */
	private void migrate() throws IOException, ClassNotFoundException, MTCPMigrationException {
		ackLock.set(true);
		//Pick S2 from list in FIFO strategy
		if (otherServers.isEmpty()) {
			throw new MTCPMigrationException("No servers to migrate to");
		}
		//construct socket and streams to S2
		AddressPortTuple s2AddressPort = otherServers.remove(0);
		Socket s2Socket = new Socket(s2AddressPort.getAddress(), s2AddressPort.getPort(0));
		ObjectOutputStream s2oos = new ObjectOutputStream(s2Socket.getOutputStream());
		ObjectInputStream s2ois = new ObjectInputStream(s2Socket.getInputStream());

		//write SYN MIG to S2
		Flag[] synMig = {Flag.SYN, Flag.MIG};
		s2oos.writeObject(new InternalPacket(synMig, new AddressPortTuple(s1Address, s1Port)));

		// Wait for expected ACK, MIG
		Flag[] ackMig = ((InternalPacket)s2ois.readObject()).getFlags();
		if (ackMig.length != 2) {
			throw new MTCPMigrationException("Got array of wrong length when expecting ACK,MIG (should be 2)");
		}
		if (!containsFlag(Flag.ACK, ackMig) || !containsFlag(Flag.MIG, ackMig)) {
			throw new MTCPMigrationException("Did not get ACK,MIG despite expecting only that");
		}
		/* got ACK,MIG we now know that S2 is available to assume server
		 * responsibilities for C, and will have a chat with S1 to sort this out */

		//Must now wait until S2 is ready for migration
		Flag[] ackReady = ((InternalPacket)s2ois.readObject()).getFlags();
		if (ackReady.length != 2) {
			throw new MTCPMigrationException("Got array of wrong length when expecting ACK,MIG_READY (should be 2)");
		}
		if (!containsFlag(Flag.ACK, ackReady) || !containsFlag(Flag.MIG_READY, ackReady)) {
			throw new MTCPMigrationException("Did not get ACK,MIG_READY despite expecting only that");
		}

		/* got ACK,MIG_READY we now know that S2 is ready to replace S1 in the
		 * communication */
		super.socket = s2Socket;
		super.oos = s2oos;
		super.ois = s2ois;
		ackLock.set(false);
	}

	protected void handleOutgoingPacket() {
		(new Thread(() -> {
			try {
				while(true) {
					while (ackLock.get()) {
						//block
					}
					ackLock.set(true);
					Flag[] flags = {Flag.SYN};
					oos.writeObject(new Packet(flags, outByteMessages.take()));
					socket.setSoTimeout(2000);
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
			try {
				while(true) {
					Packet p = (Packet)ois.readObject();
					Flag[] f = p.getFlags();
					if (containsFlag(Flag.SYN, f)) {
						log("Got packet");
						inByteMessages.put(p.getPayload());
						log("Wrote ACK");
						Flag[] flags = {Flag.ACK};
						oos.writeObject(new Packet(flags, null));
					} else if (containsFlag(Flag.ACK, f)) {
						ackLock.set(false);
						log("ACK");


						socket.setSoTimeout(0);

						
					}
				}
			} catch (SocketTimeoutException e) {
				logError("Client side timeout, now forcing migration");
				try {
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
		})).start();
	}
}
