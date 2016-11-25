package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public class MigratableSocket extends AbstractMigratableParentSocket {

	private Socket server;
	private List<AddressPortTuple> serverList;

	public MigratableSocket(InetSocketAddress socketAddress) throws IOException, ClassNotFoundException, MTCPHandshakeException {
		super();

		log("creating socket");
		this.server = new Socket(socketAddress.getAddress(), socketAddress.getPort());
		log("have created a socket");

		super.os = new ObjectOutputStream(server.getOutputStream());
		log("have created out stream");

		super.is = new ObjectInputStream(server.getInputStream());
		log("have created in stream");

		this.performInitialHandshake();

		//each of the following will run in loop in own thread
		this.incomingPacketsListener();
		this.outgoingPacketsListener();
	}

	protected void handleIncomingPacket(Packet packet) {

	}

	///all below methods are private to the class and the client should not be using them.
	protected void performInitialHandshake() throws MTCPHandshakeException, IOException, ClassNotFoundException {
		this.server.setSoTimeout(5000);

		//send first SYN to the server.
		Flag[] flags = new Flag[1];
		flags[0] = Flag.SYN;
		log("Started handhake: Sending SYN");
		super.os.writeObject(new Packet<String>(flags));
		super.os.flush();

		//wait for response of SYN plus ACK, and payload of Server list
		log("Waiting for read");
		Packet<ArrayList<AddressPortTuple>> response = (Packet<ArrayList<AddressPortTuple>>) super.is.readObject();
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
		// this.server.setSoTimeout(10000);
		log("Got to the end of my handshake!!!!1");
	}




	/* TODO reengineer and REMOVE PUBLIC migration access */


	public void migrate() throws MTCPMigrationException, IOException, ClassNotFoundException {

		super.lock();
		/* Pick a server according to some strategy; currently FIRST in LIST */
		if (serverList == null || serverList.isEmpty() || serverList.get(0) == null) {
			throw new MTCPMigrationException("No available servers");
		}
		AddressPortTuple candidate = serverList.remove(0);

		/* Perform migration */
		Socket newServer = new Socket(candidate.getAddress(), candidate.getPorts()[0]);
		//TODO THIS MIGHT NEED TO BE 1

		log("candidate:" + candidate.toString());
		ObjectOutputStream mos = new ObjectOutputStream(newServer.getOutputStream());
		ObjectInputStream mis = new ObjectInputStream(newServer.getInputStream());
		Flag[] flags = { Flag.SYN, Flag.MIG };

		//TODO getLocalAddress????
		log("server.getLocalAddress():" + server.getLocalAddress());
		AddressPortTuple s1Addr = new AddressPortTuple(server.getLocalAddress(), server.getPort());
		mos.writeObject(new Packet<AddressPortTuple>(flags, s1Addr));

		//Read from stream, check for errors and throw to prevent continuation of execution
		Packet<String> resp = (Packet<String>)mis.readObject();
		if (resp.getFlags().length == 2) {
			if (resp.getFlag(0) != Flag.ACK || resp.getFlag(1) != Flag.MIG) {
				logError("2 flags but not (ACK,MIG); instead: (" + resp.getFlag(0) + "," + resp.getFlag(1) + ")");
				throw new MTCPMigrationException();
			}
		} else {
			logError("Got flags of length (" + resp.getFlags().length + ") rather than (2).");
			throw new MTCPMigrationException();
		}
		log("Packet read was (ACK,MIG); now reading again");

		//Read from stream, check for errors and throw to prevent continuation of execution
		resp = (Packet<String>)mis.readObject();
		log("Read another packet!");
		if (resp.getFlags().length == 2) {
			if (resp.getFlag(0) != Flag.ACK || resp.getFlag(1) != Flag.MIG) {
				logError("2 flags but not (ACK,MIG_DONE); instead: (" + resp.getFlag(0) + "," + resp.getFlag(1) + ")");
				throw new MTCPMigrationException();
			}
		} else {
			logError("Got flags of length (" + resp.getFlags().length + ") rather than (0).");
			throw new MTCPMigrationException();
		}

		log("Migration completed!!");

		log("server was:" + server.toString());


		try {
			this.server.close();
			this.server = null;
		} catch (Exception e) {
			e.printStackTrace();
		}




		this.server = newServer;
		log("Reassigned migrator socket to this MSock's class variable socket");

		super.is = mis;
		log("Reassigned ObjectInputStream" + super.is);

		super.os = mos;
		log("Reassigned ObjectOutputStream" + super.os);

		log("server now:" + server.toString());

		super.unlock();
	}

	protected Thread incomingPacketsListener() {
		Thread thread = new Thread(() -> {
			try {
				while (true) {
					try {
						log("Looking for input");
						try {

							Packet p = (Packet)is.readObject();
							if (super.containsFlag(Flag.SYN, p.getFlags())) {
								super.inMessageQueue.put(p.getPayload());
								log("Put onto inMessageQueue");
								super.sendACK();
							} else if (super.containsFlag(Flag.ACK, p.getFlags())){
								log("Got ACK");
								log("releasing locks");
								super.unlock();
							} else {
								logError("Unexpected flag(s), got: " + java.util.Arrays.toString(p.getFlags()));
							}

						} catch (InterruptedException e) {
							e.printStackTrace();
						}


						Thread.sleep(1);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (SocketTimeoutException e) {
						try {
							log("migrating");
							migrate();
							log("finished migrating");
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		thread.start();
		return thread;

	}


		protected Thread outgoingPacketsListener() {
			Thread thread = new Thread(() -> {
				try {
					while (true) {
					//take will block if empty queue
						Flag[] spam = {Flag.SPAMSPAMSPAMSPAMBACONANDSPAM, Flag.SYN};
						try {
							log("Doing a write:");
							log(server.getPort() + " " + server.getLocalPort());
							while (super.getACKLock()) {
								//block
								log("blocked");
								Thread.sleep(10);
							}
							super.lock();
							log("took ack lock");
							os.writeObject(new Packet<Object>(spam, outMessageQueue.take()));
							os.flush();
							log("wrote");
						} catch (SocketTimeoutException e) {
							logError("TIMEOUT!");
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Thread.sleep(1);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			thread.start();
			return thread;
		}


}
