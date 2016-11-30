package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public class AbstractSerializedShellSocket {
  MigratoryInputStream is;
  MigratoryOutputStream os;
  protected BlockingQueue<byte[]> inByteMessages;
  protected BlockingQueue<byte[]> outByteMessages;
  ObjectOutputStream oos;
  ObjectInputStream ois;
  protected AtomicBoolean ackLock = new AtomicBoolean(false);

  public MigratoryOutputStream getOutputStream() {
    return os;
  }

  public MigratoryInputStream getInputStream() {
    return is;
  }

  protected void handleIncomingPacket() {
    (new Thread(() -> {
      try {
        while(true) {
          Packet p = (Packet)ois.readObject();
          Flag[] f = p.getFlags();
          if (containsFlag(Flag.SYN, f)) {
            inByteMessages.put(p.getPayload());
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

  protected void handleOutgoingPacket() {
    (new Thread(() -> {
      try {
        while(true) {
          while (ackLock.get()) {
            //block
          }
          Flag[] flags = {Flag.SYN};
          oos.writeObject(new Packet(flags, outByteMessages.take()));
          ackLock.set(true);
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
