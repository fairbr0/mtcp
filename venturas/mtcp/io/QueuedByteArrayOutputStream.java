package venturas.mtcp.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/* why not make this class generic rather than having to use
 * BlockingQueue<Object>? Because this way it will conform to the same interface
 * as Java's streams
 */

public class QueuedByteArrayOutputStream {
	private BlockingQueue<byte[]> messageQueue;

	public QueuedByteArrayOutputStream(BlockingQueue<byte[]> messageQueue) {
		this.messageQueue = messageQueue;
	}

	public void close() {
		this.messageQueue.clear();
	}

	public void flush() {
		throw new UnsupportedOperationException();
	}

	public void writeObject(byte[] t) {
		this.messageQueue.offer(t);
	}
}
