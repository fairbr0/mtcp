package venturas.mtcp.sockets;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import venturas.mtcp.packets.*;
import venturas.mtcp.io.*;

public abstract class AbstractMSock {
	Socket socket;
	MigratoryInputStream is;
	MigratoryOutputStream os;
	protected BlockingQueue<byte[]> inByteMessages;
	protected BlockingQueue<byte[]> outByteMessages;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	protected AtomicBoolean ackLock = new AtomicBoolean(false);

	public AbstractMSock() {
		//do nothing
	}

	public AbstractMSock(Socket s) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		acceptClient(s);
	}

	protected void acceptClient(Socket s) throws IOException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException {
		socket = s;
		oos = new ObjectOutputStream(socket.getOutputStream());
		outByteMessages = new LinkedBlockingQueue<byte[]>();
		os = new MigratoryOutputStream(outByteMessages);

		ois = new ObjectInputStream(socket.getInputStream());
		inByteMessages = new LinkedBlockingQueue<byte[]>();
		is = new MigratoryInputStream(inByteMessages);

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
