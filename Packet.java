import java.io.Serializable;

public class Packet<T> implements Serializable {

	private Flag[] args;
	private T payload;

	public Packet(Flag[] args) {
		this.args = args;
		this.payload = null;
	}

	public Packet(Flag[] args, T payload) {
		this.args = args;
		this.payload = payload;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}

	public Flag[] getArgs() {
		return this.args;
	}

	public T getPayload() {
		return this.payload;
	}
}
