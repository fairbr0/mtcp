package venturas.mtcp.sockets;
import java.net.*;
public class AddressPortTuple {
	private final InetAddress address;
	private final int[] ports;

	public AddressPortTuple(InetAddress address, int portA, int portB) {
		this.address = address;
		ports = new int[2];
		ports[0] = portA;
		ports[1] = portB;
	}

	public AddressPortTuple(InetAddress address, int portA) {
		this.address = address;
		ports = new int[1];
		ports[0] = portA;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int[] getPorts() {
		return ports;
	}
}
