package venturas.mtcp.packets;

import java.io.Serializable;

// Used over the private network to send objects
//I.e. it is not worth serializing EVERYTHING, only the parts of TCP we actually wanna simulate
// SO this is used very rarely to send server pool list for instance. Again, only used internally.
// All client-to-server data is sent over as a PACKET instead
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
