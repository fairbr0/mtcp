package venturas.mtcp.sockets;
import java.util.LinkedList;
import java.io.Serializable;

public class State<T> implements Serializable {
    private T state;
    private LinkedList<byte[]> bufferIn;
    private LinkedList<byte[]> bufferOut;

    public State(T state) {
        this.state = state;
        this.bufferIn = new LinkedList<byte[]>();
        this.bufferOut = new LinkedList<byte[]>();
    }

    public void setState(T state) {
        this.state = state;
    }

    public T getState() {
        return this.state;
    }

    public LinkedList<byte[]> getBufferIn() {
        return this.bufferIn;
    }

    public LinkedList<byte[]> getBufferOut() {
        return this.bufferOut;
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
		return "SNAPSHOT(" + state + "):BUFFERIN:(" + bufferIn.toString() +"):BUFFEROUT:(" + bufferOut.toString() + ")";
	}
}
