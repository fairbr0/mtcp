package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public class ShellSocket extends AbstractMigratableParentSocket {

  private Socket server;

  public ShellSocket(InetSocketAddress socketAddress) throws IOException, ClassNotFoundException {
    super();
    this.server = new Socket(socketAddress.getAddress(), socketAddress.getPort());
    super.os = new ObjectOutputStream(server.getOutputStream());
    super.is = new ObjectInputStream(server.getInputStream());

    this.incomingPacketsListener();
    this.outgoingPacketsListener();
  }

  public void performInitialHandshake() {

  }

  protected final void incomingPacketsListener() {
    (new Thread(() -> {
      try {
        while (true) {
          Packet p = (Packet) is.readObject();
          if (super.containsFlag(Flag.SYN, p.getFlags())) {
            super.inMessageQueue.put(p.getPayload());
            super.sendACK();
          } else if (super.containsFlag(Flag.ACK, p.getFlags())){
            log("<Shell> Got ACK");
            super.unlock();
          } else {
            log("Unexpected flag(s), got: " + java.util.Arrays.toString(p.getFlags()));
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
          Flag[] spam = {Flag.SPAMSPAMSPAMSPAMBACONANDSPAM, Flag.SYN};
          log("<Shell> Doing a write:");
          log(server.getPort() + " " + server.getLocalPort());
          while (super.getACKLock()) {
            //block
            log("blocked");
            Thread.sleep(10);
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
