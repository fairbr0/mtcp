package venturas.mtcp.sockets;

import java.net.InetAddress;

public class AddressPortPair {
	public InetAddress address;
	public int port;

	public AddressPortPair(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}
}
