package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public class MServerSock extends AbstractMSock {
	private List<AddressMapping> serverList; //pool
    private State latestState; //latest snapshot
	private int publicPort; // port for client
	private int privatePort; //port for private network between servers
	private Socket otherServer; // talk to other server to do a migration
	private boolean hasClient; //do I have a client on me already?
	private ServerSocket ssServer; //accept incoming server connection
	private ServerSocket ssClient; //accept incoming client connection

  	public MServerSock(int publicPort, int privatePort, List<AddressMapping> serverList) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		super();
		init(publicPort, privatePort, serverList);
		this.ssServer = new ServerSocket(privatePort);
		this.ssClient = new ServerSocket(publicPort);
  	}

	// Reinitialise the object after migration away from it
	private void reinit() throws IOException {
		init(this.publicPort, this.privatePort, this.serverList);
		try {
			ois.close();
			oos.close();
			socket.close();
		} catch (EOFException e) {
			// this is okay :)
   		}
		accept();
		migrated.set(false);
	}

	private void init(int publicPort, int privatePort, List<AddressMapping> serverList) throws IOException {
		//normal constructor would call super(), probably don't need that hear/ nor is it possible without redesign
		this.publicPort = publicPort;
		this.privatePort = privatePort;
		this.serverList = serverList;
		this.hasClient = false;
		this.latestState = new State(null);
	}

	/** This is NOT blocking. Apps have to loop on hasClient() method instead **/
	public void accept() {
		this.acceptServer();
		this.acceptClient();
	}

	// Wait for another server to come in
	private void acceptServer() {
		(new Thread(() -> {
			try {
				otherServer = ssServer.accept();
				log("Server connected in");
				serverToServerHandshake();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (MTCPMigrationException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		})).start();
	}

	// Wait for a client to come in!
	private void acceptClient() {
		(new Thread(() -> {
			try {
				super.acceptClient(ssClient.accept());
				log("Client connected in");
				hasClient = true;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (MTCPHandshakeException e) {
				e.printStackTrace();
			} catch (MTCPMigrationException e) {
				e.printStackTrace();
			}
		})).start();
	}

	/* API endpoint exposed to app. App on the SERVER SIDE should periodically export their computation state */
	public void exportState(State state) {
        this.latestState = state;
    }

	/* API endpoint exposed to app. If we just did a migration, import the state
	 * to reconstruct which point in the computation we were at so computation can be continued */
    public State importState() throws MTCPStateException {
        if (this.latestState == null) {
            throw new MTCPStateException("Latest state snapshot is null");
        }
        return this.latestState;
    }


	/* Deal with one side of the migration handshake! */
	private void serverToServerHandshake() throws IOException, ClassNotFoundException, MTCPMigrationException, MTCPHandshakeException {
		log("serverToServerHandshake initiated");
		ObjectOutputStream soos = new ObjectOutputStream(otherServer.getOutputStream());
        ObjectInputStream sois = new ObjectInputStream(otherServer.getInputStream());

		//Read REQ_STATE packet
		Flag[] reqState = ((InternalPacket)sois.readObject()).getFlags();
		//log("read a packet, hoping it is REQ_STATE, let's check bro");
		if (reqState.length != 1) {
			throw new MTCPMigrationException("Did not get length 1 flags when expecting REQ_STATE");
		}
		if (!containsFlag(Flag.REQ_STATE, reqState)) {
			throw new MTCPMigrationException("Did not get REQ_STATE");
		}

		log("Read REQ_STATE");

		//Write RSP_STATE packet with response with STATE
		Flag[] rspState = {Flag.RSP_STATE};
		InternalPacket<State> state = new InternalPacket<State>(rspState, latestState);
		soos.writeObject(state);
		log("Wrote an RSP_STATE");

		//Read ACK
		Flag[] ack = ((InternalPacket)sois.readObject()).getFlags();
		if (ack.length != 1) {
			throw new MTCPMigrationException("Did not get length 1 flags when expecting ACK");
		}
		if (!containsFlag(Flag.ACK, ack)) {
			throw new MTCPMigrationException("Did not get ACK");
		}
		log("Read ACK");

		//Everything worked, close stuff off! We have just handed over to another server
		try {
			sois.close();
			soos.close();
			this.otherServer.close();
		} catch (EOFException e) {
			e.printStackTrace();
		}

		super.migrated.set(true);

		//Reinit for a later mig
		reinit();
	}

	/* Client is requesting to migrate to us! */
	private void clientMigrationRequest(InetAddress address, int port) throws IOException, ClassNotFoundException, MTCPHandshakeException {

		log("Facilitating mig request");
		//Write ACK, MIG
		Flag[] ackMig = {Flag.ACK, Flag.MIG};
		oos.writeObject(new InternalPacket(ackMig, null));
		log("Wrote ACK, MIG");

		//Set up a socket on the address and port of the server to migrate AWAY from
        Socket s = getSocketFromMapping(address, port);
        ObjectOutputStream soos = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream sois = new ObjectInputStream(s.getInputStream());

        Flag[] flags = {Flag.REQ_STATE};
        InternalPacket p = new InternalPacket(flags);
        soos.writeObject(p);
		log("Wrote REQ_STATE");

        InternalPacket<State> response = (InternalPacket<State>) sois.readObject();
        if (!containsFlag(Flag.RSP_STATE, response.getFlags())) {
			System.out.println(java.util.Arrays.toString(flags));
			throw new MTCPHandshakeException("Not got RSP_STATE flag");
		}
		log("Read RSP_STATE, and got application state from other server");
        this.latestState = (State) response.getPayload();
        Flag[] serverResponseFlags = {Flag.ACK};
        soos.writeObject(new InternalPacket(serverResponseFlags));
		log("Wrote ACK");
        try {
			//close stuff off
			soos.close();
			sois.close();
            s.close();
        } catch (Exception e) {
            // catch the errors thrown by streams;. THESE ARE OKAY :)
			e.printStackTrace();
        }

        //tell the client that the migration was a success
		log("Wrote ACK, MIG_READY");
        Flag[] clientResponseFlags = {Flag.ACK, Flag.MIG_READY};
        this.oos.writeObject(new InternalPacket(clientResponseFlags));
		ackLock.set(true);
		log("Wrote ACK, MIG_READY");
		log("Mig completed");
    }

	// Take a public address and return the socket over the corresponding private address
    public Socket getSocketFromMapping(InetAddress address, int port) throws IOException {
        int mappedPort = 0;
        InetAddress mappedAddress = null;
		// System.err.println("[[[[[[[" + serverList.toString() + "]]]]]]]");
        Iterator<AddressMapping> it = serverList.iterator();
        while (it.hasNext()) {
            AddressMapping mapping = it.next();
            if (mapping.corresponds(address, port)) {
                mappedAddress = mapping.getPrivateAddress();
                mappedPort = mapping.getPrivatePort();
                break;
            } else {
				// System.err.println("+++++++++++++++" + address.toString() + ",,,,," + port);
			}
        }
		//log("Found socket mapping");
        Socket server = new Socket(mappedAddress, mappedPort);
		//log("Got connection to other server");
        return server;
    }

	/* Perform the initial handshake! */
  	protected void initialHandshake() throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		ackLock.set(true);
		log("Beginning Handshake");
		InternalPacket p = (InternalPacket)ois.readObject();
		if (p.getFlags().length > 2) {
			throw new MTCPHandshakeException("got wrong no. of flags on expected SYN");
		}
		if (!containsFlag(Flag.SYN, p.getFlags())) {
			throw new MTCPHandshakeException("did not get SYN, instead got:" + Arrays.toString(p.getFlags()));
		}
		if (containsFlag(Flag.MIG, p.getFlags())) {
			log("Got SYN, MIG. Beginning server migration.");
			AddressMapping s1 = (AddressMapping)p.getPayload();
			clientMigrationRequest(s1.getPublicAddress(), s1.getPublicPort());
			ackLock.set(false);
			return;
		}
		log("Got SYN flag, without MIG, will just do a normal handshake");
		Flag[] synAck = {Flag.SYN, Flag.ACK};
		oos.writeObject(new InternalPacket(synAck, getClientMapping()));
		oos.flush();
		log("Wrote SYN, ACK to client");


		Flag[] ackResponse = ((InternalPacket)ois.readObject()).getFlags();
		if (ackResponse.length != 1) {
			throw new MTCPHandshakeException("got wrong no. of flags on expected ACK");
		}
		if (!containsFlag(Flag.ACK, ackResponse)) {
			throw new MTCPHandshakeException("did not get ACK");
		}
		log("Read ACK");
		ackLock.set(false);
		//socket is ready
  	}


	private List<AddressMapping> getClientMapping() {
		if (this.serverList == null ) {
			return new LinkedList<AddressMapping>();
		}
		List<AddressMapping> result = new LinkedList<AddressMapping>();
		Iterator<AddressMapping> it = serverList.iterator();
		while (it.hasNext()) {
			AddressMapping next = it.next();
			AddressMapping clientNext = next.getPublicOnlyAddressMapping();
			result.add(clientNext);
		}
		return result;
	}

	@Override
	protected void handleIncomingPacket() throws IOException, ClassNotFoundException, MTCPHandshakeException {
		Thread t = new Thread(() -> {
			try {
				while(true) {
					Packet p = null;
					try {
						if (forcedReadTimeout.get()) {
							forcedReadTimeout.set(false);
							throw new SocketException();
						}
						p = (Packet)ois.readObject();
					} catch (EOFException e) {
						//All okay :) We've been migrated away from
						logError("EOFException, migration??????????");
						break;
					} catch (SocketException e) {
						//All okay :) We've been migrated away from
						break;
					}

					// Check if we got a MESSAGE or an ACK
					Flag[] f = p.getFlags();
					if (containsFlag(Flag.MESSAGE, f)) {
						inByteMessages.put(p.getPayload());
						this.latestState.addToBufferIn(p.getPayload());
						Flag[] flags = {Flag.ACK};
						oos.writeObject(new Packet(flags, null));
						//wrote ACK
					} else if (containsFlag(Flag.ACK, f)) {
						//Read ACK
						ackLock.set(false);

					}
				}

			// Stack traces are all okay :)
			} catch (SocketTimeoutException e) {
				//logError("Socket timed out, but that's probably okay");
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		t.start();
	}

	@Override
    protected void handleOutgoingPacket() {
      (new Thread(() -> {
        try {
          while(true) {
            while (ackLock.get()) {
              //block
			  Thread.sleep(0);
            }
            ackLock.set(true);
            byte[] outgoingBytes = outByteMessages.take();
			if (forcedWriteTimeout.get()) {
				// Tell the client he should migrate away from me because I have heavy load or something
				forcedWriteTimeout.set(false);
				System.err.println("ADVISE_MIG");
				Flag[] flags = {Flag.MESSAGE, Flag.ADVISE_MIG};
            	oos.writeObject(new Packet(flags, outgoingBytes));
			} else {
				// Just send a message over as normal
				Flag[] flags = {Flag.MESSAGE};
				oos.writeObject(new Packet(flags, outgoingBytes));
			}
			oos.flush();

			// Add to buffers
			this.latestState.addToBufferOut(outgoingBytes);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      })).start();
    }

	protected String getLabel() {
		return "<MServerSock>";
	}

	// Application uses this to check if it can use this MSS yet
	public boolean hasClient() {
		return hasClient;
	}
}
