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
	protected BlockingQueue<Object> inMessageQueue;
	protected BlockingQueue<Object> outMessageQueue;
	private QueuedObjectInputStream qis;
	private QueuedObjectOutputStream qos;
	private final String loggingLabel;
	protected boolean queueStreamsLocked;

	protected AbstractMigratableParentSocket() {
		inMessageQueue = new LinkedBlockingQueue<Object>();
		outMessageQueue = new LinkedBlockingQueue<Object>();
		qis = new QueuedObjectInputStream(inMessageQueue);
		qos = new QueuedObjectOutputStream(outMessageQueue);
		queueStreamsLocked = false;
		if (this instanceof MigratableSocket) {
			loggingLabel = "<MSocket>";
		} else {
			loggingLabel = "<MServerSckt>";
		}
	}
	
	protected abstract void incomingPacketsListener();

	protected final void outgoingPacketsListener() {
		(new Thread(() -> {
			while (true) {
				//take will block if empty queue
				Flag[] spam = {Flag.SPAMSPAMSPAMSPAMBACONANDSPAM};
				try {
					os.writeObject(new Packet<Object>(spam, outMessageQueue.take()));
					os.flush();
				} catch (SocketTimeoutException e) {
					logError("TIMEOUT!");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		})).start();
	}

	public final QueuedObjectOutputStream getOutputStream() {
		return qos;
	}

	public final QueuedObjectInputStream getInputStream() {
		return qis;
	}

	protected final void logError(String msg) {
		System.err.println(loggingLabel + " {ERR} " + msg);
	}

	protected final void log(String msg) {
		System.out.println(loggingLabel + " " + msg);
	}

	protected abstract void performInitialHandshake() throws MTCPHandshakeException, MTCPMigrationException, IOException, ClassNotFoundException;
}
