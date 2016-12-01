package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public class MServerSock extends AbstractMSock {
	private List<AddressPortTuple> otherServers;
    private State latestState;
	private int clientPort;
	private int serverPort;
	private Socket otherServer;
	private boolean hasClient;

  	public MServerSock(int clientPort, int serverPort, List<AddressPortTuple> otherServers) throws IOException, ClassNotFoundException, MTCPHandshakeException {
		//TODO setup list of servers
		super(); //does nothing
		this.clientPort = clientPort;
		this.serverPort = serverPort;
		this.hasClient = false;
		this.otherServers = otherServers;
  	}

	/** This is NOT blocking **/
	public void accept() {
		this.acceptServer();
		this.acceptClient();
	}

	//TODO implement me
	private void acceptServer() {
		(new Thread(() -> {
			//TODO implement me
			logError("acceptServer() worked, but needs implementation");
		})).start();
	}

	private void acceptClient() {
		(new Thread(() -> {
			try {
				ServerSocket ss = new ServerSocket(clientPort);
				System.err.println("made ss, now to super.acceptClient!");
				super.acceptClient(ss.accept());
				System.err.println("super.acceptClient did its work, WHAT THE FUCK IS WRONG");
				hasClient = true;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (MTCPHandshakeException e) {
				e.printStackTrace();
			}
		})).start();
	}

	public void exportState(State state) {
        this.latestState = state;
    }

    public State importState() {
        while (this.latestState == null) {
            //block
        }
        return this.latestState;
    }

	public void migrate(InetAddress address, int port) throws Exception {
        //get actual address from mapping
        Socket s = getSocketFromMapping(address, port);
        ObjectOutputStream soos = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream sois = new ObjectInputStream(s.getInputStream());

        Flag[] flags = {Flag.REQ_STATE};
        InternalPacket p = new InternalPacket(flags);
        soos.writeObject(p);

        InternalPacket<State> response = (InternalPacket<State>) sois.readObject();

        if (containsFlag(Flag.RSP_STATE, flags)) {
            //got the state back.
            this.latestState = (State) response.getPayload();
            Flag[] serverResponseFlags = {Flag.ACK};
            soos.writeObject(new InternalPacket(serverResponseFlags));
            try {
                s.close();
            } catch (Exception e) {
                // catch the errors thrown by streams;
            }

            //tell the client that the migration was a success
            Flag[] clientResponseFlags = {Flag.ACK, Flag.MIG};
            this.oos.writeObject(new Packet(clientResponseFlags));

        } else {
            throw new Exception();
        }

    }

    public Socket getSocketFromMapping(InetAddress address, int port) throws IOException, MTCPMigrationException {
        int mappedPort = 0;
        InetAddress mappedAddress = null;
        Iterator<AddressPortTuple> it = otherServers.iterator();
        while (it.hasNext()) {
            AddressPortTuple tuple = it.next();
            if (address.equals(tuple.getAddress())) {
                mappedAddress = tuple.getAddress();
                int[] ports = tuple.getPorts();
                if (ports.length == 2) {
                    if (ports[0] == port) {
                        mappedPort = ports[1];
                    } else {
                        mappedPort = ports
                        [0];
                    }
                } else {
					throw new MTCPMigrationException();
				}
                break;
            }
        }
        Socket server = new Socket(mappedAddress, mappedPort);
        return server;
    }

  	protected void initialHandshake() throws IOException, ClassNotFoundException, MTCPHandshakeException {
		ackLock.set(true);
		InternalPacket p = (InternalPacket)ois.readObject();
		if (p.getFlags().length != 1) {
			throw new MTCPHandshakeException("got wrong no. of flags on expected SYN");
		}
		if (!containsFlag(Flag.SYN, p.getFlags())) {
			throw new MTCPHandshakeException("did not get SYN");
		}
		Flag[] synAck = {Flag.SYN, Flag.ACK};
		oos.writeObject(new InternalPacket(synAck, getClientMapping()));
		oos.flush();
		Flag[] ackResponse = ((InternalPacket)ois.readObject()).getFlags();
		if (ackResponse.length != 1) {
			throw new MTCPHandshakeException("got wrong no. of flags on expected ACK");
		}
		if (!containsFlag(Flag.ACK, ackResponse)) {
			throw new MTCPHandshakeException("did not get ACK");
		}
		//got ack, yay!
		ackLock.set(false);
  	}

	private List<AddressPortTuple> getClientMapping() {
		List<AddressPortTuple> result = new LinkedList<AddressPortTuple>();
		Iterator<AddressPortTuple> it = otherServers.iterator();
		while (it.hasNext()) {
			AddressPortTuple next = it.next();
			AddressPortTuple clientNext = new AddressPortTuple(next.getAddress(), next.getPorts()[0]);
			result.add(clientNext);
		}
		return result;
	}

	@Override
	public void handleIncomingPacket() {
      (new Thread(() -> {
        try {
          while(true) {
            Packet p = (Packet)ois.readObject();
            Flag[] f = p.getFlags();
            if (containsFlag(Flag.SYN, f)) {
              inByteMessages.put(p.getPayload());
              this.latestState.addToBufferIn(p.getPayload());
              ackLock.set(true);
              Flag[] flags = {Flag.ACK};
              oos.writeObject(new Packet(flags, null));
            } else if (containsFlag(Flag.ACK, f)) {
              ackLock.set(false);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      })).start();
    }

	@Override
    public void handleOutgoingPacket() {
      (new Thread(() -> {
        try {
          while(true) {
            while (ackLock.get()) {
              //block
            }
            ackLock.set(true);
            Flag[] flags = {Flag.SYN};
            byte[] outgoingBytes = outByteMessages.take();
            oos.writeObject(new Packet(flags, outgoingBytes));
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
