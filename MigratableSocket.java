import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class MigratableSocket {

	Socket active;
	List<AddressPortPair> serverList;
	int inUse;
	ObjectInputStream is;
	ObjectOutputStream os;

	public MigratableSocket(InetSocketAddress socketAddress) {
		try {
			System.err.println("<msock> creating socket");
			this.active = new Socket(socketAddress.getAddress(), socketAddress.getPort());
			System.err.println("<msock> have created a socket");
			this.os = new ObjectOutputStream(active.getOutputStream());
			System.err.println("<msock> have created out stream");
			this.is = new ObjectInputStream(active.getInputStream());
			System.err.println("<msock> have created in stream");

			initialHandshake();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException d) {
			d.printStackTrace();
		}
	}


	///all below methods are private to the class and the client should not be using them.

	private void initialHandshake() throws IOException, ClassNotFoundException {

		System.err.println("<msock> Started handshake");

		boolean handshakeException = false;
		Flag[] args = new Flag[2];
		args[0] = Flag.SYN;
		args[1] = Flag.ONE; //legacy

		//send first SYN to the server.
		this.active.setSoTimeout(5000);
		System.err.println("<msock> Sending SYN");
		this.os.writeObject(new Packet<String>(args));
		this.os.flush();

		//wait for response of SYN plus ACK, Server list
		System.err.println("<msock> Waiting for read");
		Packet<ArrayList<AddressPortPair>> response = (Packet<ArrayList<AddressPortPair>>) this.is.readObject();
		System.err.println("<msock> Read input");
		if (response.getArgs().length == 2) {
			if (response.getArgs()[0].equals(Flag.SYN) && response.getArgs()[1].equals(Flag.ACK)) {
				System.err.println("<msock> Got SYN ACK, getting payload");
				this.serverList = response.getPayload();
			} else {
				System.err.println("<msock> Not SYN ACK");
				handshakeException = true;
			}
		} else {
			System.err.println("<msock> Not length 2");
			handshakeException = true;
		}

		if (handshakeException) {
			throw new IOException();
		}

		args[0] = Flag.BOB;
		args[1] = Flag.TWO; //legacy
		System.err.println("<msock> Writing " + args[0]);
		this.os.flush();
		this.os.writeObject(new Packet<String>(args));
		this.os.flush();
		this.active.setSoTimeout(0);
		return;
	}

	public void getInStream() {

	}

	public void getOutStream() {

	}

	private void migrate() {

	}
}
