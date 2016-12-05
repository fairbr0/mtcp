package venturas.mtcp.sockets;
import java.util.LinkedList;
import java.io.Serializable;

/* State is a Snapshot plus the latest stuff that has been buffered both in and out by the app */
public class State<T> implements Serializable {
    private T snapshot;
    private LinkedList<byte[]> bufferIn;
    private LinkedList<byte[]> bufferOut;

    // If creating state from a snapshot, empty buffers
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

    // Grab everything that has come in since the snapshot
    public LinkedList<byte[]> getBufferIn() {
        return this.bufferIn;
    }

    // Grab everything that went out since the snapshot
    public LinkedList<byte[]> getBufferOut() {
        return this.bufferOut;
    }


    // Clear the buffers if needed
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

    //Print state for debugs
	public String toString() {
		return "SNAPSHOT(" + snapshot + "):BUFFERIN:(" + bufferIn.toString() +"):BUFFEROUT:(" + bufferOut.toString() + ")";
	}
}
