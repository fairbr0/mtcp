package venturas.mtcp.sockets;
import java.net.*;
import java.io.*;
public class AddressPortTuple implements Serializable {
	private final String address;
	private final int[] ports;

	public AddressPortTuple(String address, int portA, int portB) {
		this.address = address;
		ports = new int[2];
		ports[0] = portA;
		ports[1] = portB;
	}

	public AddressPortTuple(String address, int portA) {
		this.address = address;
		ports = new int[1];
		ports[0] = portA;
	}

	public String getAddress() {
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
