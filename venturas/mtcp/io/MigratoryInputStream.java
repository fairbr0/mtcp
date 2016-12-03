package venturas.mtcp.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import venturas.mtcp.sockets.MTCPStreamMigratedException;

/* why not make this class generic rather than having to use
 * BlockingQueue<Object>? Because this way it will conform to the same interface
 * as Java's streams
 */

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
				throw new MTCPStreamMigratedException("Socket for this MigratoryOutputStream has undergone migration");
			}
		}
		byte[] returnval = this.stream.take();
		return returnval;
	}
}
