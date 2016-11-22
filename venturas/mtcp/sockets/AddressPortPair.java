package venturas.mtcp.sockets;

import java.net.InetAddress;
import java.io.Serializable;

public class AddressPortPair implements Serializable {
	private InetAddress address;
	private int port;

	public AddressPortPair(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	public InetAddress getAddress() {
		return this.address;
	}

	public int getPort() {
		return this.port;
	}

	public String toString() {
		return address.toString() + "," + port;
	}
}
