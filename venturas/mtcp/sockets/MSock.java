package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;
import  java.util.Arrays;

/* A migratory socket for a client */
public class MSock extends AbstractMSock {

	private List<AddressMapping> otherServers; // list of all servers including current
	private InetAddress s1Address; // address of current server
	private int s1Port; //port of current
	public static final int TIMEOUT = 500; //timeout value, not really relevant for the version that migrates on the HARD ENTER KEY

	/* Construct an MSock over an address and port */
	public MSock(InetAddress address, int port) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		super(new Socket(address, port));
		this.s1Address = address;
		this.s1Port = port;
		socket.setSoTimeout(TIMEOUT);
	}

	/* Perform the initial handshake between C and S1 */
	protected void initialHandshake() throws IOException, ClassNotFoundException, MTCPHandshakeException {
		ackLock.set(true); //acquire the lock

		// Send SYN with no payload
		Flag[] syn = {Flag.SYN};
		oos.writeObject(new InternalPacket(syn, null));
		oos.flush();
		log("Wrote SYN in initial handshake");

		// Wait for SYN,ACK response, with list of all servers in the pool as the payload
		InternalPacket<List<AddressMapping>> response = (InternalPacket<List<AddressMapping>>)ois.readObject();
		Flag[] responseFlags = response.getFlags();
		if (containsFlag(Flag.SYN, responseFlags) && containsFlag(Flag.ACK, responseFlags)) {
			if (responseFlags.length != 2) {
				throw new MTCPHandshakeException("SYN,ACK, but wrong length");
			}
		} else {
			throw new MTCPHandshakeException("Did not get SYN,ACK");
		}
		log("Read SYN, ACK on initial handshake");
		// Set servers list by payload
		this.otherServers = response.getPayload();

		// Send ACK with no payload
		Flag[] ack = {Flag.ACK};
		oos.writeObject(new InternalPacket(ack, null));
		oos.flush();
		log("Wrote ACK in initial handshake which has hence completed!");

		// Release the lock so other stuff can happen
		ackLock.set(false);
	}

	protected String getLabel() {
		return "<MSock>";
	}

	/* PERFORMS THE MIGRATION
	 * Pre: C is connected to S1, but connection has already degraded
	 * Post: C is connected to S2 */
	private void migrate() throws IOException, ClassNotFoundException, MTCPMigrationException {
		log("Migration initiated!!!");
		ackLock.set(true); //acquire the lock

		// Pick S2 from the list randomly
		Socket s2Socket;
		AddressMapping s2Mapping = null;
		while (true) {
			if (otherServers == null || otherServers.isEmpty() || otherServers.size() == 0) {
				// No other servers to migrate to, this is not in our fault model.
				throw new MTCPMigrationException("No servers to migrate to");
			}
			if (otherServers.size() == 1) {
				//Cannot migrate, because S1 in the only server left in the pool
				ackLock.set(false);
				logError("Cannot migrate, no other servers available");
				return;
			}
			int random = 0;
			try {
				s2Mapping = null;
				boolean foundOtherServer = false;
				while (!foundOtherServer) {
					random = new Random().nextInt(otherServers.size());
					s2Mapping = otherServers.get(random);
					if (s2Mapping.getPublicAddress().equals(socket.getInetAddress())) {
						if (s2Mapping.getPublicPort() != socket.getPort()) {
							// This is server is different to S1, can migrate to it
							foundOtherServer = true;
						}
					} else {
						// This is server is different to S1, can migrate to it
						foundOtherServer = true;
					}
				}

				log("Found a server to migrate to!");

				//instantiate the socket to S2
				s2Socket = new Socket(s2Mapping.getPublicAddress(), s2Mapping.getPublicPort());
				break;
			} catch (ConnectException e) {
				otherServers.remove(random);
			}
		}

		// Get S2 streams
		ObjectOutputStream s2oos = new ObjectOutputStream(s2Socket.getOutputStream());
		ObjectInputStream s2ois = new ObjectInputStream(s2Socket.getInputStream());

		//write SYN MIG to S2, and tell it the address details of S1 so that S2 can help us migrate away from S1
		Flag[] synMig = {Flag.SYN, Flag.MIG};
		InetAddress currentAdd = this.s1Address;
		int currentPort = this.s1Port;
		AddressMapping apt = new AddressMapping(currentAdd, currentPort);
		s2oos.writeObject(new InternalPacket(synMig, new AddressMapping(s1Address, s1Port)));
		log("Wrote SYN,MIG in migrate()");

		// Read ACK, MIG from S2
		Flag[] ackMig = ((InternalPacket)s2ois.readObject()).getFlags();
		if (ackMig.length != 2) {
			throw new MTCPMigrationException("Got array of wrong length when expecting ACK,MIG (should be 2)");
		}
		if (!containsFlag(Flag.ACK, ackMig) || !containsFlag(Flag.MIG, ackMig)) {
			throw new MTCPMigrationException("Did not get ACK,MIG despite expecting only that");
		}
		log("Read an ACK MIG in migrate()");
		/* got ACK,MIG so we now know that S2 is available to assume server
		 * responsibilities for C, and on its end it will have a chat with S1 to sort this out */

		//Must now wait until S2 is ready for migration
		//Wait for ACK, MIG_READY from S2
		Flag[] ackReady = ((InternalPacket)s2ois.readObject()).getFlags();
		//log("got reply, let's check if it is ACK");
		if (ackReady.length != 2) {
			throw new MTCPMigrationException("Got array of wrong length when expecting ACK,MIG_READY (should be 2)");
		}
		if (!containsFlag(Flag.ACK, ackReady) || !containsFlag(Flag.MIG_READY, ackReady)) {
			throw new MTCPMigrationException("Did not get ACK,MIG_READY despite expecting only that");
		}
		log("Read ACK, MIG_READY in migrate()");

		/* got ACK,MIG_READY we now know that S2 is ready to replace S1 in the
		 * communication */


		 // CLose the old socket, catch the EOFException is fine
		try {
			super.ois.close();
			super.oos.close();
			super.socket.close();
		} catch (EOFException e) {
			// Fine
		}
		// Now swap the sckets and streams over, before unlocking
		super.socket = s2Socket;
		super.socket.setSoTimeout(TIMEOUT);
		super.oos = s2oos;
		super.ois = s2ois;
		this.s1Address = s2Mapping.getPublicAddress();
		this.s1Port = s2Mapping.getPublicPort();
		ackLock.set(false);
		log("Migration completed!");
	}

	/* Listen for arrays of bytes put onto the outgoing queue, take them off, package them up and send out to migratory streams/ queue */
	protected void handleOutgoingPacket() {
		(new Thread(() -> {
			try {
				while(true) {
					while (ackLock.get()) {
						Thread.sleep(0);
						//block
					}
					// acquire the lock -> idea is that we cannot send another message until an ACK on the incoming is received and releases locks
					ackLock.set(true);
					Flag[] flags = {Flag.MESSAGE};
					byte[] message = outByteMessages.take();

					// Did Matt Bradbury force a migration with the ENTER key
					if (forcedWriteTimeout.get()) {
						forcedWriteTimeout.set(false);
						migrate();
					}
					oos.writeObject(new Packet(flags, message));
				}

			// IT IS NOT A PROBLEM IF THESE OCCUR, BUT STILL PRINT THE TRACES FOR DEBUGGING APPS :)
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (MTCPMigrationException e) {
				e.printStackTrace();
			}
		})).start();
	}

	/* Listen on the input stream, depacket and put onto the queue for migratory streams/ queues to work with */
	protected void handleIncomingPacket() throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		(new Thread(() -> {
			while(true) {
				try {
					Packet p = null;
					try {
						if (forcedReadTimeout.get()) {
							// Matt forced a migration so throw a timeout exception to force the migration
							forcedReadTimeout.set(false);
							throw new SocketTimeoutException();
						}
						p = (Packet)ois.readObject();
					} catch (StreamCorruptedException e) {
						// Not a problem, but again MIGRATE
						e.printStackTrace();
						throw new SocketTimeoutException();
					} catch (ClassNotFoundException e) {
						// Not a problem, but again MIGRATE
						e.printStackTrace();
						throw new SocketTimeoutException();
					} catch (ClassCastException e) {
						// Not a problem, but again MIGRATE
						e.printStackTrace();
						throw new SocketTimeoutException();
					} catch (OptionalDataException e) {
						// Not a problem, but again MIGRATE
						e.printStackTrace();
						throw new SocketTimeoutException();
					}


					// Yes, this IS not amazing software engineering. But it works!
					// If we had the chance to do it again, we'd throw our own exception
					// and catch that, or just use booleans for control flow. We didn't
					// want it to break however!


					Flag[] f = p.getFlags();
					if (containsFlag(Flag.MESSAGE, f)) {
						//log("Got MESSAGE packet");
						if (containsFlag(Flag.ADVISE_MIG, f)) {
							log("Server advised migration, handling it!");
							inByteMessages.put(p.getPayload());
							Flag[] flags = {Flag.ACK};
							oos.writeObject(new Packet(flags, null));
							throw new SocketTimeoutException(); //again, throw it to force migration.
						}
						inByteMessages.put(p.getPayload());
						Flag[] flags = {Flag.ACK};
						oos.writeObject(new Packet(flags, null));
					} else if (containsFlag(Flag.ACK, f)) {
						// Got an ACK, so a write was received successfully so we can release the lock for INCOMING thread to continue its job
						ackLock.set(false);
					}
				} catch (SocketTimeoutException e) {
					try {
						migrate();
					} catch (MTCPMigrationException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		})).start();
	}
}
