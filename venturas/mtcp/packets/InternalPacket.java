package venturas.mtcp.packets;

import java.io.Serializable;

public class InternalPacket<T> implements Serializable {

	private Flag[] flags;
    private T payload;

	public InternalPacket(Flag[] flags) {
		this.flags = flags;
		this.payload = null;
	}

	public InternalPacket(Flag[] flags, T payload) {
		this.flags = flags;
		this.payload = payload;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}

	public Flag[] getFlags() {
		return this.flags;
	}

	public Flag getFlag(int i) {
		return this.flags[i];
	}

	public T getPayload() {
		return this.payload;
	}
}
