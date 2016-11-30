package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public class SerializedShellSocket {

    private Socket socket;
    private MigratoryInputStream is;
    private MigratoryOutputStream os;
    private BlockingQueue<byte[]> inByteMessages;
    private BlockingQueue<byte[]> outByteMessages;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private AtomicBoolean ackLock = new AtomicBoolean(false);

    public SerializedShellSocket(InetAddress address, int port) throws Exception {

        socket = new Socket("localhost", port);

        oos = new ObjectOutputStream(socket.getOutputStream());
        outByteMessages = new LinkedBlockingQueue<byte[]>();
        os = new MigratoryOutputStream(outByteMessages);

        ois = new ObjectInputStream(socket.getInputStream());
        inByteMessages = new LinkedBlockingQueue<byte[]>();
        is = new MigratoryInputStream(inByteMessages);

        handleIncomingPacket();
        handleOutgoingPacket();
    }

    public MigratoryOutputStream getOutputStream() {
        return os;
    }

    public MigratoryInputStream getInputStream() {
        return is;
    }

    public void handleIncomingPacket() {
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
