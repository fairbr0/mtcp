package venturas.mtcp.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/* why not make this class generic rather than having to use
 * BlockingQueue<Object>? Because this way it will conform to the same interface
 * as Java's streams
 */

public class QueuedObjectInputStream {
	private BlockingQueue<Object> stream;

	public QueuedObjectInputStream(BlockingQueue<Object> stream) {
		this.stream = stream;
	}

	public void close() {
		this.stream.clear();
	}

	public Object readObject() throws InterruptedException {
		//following will BLOCK until queue not empty
		return this.stream.take();
	}
}
