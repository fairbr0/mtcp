package venturas.mtcp.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueuedObjectOutputStream<T> {
	private BlockingQueue<T> messageQueue;

	public QueuedObjectOutputStream(BlockingQueue messageQueue) {
		this.messageQueue = messageQueue;
	}

	public void close() {
		this.messageQueue.clear();
	}

	public void flush() {
		throw new UnsupportedOperationException();
	}

	public void writeObject(T t) {
		this.messageQueue.offer(t);
	}
}
