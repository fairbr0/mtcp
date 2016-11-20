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
	private String loggingLabel;

	protected AbstractMigratableParentSocket() {
		inMessageQueue = new LinkedBlockingQueue<Object>();
		outMessageQueue = new LinkedBlockingQueue<Object>();
		qis = new QueuedObjectInputStream(inMessageQueue);
		qos = new QueuedObjectOutputStream(outMessageQueue);
		if (this instanceof MigratableSocket) {
			loggingLabel = "<MSocket>";
		} else {
			loggingLabel = "<MServerSckt>";
		}
	}

	protected void handleIncomingPackets() {
		(new Thread(() -> {
			while (true) {
				try {
					inMessageQueue.put(((Packet)is.readObject()).getPayload());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
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

	protected void handleOutgoingPackets() {
		(new Thread(() -> {
			while (true) {
				//take will block if empty queue
				Flag[] spam = {Flag.SPAMSPAMSPAMSPAMBACONANDSPAM};
				try {
					os.writeObject(new Packet<Object>(spam, outMessageQueue.take()));
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

	public QueuedObjectOutputStream getOutputStream() {
		return qos;
	}

	public QueuedObjectInputStream getInputStream() {
		return qis;
	}

	protected void logError(String msg) {
		System.err.println(loggingLabel + " {ERR} " + msg);
	}

	protected void log(String msg) {
		System.out.println(loggingLabel + " " + msg);
	}

	protected abstract void performInitialHandshake() throws MTCPHandshakeException, IOException, ClassNotFoundException;
}
