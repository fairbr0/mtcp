package venturas.mtcp.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import venturas.mtcp.sockets.MTCPStreamMigratedException;

/* why not make this class generic rather than having to use
 * BlockingQueue<Object>? Because this way it will conform to the same interface
 * as Java's streams
 */

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
