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

  public MSock(InetSocketAddress socketAddress) {
    try {
      System.err.println("<msock> creating socket");
      this.active = new Socket(socketAddress.getAddress(), socketAddress.getPort());
      System.err.println("<msock> have created a socket");
      this.os = new ObjectOutputStream(active.getOutputStream());
      System.err.println("<msock> have created out stream");
      this.is = new ObjectInputStream(active.getInputStream());
      System.err.println("<msock> have created in stream");

      initialHandshake();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException d) {
      d.printStackTrace();
    }
  }


  ///all below methods are private to the class and the client should not be using them.

  private void initialHandshake() throws IOException, ClassNotFoundException {

    System.err.println("<msock> Started handshake");

    boolean handshakeException = false;
    String[] args = new String[2];
    args[0] = "SYN";
    args[1] = "1";

    //send first SYN to the server.
    this.active.setSoTimeout(5000);
    System.err.println("<msock> Sending SYN");
    this.os.writeObject(new Packet<String>(args));
    this.os.flush();

    //wait for response of SYN plus ACK, Server list
    System.err.println("<msock> Waiting for read");
    Packet<ArrayList<ServerListItem>> response = (Packet<ArrayList<ServerListItem>>) this.is.readObject();
    System.err.println("<msock> Read input");
    if (response.getArgs().length == 2) {
      if (response.getArgs()[0].equals("SYN") && response.getArgs()[1].equals("ACK")) {
        System.err.println("<msock> Got SYN ACK, getting payload");
        this.serverList = response.getPayload();
      } else {
        System.err.println("<msock> Not SYN ACK");
        handshakeException = true;
      }
    } else {
      System.err.println("<msock> Not length 2");
      handshakeException = true;
    }

    if (handshakeException) { throw new IOException(); }

    args[0] = "BOB";
    args[1] = "2";
    System.err.println("<msock> Writing " + args[0]);
    this.os.flush();
    this.os.writeObject(new Packet<String>(args));
    this.os.flush();
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
    System.err.println("<mserversock> Instantiating server sockets");
    this.serverListener = new ServerSocket(portB);
    this.clientListener = new ServerSocket(portA);
    System.err.println("<mserversock> Done server sockets");
    acceptClient();
  }

  public void acceptClient() throws IOException {
    Thread acceptClient = new Thread(() -> {
      try {
        System.err.println("<mserversock> clientListener.accept about to be called");
        this.client = clientListener.accept();
        System.err.println("<mserversock> accepted client");
        this.os = new ObjectOutputStream(client.getOutputStream());
        System.err.println("<mserversock> created out stream");
        this.is = new ObjectInputStream(client.getInputStream());
        System.err.println("<mserversock> created in stream");
        initialClientHandshake();
        listenClient();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    acceptClient.start();
  }

  private void listenClient() {
    try {
      do {
        Packet packet = (Packet) is.readObject();
        System.out.println("got message");
        handleClientRequest(packet);
      } while (true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handleClientRequest(Packet packet) {

  }

  private void initialClientHandshake() throws IOException, ClassNotFoundException {
    System.err.println("<mserversock> Performing init client handshake");
    boolean handshakeException = false;

    this.client.setSoTimeout(5000);
    System.err.println("<mserversock> Reading response now");
    Packet<String> response = (Packet<String>) is.readObject();
    System.err.println("<mserversock> Got response now");
    if (response.getArgs().length == 2) {
      if (!response.getArgs()[0].equals("SYN")) {
        System.err.println("<mserversock> Got not SYN");
        handshakeException = true;
      } else {
        System.err.println(response.getArgs()[0]);
      }
    } else  {
      System.err.println("<mserversock> Args not length 1");
      handshakeException = true;
    }

    if (handshakeException) { throw new IOException(); }

    String[] args = new String[2];
    args[0] = "SYN";
    args[1] = "ACK";
    System.err.println("<mserversock> Writing SYN ACK");
    this.os.writeObject(new Packet<List<ServerListItem>>(args, this.otherServers));
    this.os.flush();
    System.err.println("<mserversock> Write done, reading object now");
    Packet<String> respb = (Packet<String>) is.readObject();
    Packet<String> resp = (Packet<String>) is.readObject();
    if (resp.getArgs().length == 2) {
      if (!resp.getArgs()[0].equals("ACK")) {
        System.err.println("<mserversock> Read message, not ACK, got: " + resp.getArgs()[0] + "," + resp.getArgs()[1]);
        handshakeException = true;
      }
    } else {
      System.err.println("<mserversock> Read message, not length 1");
      handshakeException = true;
    }

    if (handshakeException) { throw new IOException(); }
    this.client.setSoTimeout(0);
    return;
  }
}
