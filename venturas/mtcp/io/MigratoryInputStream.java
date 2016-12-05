package venturas.mtcp.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import venturas.mtcp.sockets.MTCPStreamMigratedException;

/* Conforms to roughly the same interface as Java stream but is basically just a queue!
 * Why? Because it blocks if empty (so read methods block until there is an item to read)
 * and then the sockets can continue to write to the stream by just appending onto the queue,
 * as objects are passed by reference. But application can only read */

public class MigratoryInputStream {
	private BlockingQueue<byte[]> stream;
	private AtomicBoolean migrated;

	public MigratoryInputStream(BlockingQueue<byte[]> stream, AtomicBoolean migrated) {
		this.stream = stream;
		this.migrated = migrated;
	}

	public void close() {
		this.stream.clear();
	}

	public byte[] readBytes() throws InterruptedException, MTCPStreamMigratedException {
		//following will BLOCK until queue not empty
		while (stream.isEmpty()) {
			//block!
			if (migrated.get()) {
				migrated.set(false);
				//basically not really used by most applications, but might be by some!
				throw new MTCPStreamMigratedException("Socket for this MigratoryOutputStream has undergone migration");
			}
		}
		byte[] returnval = this.stream.take();
		return returnval;
	}
}
