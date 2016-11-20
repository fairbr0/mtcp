package venturas.mtcp.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueuedObjectInputStream<T> {
	private BlockingQueue<T> stream;

	public QueuedObjectInputStream(BlockingQueue<T> stream) {
		this.stream = stream;
	}

	public void close() {
		this.stream.clear();
	}

	public T readObject() throws InterruptedException {
		//following will BLOCK until queue not empty
		return this.stream.take();
	}
}
