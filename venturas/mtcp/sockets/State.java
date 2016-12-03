package venturas.mtcp.sockets;
import java.util.LinkedList;
import java.io.Serializable;

public class State<T> implements Serializable {
    private T snapshot;
    private LinkedList<byte[]> bufferIn;
    private LinkedList<byte[]> bufferOut;

    public State(T snapshot) {
        this.snapshot = snapshot;
        this.bufferIn = new LinkedList<byte[]>();
        this.bufferOut = new LinkedList<byte[]>();
    }

    public void setSnapshot(T snapshot) {
        this.snapshot = snapshot;
    }

    public T getSnapshot() {
        return this.snapshot;
    }

    public LinkedList<byte[]> getBufferIn() {
        return this.bufferIn;
    }

    public LinkedList<byte[]> getBufferOut() {
        return this.bufferOut;
    }

	public boolean isEmpty() {
		return (snapshot == null) && bufferIn.isEmpty() && bufferOut.isEmpty();
	}

    public void clearBuffers() {
        this.bufferIn = new LinkedList<byte[]>();
        this.bufferOut = new LinkedList<byte[]>();
    }

    public void addToBufferIn(byte[] buffer) {
        this.bufferIn.add(buffer);
    }

    public void addToBufferOut(byte[] buffer) {
        this.bufferOut.add(buffer);
    }

	public String toString() {
		return "SNAPSHOT(" + snapshot + "):BUFFERIN:(" + bufferIn.toString() +"):BUFFEROUT:(" + bufferOut.toString() + ")";
	}
}
