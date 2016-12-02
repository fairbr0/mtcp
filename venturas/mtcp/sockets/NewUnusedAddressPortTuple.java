package venturas.mtcp.sockets;
import java.net.*;
import java.io.*;
public class NewUnusedNewUnusedAddressPortTuple implements Serializable {
	private final InetAddress publicAddress;
	private final InetAddress privateAddress;
	private final Integer clientPort;
	private final Integer serverPort;

	private NewUnusedAddressPortTuple(InetAddress publicAddress, InetAddress privateAddress, Integer clientPort, Integer serverPort) {
		this.publicAddress = publicAddress;
		this.privateAddress = privateAddress;
		this.clientPort = clientPort;
		this.serverPort = serverPort;
	}

	public NewUnusedAddressPortTuple(String publicAddress, int clientPort, int serverPort) {
		this(createInetAddress(publicAddress), null, clientPort, serverPort);
	}

	public NewUnusedAddressPortTuple(String publicAddress, int clientPort) {
		this(createInetAddress(publicAddress), null, clientPort, null);
	}

	public NewUnusedAddressPortTuple(InetAddress publicAddress, int clientPort) {
		this(publicAddress, null, clientPort, null);
	}

	public NewUnusedAddressPortTuple(InetAddress publicAddress, int clientPort, int serverPort) {
		this(publicAddress, null, clientPort, serverPort);
	}

	private static InetAddress createInetAddress(String from) {
		return (new InetSocketAddress(from, 9000)).getAddress();
	}

	public InetAddress getPublicAddress() {
		return publicAddress;
	}

	public InetAddress getPrivateAddress() {
		throw new UnsupportedOperationException("Not checked about private addresses yet!");
	}

	public Integer getClientPort() {
		return clientPort;
	}

	public Integer getServerPort() {
		return serverPort;
	}

	public String toString() {
		return publicAddress.toString() + ":" + serverPort + "," + "((notyetimplementedprivate)):" + clientPort;
	}
}
