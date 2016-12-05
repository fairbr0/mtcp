package venturas.mtcp.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import venturas.mtcp.sockets.MTCPStreamMigratedException;

/* Conforms to roughly the same interface as Java stream but is basically just a queue!
 * Why? Because the sockets can continue to write to the stream by just appending onto the queue,
 * as objects are passed by reference. But application can only write onto it and not read */

public class MigratoryOutputStream {
	private BlockingQueue<byte[]> messageQueue;
	private AtomicBoolean migrated;

	public MigratoryOutputStream(BlockingQueue<byte[]> messageQueue, AtomicBoolean migrated) {
		this.messageQueue = messageQueue;
		this.migrated = migrated;
	}

	public void close() {
		this.messageQueue.clear();
	}

	public void flush() {
		throw new UnsupportedOperationException();
	}

	public void writeBytes(byte[] t) throws MTCPStreamMigratedException {
		this.messageQueue.offer(t);
		if (migrated.get()) {
			migrated.set(false);
			throw new MTCPStreamMigratedException("Socket for this MigratoryOutputStream has undergone migration");
		}
	}
}
