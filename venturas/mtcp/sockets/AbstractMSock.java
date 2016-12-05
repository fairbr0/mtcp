package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

/* Abstract migratory socket - covers functionality shared by both client and server facing migratory sockets */
public abstract class AbstractMSock {
	protected Socket socket;
	private MigratoryInputStream is; //Input stream facing the application, basically just a queue.
	private MigratoryOutputStream os;
	protected BlockingQueue<byte[]> inByteMessages; //queue that goes inside the migratory in stream by reference
	protected BlockingQueue<byte[]> outByteMessages;
	protected ObjectOutputStream oos; //Java stream out to other sockets, we send Packets and InternalPackets over these!
	protected ObjectInputStream ois;
	protected AtomicBoolean ackLock = new AtomicBoolean(false);
	protected AtomicBoolean migrated = new AtomicBoolean(false); //did we just migrate?
	protected AtomicBoolean forcedReadTimeout = new AtomicBoolean(false); //Matt Bradbury forced a migrate!
	protected AtomicBoolean forcedWriteTimeout = new AtomicBoolean(false); //Matt Bradbury forced a migrate!

	// Just instatiate the queues
	public AbstractMSock() {
		outByteMessages = new LinkedBlockingQueue<byte[]>();
		inByteMessages = new LinkedBlockingQueue<byte[]>();
	}

	// Used by MServerSock
	public AbstractMSock(Socket s) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		this();
		acceptClient(s);
	}

	//For Matt B
	public void forceWriteTimeout() {
		forcedWriteTimeout.set(true);
	}

	public void forceReadTimeout() {
		forcedReadTimeout.set(true);
	}

	/* Accepts a client in on socket */
	protected void acceptClient(Socket s) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		socket = s;
		oos = new ObjectOutputStream(socket.getOutputStream());
		os = new MigratoryOutputStream(outByteMessages, migrated);
		ois = new ObjectInputStream(socket.getInputStream());
		is = new MigratoryInputStream(inByteMessages, migrated);

		// Perform the init handshake and then call the listener threads
		initialHandshake();
		handleIncomingPacket();
		handleOutgoingPacket();
	}

	// delegated to child
	protected abstract void initialHandshake() throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException;

	// let apps get the streams
	public MigratoryOutputStream getOutputStream() {
		return os;
	}

	public MigratoryInputStream getInputStream() {
		return is;
	}

	// delegated to child
	protected abstract void handleIncomingPacket() throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException;

	protected abstract void handleOutgoingPacket();

	// Check if f is in flags. Should have used a SET of flags in hindsight, but didn't wanna risk it breaking
	protected boolean containsFlag(Flag f, Flag[] flags) {
		for (Flag p : flags) {
			if (p == f) return true;
		}
		return false;
	}

	protected final void logError(String msg) {
		System.err.println(getLabel() + " {ERR} " + msg);
	}

	protected final void log(String msg) {
		System.out.println(getLabel() + " " + msg);
	}

	protected abstract String getLabel();
}
