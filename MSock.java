import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class MSock {

  Socket active;
  List<ServerListItem> serverList;
  int inUse;
  ObjectInputStream is;
  ObjectOutputStream os;

  public MSock(InetAddress address, int port) {
    try {
      this.active = new Socket(address, port);
      this.is = new ObjectInputStream(active.getInputStream());
      this.os = new ObjectOutputStream(active.getOutputStream());

      initialHandshake();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException d) {
      d.printStackTrace();
    }
  }


  ///all below methods are private to the class and the client should not be using them.

  private void initialHandshake() throws IOException, ClassNotFoundException {
    boolean handshakeException = false;
    String[] args = new String[1];
    args[0] = "SYN";

    //send first SYN to the server.
    this.active.setSoTimeout(5000);
    this.os.writeObject(new Packet<String>(args));

    //wait for response of SYN plus ACK, Server list
    Packet<ArrayList<ServerListItem>> response = (Packet<ArrayList<ServerListItem>>) this.is.readObject();
    if (response.getArgs().length == 2) {
      if (response.getArgs()[0].equals("SYN") && response.getArgs()[1].equals("ACK")) {
        this.serverList = response.getPayload();
      } else {
        handshakeException = true;
      }
    } else {
      handshakeException = true;
    }

    if (handshakeException) throw new IOException();

    args[0] = "ACK";
    this.os.writeObject(new Packet<String>(args));
    this.active.setSoTimeout(0);
    return;
  }

  public void getInStream() {

  }

  public void getOutStream() {

  }
}

class ServerListItem {
  public InetAddress address;
  public int port;

  public ServerListItem(InetAddress address, int port) {
    this.address = address;
    this.port = port;
  }
}

class Packet<T> implements Serializable {

  String[] args;
  T payload;

  public Packet(String[] args) {
    this.args = args;
    this.payload = null;
  }

  public Packet(String[] args, T payload) {
    this.args = args;
    this.payload = payload;
  }

  public void setPayload(T payload) {
    this.payload = payload;
  }

  public String[] getArgs() {
    return this.args;
  }

  public T getPayload() {
    return this.payload;
  }
}

class MServerSocket {
  // check that only 1 conn exists

  Socket client;
  //Socket otherServer;
  ServerSocket clientListener;
  ServerSocket serverListener;
  ObjectInputStream is;
  ObjectOutputStream os;
  List<ServerListItem> otherServers;

  public MServerSocket(int portA, int portB) throws IOException, ClassNotFoundException {
    this.serverListener = new ServerSocket(portA);
    this.clientListener = new ServerSocket(portB);
  }

  public void acceptServer() {

  }

  private void initialClientHandshake() throws IOException, ClassNotFoundException {
    boolean handshakeException = false;

    this.client.setSoTimeout(5000);
    Packet<String> response = (Packet<String>) is.readObject();
    if (response.getArgs().length == 1) {
      if (!response.getArgs()[0].equals("SYN")) {
        handshakeException = true;
      }
    } else  {
      handshakeException = true;
    }

    if (handshakeException) { throw new IOException(); }

    String[] args = new String[2];
    args[0] = "SYN";
    args[1] = "ACK";
    this.os.writeObject(new Packet<List<ServerListItem>>(args, this.otherServers));

    response = (Packet<String>) is.readObject();
    if (response.getArgs().length == 1) {
      if (!response.getArgs()[0].equals("ACK")) {
        handshakeException = true;
      }
    } else {
      handshakeException = true;
    }

    if (handshakeException) { throw new IOException(); }
    this.client.setSoTimeout(0);
    return;
  }
}
