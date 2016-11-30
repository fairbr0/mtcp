package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;


public class SerializedShellServerSocket {

  private Socket socket;
  private MigratoryInputStream is;
  private MigratoryOutputStream os;
  private BlockingQueue<byte[]> inByteMessages;
  private BlockingQueue<byte[]> outByteMessages;
  private ObjectOutputStream oos;
  private ObjectInputStream ois;
  private AtomicBoolean ackLock = new AtomicBoolean(false);
  private List<AddressPortTuple> otherServers;
  private State latestState;

  public SerializedShellServerSocket(int port) throws Exception {

public class SerializedShellServerSocket extends AbstractSerializedShellSocket {

  private Socket socket;

  public SerializedShellServerSocket(int port) throws Exception {
    socket = (new ServerSocket(port)).accept();

    oos = new ObjectOutputStream(socket.getOutputStream());
    outByteMessages = new LinkedBlockingQueue<byte[]>();
    os = new MigratoryOutputStream(outByteMessages);

    ois = new ObjectInputStream(socket.getInputStream());
    inByteMessages = new LinkedBlockingQueue<byte[]>();
    is = new MigratoryInputStream(inByteMessages);

    handleIncomingPacket();
    handleOutgoingPacket();
  }D

  public MigratoryOutputStream getOutputStream() {
    return os;
  }

  public MigratoryInputStream getInputStream() {
    return is;
  }

  public void accept() {
      this.socket = (new ServerSocket(port)).accept();

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
      ServerPacket p = new ServerPacket(flags);
      soos.writeObject(p);

      ServerPacket<State> response = (ServerPacket<State>) sois.readObject();

      if (containsFlag(Flag.RSP_STATE, flags
      )) {
          //got the state back.
          this.latestState = (State) response.getPayload();
          Flag[] serverResponseFlags = {Flag.ACK};
          soos.writeObject(new ServerPacket(serverResponseFlags));
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

  public void setOtherServers(List<AddressPortTuple> otherServers) {
      this.otherServers = otherServers;
  }

  public Socket getSocketFromMapping(InetAddress address, int port) throws IOException{
      int mappedPort = 0;
      InetAddress mappedAddress = null;
      Iterator<AddressPortTuple> it = otherServers.iterator();
      while (it.hasNext()) {
          AddressPortTuple tuple = it.next();
          if (address.equals(tuple.getAddress())) {
              mappedAddress = tuple.getAddress();
              int[] ports = tuple.getPorts();
              if (ports.length > 0) {
                  if (ports[0] == port) {
                      mappedPort = ports[1];
                  } else {
                      mappedPort = ports
                      [0];
                  }
              }
              break;
          }
      }
      Socket server = new Socket(mappedAddress, mappedPort);
      return server;
  }

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


  protected boolean containsFlag(Flag f, Flag[] flags) {
		for (Flag p : flags) {
			if (p == f) return true;
		}

		return false;
	}



}
