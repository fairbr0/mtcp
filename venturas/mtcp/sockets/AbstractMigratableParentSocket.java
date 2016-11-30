package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public abstract class AbstractMigratableParentSocket {
	protected ObjectInputStream is; //child must instantiate
	protected ObjectOutputStream os; //child must instantiate
	protected BlockingQueue<byte[]> inMessageQueue;
	protected BlockingQueue<byte[]> outMessageQueue;
	private QueuedByteArrayInputStream qis;
	private QueuedByteArrayOutputStream qos;
	private final String loggingLabel;
	private boolean ackLock;

	protected AbstractMigratableParentSocket() {
		inMessageQueue = new LinkedBlockingQueue<byte[]>();
		outMessageQueue = new LinkedBlockingQueue<byte[]>();
		qis = new QueuedByteArrayInputStream(inMessageQueue);
		qos = new QueuedByteArrayOutputStream(outMessageQueue);
		ackLock = false;
		// if (this instanceof MigratableSocket) {
		 	loggingLabel = "<MSocket>";
		// } else {
		// 	loggingLabel = "<MServerSckt>";
		// }
	}

	protected abstract void incomingPacketsListener();

	protected abstract void outgoingPacketsListener();

	public final QueuedByteArrayOutputStream getOutputStream() {
		return qos;
	}

	public final QueuedByteArrayInputStream getInputStream() {
		return qis;
	}

	protected final void logError(String msg) {
		System.err.println(loggingLabel + " {ERR} " + msg);
	}

	protected final void log(String msg) {
		System.out.println(loggingLabel + " " + msg);
	}

	protected abstract void performInitialHandshake() throws MTCPHandshakeException, MTCPMigrationException, IOException, ClassNotFoundException;

	protected boolean containsFlag(Flag f, Flag[] flags) {
		for (Flag p : flags) {
			if (p == f) return true;
		}

		return false;
	}

	protected void sendACK() throws IOException, ClassNotFoundException {
		Flag[] flags = {Flag.ACK};
		Packet p = new Packet(flags, null);
		this.os.writeObject(p);
		log("Sent ACK");
	}

	protected synchronized void lock() {
		ackLock = true;
	}

	protected synchronized void unlock() {
		ackLock = false;
	}

	protected synchronized boolean getACKLock() {
		return ackLock;
	}
}
