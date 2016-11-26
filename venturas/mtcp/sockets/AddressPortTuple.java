package venturas.mtcp.sockets;
import java.net.*;
import java.io.*;
public class AddressPortTuple implements Serializable {
	private final InetAddress address;
	private final int[] ports;

	public AddressPortTuple(String address, int portA, int portB) {
		this.address = createInetAddress(address);
		System.err.println("*******" + address + "*******");
		System.err.println("~~~~~~~~~" + this.address + "~~~~~~~~~");
		ports = new int[2];
		ports[0] = portA;
		ports[1] = portB;
	}

	public AddressPortTuple(String address, int portA) {
		this.address = createInetAddress(address);
		System.err.println("*******" + address + "*******");
		System.err.println("~~~~~~~~~" + this.address + "~~~~~~~~~");
		ports = new int[1];
		ports[0] = portA;
	}

	public AddressPortTuple(InetAddress address, int portA) {
		this.address = address;
		System.err.println("*******" + address + "*******");
		ports = new int[1];
		ports[0] = portA;
	}

	public AddressPortTuple(InetAddress address, int portA, int portB) {
		this.address = address;
		System.err.println("*******" + address + "*******");
		ports = new int[2];
		ports[0] = portA;
		ports[1] = portB;
	}

	private static InetAddress createInetAddress(String from) {
		return (new InetSocketAddress(from, 9000)).getAddress();
	}

	public InetAddress getAddress() {
		return address;
	}

	public int[] getPorts() {
		return ports;
	}

	public String toString() {
		if (ports.length == 2) {
			return address + "," + ports[0] + "," + ports[1];
		}
		return address + "," + ports[0];
	}
}
