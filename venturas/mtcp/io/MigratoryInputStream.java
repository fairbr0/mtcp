package venturas.mtcp.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/* why not make this class generic rather than having to use
 * BlockingQueue<Object>? Because this way it will conform to the same interface
 * as Java's streams
 */

public class MigratoryInputStream {
	private BlockingQueue<byte[]> stream;

	public MigratoryInputStream(BlockingQueue<byte[]> stream) {
		this.stream = stream;
	}

	public void close() {
		this.stream.clear();
	}

	public byte[] readBytes() throws InterruptedException {
		//following will BLOCK until queue not empty
		byte[] returnval = this.stream.take();
		System.out.println("took of blocking queue: " + java.util.Arrays.toString(returnval));
		return returnval;
	}
}
