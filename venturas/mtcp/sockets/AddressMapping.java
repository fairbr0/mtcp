package venturas.mtcp.sockets;
import java.net.*;
import java.io.*;

/* Models public AND private network gracefully, see our paper */
public class AddressMapping implements Serializable {
	private final InetAddress publicAddress;
	private final InetAddress privateAddress;
	private final Integer publicPort;
	private final Integer privatePort;

	public AddressMapping(String publicAddress, Integer publicPort,  String privateAddress, Integer privatePort) {
		this.publicAddress = inetAddressFromString(publicAddress);
		this.privateAddress = inetAddressFromString(privateAddress);
		this.publicPort = publicPort;
		this.privatePort = privatePort;
	}

	public AddressMapping(InetAddress publicAddress, Integer publicPort,  InetAddress privateAddress, Integer privatePort) {
		this.publicAddress = publicAddress;
		this.privateAddress = privateAddress;
		this.publicPort = publicPort;
		this.privatePort = privatePort;
	}

	public AddressMapping getPublicOnlyAddressMapping() {
		return new AddressMapping(publicAddress, publicPort);
	}

	public AddressMapping(InetAddress publicAddress, Integer publicPort) {
		this(publicAddress, publicPort, null, null);
	}


	private static InetAddress inetAddressFromString(String from) {
		if (from == null) {
			return null;
		}
		return (new InetSocketAddress(from, 9000)).getAddress();
	}

	public InetAddress getPublicAddress() {
		return publicAddress;
	}

	public InetAddress getPrivateAddress() {
		return privateAddress;
	}

	public Integer getPublicPort() {
		return publicPort;
	}

	public Integer getPrivatePort() {
		return privatePort;
	}

	public boolean corresponds(InetAddress publicAddress, int publicPort) {
		return this.publicAddress.equals(publicAddress) && (this.publicPort == publicPort);
	}

	public String toString() {
		return publicAddress + ":" + publicPort + "," + privateAddress + ":" + privatePort;
	}
}
