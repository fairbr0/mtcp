package venturas.mtcp.sockets;
import java.net.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public class ShellServerSocket extends AbstractMigratableParentSocket {
  private Socket client;
  private ServerSocket clientListener;
  private Object lastGuy = null;

  public ShellServerSocket(int clientPort, int serverPort) throws IOException, ClassNotFoundException {
    this(clientPort, serverPort, null);
  }

  public ShellServerSocket(int clientPort, int serverPort, List<AddressPortTuple> otherServers) throws IOException {
    super();
    this.clientListener = new ServerSocket(clientPort);
  }

  public void accept() throws IOException {
    this.client = clientListener.accept();
    super.os = new ObjectOutputStream(client.getOutputStream());
    super.is = new ObjectInputStream(client.getInputStream());
    this.incomingPacketsListener();
    this.outgoingPacketsListener();

  }

  public void performInitialHandshake() {

  }

  protected final void incomingPacketsListener() {
    (new Thread(() -> {
      try {
        while(true) {
          log("<Shell> waiting for input");
          Packet p = (Packet) super.is.readObject();
          if (super.containsFlag(Flag.SYN, p.getFlags())) {
            log("<Shell> got packet");
            lastGuy = p.getPayload();
            super.inMessageQueue.put(p.getPayload());
            super.sendACK();
          } else if (super.containsFlag(Flag.ACK, p.getFlags())) {
            log("<Shell> Got ACK");
            super.unlock();
          } else {
            log("<Shell> error");
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    })).start();
  }

  protected final void outgoingPacketsListener() {
    (new Thread(() -> {
      try {
        while (true) {
        //take will block if empty queue
          Flag[] spam = {Flag.SYN};

          log("<Shell> Doing a write:");
          while (super.getACKLock()) {
            //block
          }
          super.lock();
          os.writeObject(new Packet<Object>(spam, outMessageQueue.take()));
          os.flush();
          log("<Shell> wrote");

          Thread.sleep(1);
        }
      } catch (SocketTimeoutException t) {
        logError("TIMEOUT!");
      } catch (IOException d) {
        d.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    })).start();
  }
}
