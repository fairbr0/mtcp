package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public abstract class AbstractMSock {
	protected Socket socket;
	private MigratoryInputStream is;
	private MigratoryOutputStream os;
	protected BlockingQueue<byte[]> inByteMessages;
	protected BlockingQueue<byte[]> outByteMessages;
	protected ObjectOutputStream oos;
	protected ObjectInputStream ois;
	protected AtomicBoolean ackLock = new AtomicBoolean(false);
	protected AtomicBoolean migrated = new AtomicBoolean(false);
	protected AtomicBoolean forcedReadTimeout = new AtomicBoolean(false);
	protected AtomicBoolean forcedWriteTimeout = new AtomicBoolean(false);

	public AbstractMSock() {
		outByteMessages = new LinkedBlockingQueue<byte[]>();
		inByteMessages = new LinkedBlockingQueue<byte[]>();
	}

	public AbstractMSock(Socket s) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		this();
		acceptClient(s);
	}

	public void forceWriteTimeout() {
		forcedWriteTimeout.set(true);
	}

	public void forceReadTimeout() {
		forcedReadTimeout.set(true);
	}

	protected void acceptClient(Socket s) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		socket = s;
		oos = new ObjectOutputStream(socket.getOutputStream());
		os = new MigratoryOutputStream(outByteMessages, migrated);
		ois = new ObjectInputStream(socket.getInputStream());
		is = new MigratoryInputStream(inByteMessages, migrated);

		initialHandshake();
		handleIncomingPacket();
		handleOutgoingPacket();
	}

	protected abstract void initialHandshake() throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException;

	public MigratoryOutputStream getOutputStream() {
		return os;
	}

	public MigratoryInputStream getInputStream() {
		return is;
	}

	protected abstract void handleIncomingPacket() throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException;

	protected abstract void handleOutgoingPacket();

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
