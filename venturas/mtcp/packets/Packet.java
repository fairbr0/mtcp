package venturas.mtcp.packets;

import java.io.Serializable;

public class Packet implements Serializable {

	private Flag[] flags;
	private byte[] payload;

	public Packet(Flag[] flags) {
		this.flags = flags;
		this.payload = null;
	}

	public Packet(Flag[] flags, byte[] payload) {
		this.flags = flags;
		this.payload = payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	public Flag[] getFlags() {
		return this.flags;
	}

	public Flag getFlag(int i) {
		return this.flags[i];
	}

	public byte[] getPayload() {
		return this.payload;
	}
}
