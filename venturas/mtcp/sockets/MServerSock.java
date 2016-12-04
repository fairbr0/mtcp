package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public class MServerSock extends AbstractMSock {
	private List<AddressMapping> serverList;
    private State latestState;
	private int publicPort;
	private int privatePort;
	private Socket otherServer;
	private boolean hasClient;
	private ServerSocket ssServer;
	private ServerSocket ssClient;

  	public MServerSock(int publicPort, int privatePort, List<AddressMapping> serverList) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		//TODO setup list of servers
		super();
		init(publicPort, privatePort, serverList);
		this.ssServer = new ServerSocket(privatePort);
		this.ssClient = new ServerSocket(publicPort);
  	}

	private void reinit() throws IOException {
		init(this.publicPort, this.privatePort, this.serverList);
		try {
			ois.close(); //#thisisnew
			oos.close(); //#thisisnew
			socket.close();
		} catch (EOFException e) {
   			//logError("CAUGHT AN EOFException on socket close");
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
	/** This is NOT blocking **/
	public void accept() {
		this.acceptServer();
		this.acceptClient();
	}

	//TODO implement me
	private void acceptServer() {
		(new Thread(() -> {
			try {
				otherServer = ssServer.accept();
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

	private void acceptClient() {
		(new Thread(() -> {
			try {
				System.err.println("made ss, now to super.acceptClient!");
				super.acceptClient(ssClient.accept());
				System.err.println("super.acceptClient did its work");
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

	public void exportState(State state) {
        this.latestState = state;
    }

    public State importState() throws MTCPStateException {
        if (this.latestState == null) {
            throw new MTCPStateException("Latest state snapshot is null");
        }
        return this.latestState;
    }

	private void serverToServerHandshake() throws IOException, ClassNotFoundException, MTCPMigrationException, MTCPHandshakeException {
		//log("serverToServerHandshake initiated");
		ObjectOutputStream soos = new ObjectOutputStream(otherServer.getOutputStream());
        ObjectInputStream sois = new ObjectInputStream(otherServer.getInputStream());
		//log("created streams, will now try to read a packet. Will hope it is req state");
		Flag[] reqState = ((InternalPacket)sois.readObject()).getFlags();
		//log("read a packet, hoping it is REQ_STATE, let's check bro");
		if (reqState.length != 1) {
			throw new MTCPMigrationException("Did not get length 1 flags when expecting REQ_STATE");
		}
		if (!containsFlag(Flag.REQ_STATE, reqState)) {
			throw new MTCPMigrationException("Did not get REQ_STATE");
		}
		//log("YAYAYAYAYAY it was a REQ_STATE");
		Flag[] rspState = {Flag.RSP_STATE};
		InternalPacket<State> state = new InternalPacket<State>(rspState, latestState);
		soos.writeObject(state);
		//log("Wrote an RSP_STATE, let's look at another packet which i hope is an ACK");

		Flag[] ack = ((InternalPacket)sois.readObject()).getFlags();
		//log("just read something, gonna check if it is an ACK yo");
		if (ack.length != 1) {
			throw new MTCPMigrationException("Did not get length 1 flags when expecting ACK");
		}
		if (!containsFlag(Flag.ACK, ack)) {
			throw new MTCPMigrationException("Did not get ACK");
		}
		try {
			sois.close(); //#thisisnew
			soos.close(); //#thisisnew
			this.otherServer.close();
			// this.acceptServer();
			// this.handleIncomingPacket();
		} catch (EOFException e) {
			e.printStackTrace();
		}

		super.migrated.set(true);
		reinit();
		//close shit off properly!!!!
	}

	private void clientMigrationRequest(InetAddress address, int port) throws IOException, ClassNotFoundException, MTCPHandshakeException {
        //get actual address from mapping

		Flag[] ackMig = {Flag.ACK, Flag.MIG};
		oos.writeObject(new InternalPacket(ackMig, null));

        Socket s = getSocketFromMapping(address, port);
        ObjectOutputStream soos = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream sois = new ObjectInputStream(s.getInputStream());

        Flag[] flags = {Flag.REQ_STATE};
        InternalPacket p = new InternalPacket(flags);
        soos.writeObject(p);
		//log("Sent REQ_STATE packet");

        InternalPacket<State> response = (InternalPacket<State>) sois.readObject();
		//log("Got response");
        if (!containsFlag(Flag.RSP_STATE, response.getFlags())) {
			System.out.println(java.util.Arrays.toString(flags));
			throw new MTCPHandshakeException("Not got RSP_STATE flag");
		}
		//log("Got RSP_STATE");
        //got the state back.
        this.latestState = (State) response.getPayload();
        Flag[] serverResponseFlags = {Flag.ACK};
        soos.writeObject(new InternalPacket(serverResponseFlags));
		//log("Sent ACK response");
        try {
			//log("Closed connection to other server");
			soos.close();
			sois.close();
            s.close();
        } catch (Exception e) {
            // catch the errors thrown by streams;
			e.printStackTrace();
        }

        //tell the client that the migration was a success
		//log("Sending ACK, MIG_READY to client");
        Flag[] clientResponseFlags = {Flag.ACK, Flag.MIG_READY};
        this.oos.writeObject(new InternalPacket(clientResponseFlags));
		ackLock.set(true);
		//log("Handshake complete");

    }

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

  	protected void initialHandshake() throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		ackLock.set(true);
		//log("Beginning Handshake");
		InternalPacket p = (InternalPacket)ois.readObject();
		//log("Got Packet");
		if (p.getFlags().length > 2) {
			throw new MTCPHandshakeException("got wrong no. of flags on expected SYN");
		}
		if (!containsFlag(Flag.SYN, p.getFlags())) {
			throw new MTCPHandshakeException("did not get SYN, instead got:" + Arrays.toString(p.getFlags()));
		}
		if (containsFlag(Flag.MIG, p.getFlags())) {
			//log("Got SYN, MIG. Beginning server migration.");
			AddressMapping s1 = (AddressMapping)p.getPayload();
			clientMigrationRequest(s1.getPublicAddress(), s1.getPublicPort());
			//log("Server ready");
			ackLock.set(false);
			return;
		}
		//log("Got SYN flag");
		Flag[] synAck = {Flag.SYN, Flag.ACK};
		oos.writeObject(new InternalPacket(synAck, getClientMapping()));
		oos.flush();
		//log("Send SYN, ACK to client");
		//log("Waiting for ACK");
		Flag[] ackResponse = ((InternalPacket)ois.readObject()).getFlags();
		if (ackResponse.length != 1) {
			throw new MTCPHandshakeException("got wrong no. of flags on expected ACK");
		}
		if (!containsFlag(Flag.ACK, ackResponse)) {
			throw new MTCPHandshakeException("did not get ACK");
		}
		//got ack, yay!
		//log("Got ACK");
		//log("Handshake complete");
		ackLock.set(false);
		//log("Socket Ready");
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
						//logError("KILLLING MYSELF for reincarnation");
						logError("EOFException, migration??????????");
						break;
					} catch (SocketException e) {
						logError("SocketException, migration???????");
						break;
					}
					// System.err.println("&&&&&&&&&&&&&&&& did that read!");
					Flag[] f = p.getFlags();
					if (containsFlag(Flag.MESSAGE, f)) {
						inByteMessages.put(p.getPayload());
						this.latestState.addToBufferIn(p.getPayload());
						Flag[] flags = {Flag.ACK};

						oos.writeObject(new Packet(flags, null));
						//log("wrote ACK, btw timeout is " + socket.getSoTimeout());
					} else if (containsFlag(Flag.ACK, f)) {
						//log("got ACK packet");
						ackLock.set(false);

					}
				}
				System.err.println("DEEEEEADDD");

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
			// catch (Exception e) {
			// 	// e.printStackTrace();
			// 	System.err.println(":(");
			// }
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
				forcedWriteTimeout.set(false);
				System.err.println("ADVISE_MIG");
				Flag[] flags = {Flag.MESSAGE, Flag.ADVISE_MIG};
            	oos.writeObject(new Packet(flags, outgoingBytes));
			} else {
				Flag[] flags = {Flag.MESSAGE};
				oos.writeObject(new Packet(flags, outgoingBytes));
			}
			oos.flush();

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

	public boolean hasClient() {
		return hasClient;
	}
}
