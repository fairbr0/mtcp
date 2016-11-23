import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

class MServerSocket {
  // check that only 1 conn exists

  Socket client;
  //Socket otherServer;
  ServerSocket clientListener;
  ServerSocket serverListener;
  ObjectInputStream is;
  ObjectOutputStream os;
  List<ServerListItem> otherServers;
  LinkedList<Object> il;
  LinkedList<Object> ol;

  public MServerSocket(int portA, int portB) throws IOException, ClassNotFoundException {
    System.err.println("<mserversock> Instantiating server sockets");
    this.serverListener = new ServerSocket(portB);
    this.clientListener = new ServerSocket(portA);
    System.err.println("<mserversock> Done server sockets");
    this.il = new LinkedList<Object>();
    this.ol = new LinkedList<Object>();

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
        processOutputStream();
        System.err.println("<mserversock> Finished setup");
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    acceptClient.start();

  }

  public void putOutputStream(Object p) {
    this.ol.add(p);
  }

  public Object getInputStream() {
    Object obj = null;
    do {
      if (this.il.size() != 0) {
        obj = il.pop();
        break;
      }
      try {
        Thread.sleep(1);
      } catch (Exception e) {e.printStackTrace();}
    } while (true);
    return obj;
  }

  //////////

  private void acceptServer() {
    Thread thread = new Thread(() -> {
      try {
        do {
          Socket server = serverListener.accept();
          handleServerMigrationRequest(server);
        } while (true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    thread.start();
  }

  private void listenClient() {
    Thread thread = new Thread(() -> {
      try {
        do {
          Packet packet = (Packet) is.readObject();
          System.out.println("<mserversock> <got message>");
          handleClientRequest(packet);
        } while (true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    thread.start();
  }

  private void processOutputStream() throws IOException {
    Thread thread = new Thread(() -> {
      try {
        do {
          if (ol.size() != 0) {
            String[] args = new String[0];
            Packet p = new Packet(args, ol.pop());
            System.err.println("<mserversock> sending packet");
            sendPacket(p);
          }
          Thread.sleep(1);
        } while (true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    thread.start();
  }

  private void sendPacket(Packet p) throws IOException {
    this.os.writeObject(p);
  }

  private void handleClientRequest(Packet packet) {
    System.err.println("<mserversocket> <got message : " + packet.getPayload().toString() + " >");
    //logic to decide what to do with the packet using the headers
    this.il.add(packet.getPayload());
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
    Packet<String> resp = (Packet<String>) is.readObject();
    if (resp.getArgs().length == 2) {
      if (!resp.getArgs()[0].equals("ACK")) {
        System.err.println("<mserversock> Read message, not ACK, got: " + resp.getArgs()[0]);
        handshakeException = true;
      }
    } else {
      System.err.println("<mserversock> Read message, not length 1");
      handshakeException = true;
    }

    if (handshakeException) { throw new IOException(); }
    System.err.println("<mserversock> Handshake complete");
    this.client.setSoTimeout(0);
    return;
  }
}
