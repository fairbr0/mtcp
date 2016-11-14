import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class MigratableServerSocket {
	// check that only 1 conn exists

	Socket client;
	//Socket otherServer;
	ServerSocket clientListener;
	ServerSocket serverListener;
	ObjectInputStream is;
	ObjectOutputStream os;
	List<AddressPortPair> otherServers;

	public MigratableServerSocket(int portA, int portB) throws IOException, ClassNotFoundException {
		System.err.println("<mserversock> Instantiating server sockets");
		this.serverListener = new ServerSocket(portB);
		this.clientListener = new ServerSocket(portA);
		System.err.println("<mserversock> Done server sockets");
		acceptClient();
	}

	public void acceptClient() throws IOException {
		Thread acceptClient = new Thread(() -> {
			try {
				System.err.println("<mserversock> clientListener.accept about to be called");
						this.client = clientListener.accept();
				System.err.println("<mserversock> accepted client");
						this.os = new ObjectOutputStream(client.getOutputStream());
				System.err.println("<mserversock> created out stream");
						this.is = new ObjectInputStream(client.getInputStream());
				System.err.println("<mserversock> created in stream");
				initialClientHandshake();
				listenClient();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		});
		acceptClient.start();
	}

	private void listenClient() {
		try {
			do {
				Packet packet = (Packet) is.readObject();
				System.out.println("got message");
				handleClientRequest(packet);
			} while (true);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void handleClientRequest(Packet packet) {

	}

	private void initialClientHandshake() throws IOException, ClassNotFoundException {
		System.err.println("<mserversock> Performing init client handshake");
		boolean handshakeException = false;

		this.client.setSoTimeout(5000);
		System.err.println("<mserversock> Reading response now");
		Packet<String> response = (Packet<String>) is.readObject();
		System.err.println("<mserversock> Got response now");
		if (response.getArgs().length == 2) {
			if (!response.getArgs()[0].equals(Flag.SYN)) {
				System.err.println("<mserversock> Got not SYN");
				handshakeException = true;
			} else {
				System.err.println(response.getArgs()[0]);
			}
		} else  {
			System.err.println("<mserversock> Args not length 1");
			handshakeException = true;
		}

		if (handshakeException) { throw new IOException(); }

		Flag[] args = new Flag[2];
		args[0] = Flag.SYN;
		args[1] = Flag.ACK;
		System.err.println("<mserversock> Writing SYN ACK");
		this.os.writeObject(new Packet<List<AddressPortPair>>(args, this.otherServers));
		this.os.flush();
		System.err.println("<mserversock> Write done, reading object now");
		Packet<String> respb = (Packet<String>) is.readObject();
		Packet<String> resp = (Packet<String>) is.readObject();
		if (resp.getArgs().length == 2) {
			if (!resp.getArgs()[0].equals(Flag.ACK)) {
				System.err.println("<mserversock> Read message, not ACK, got: " + resp.getArgs()[0] + "," + resp.getArgs()[1]);
				handshakeException = true;
			}
		} else {
			System.err.println("<mserversock> Read message, not length 1");
			handshakeException = true;
		}

		if (handshakeException) { throw new IOException(); }
		this.client.setSoTimeout(0);
		return;
	}

	/**
	 * TODO
	 * Replace Object with related State Object
	 **/
	public void exportState(Object state) {

	}

	/**
	 * TODO
	 * Replace Object with related State Object
	 **/
	public Object importState() {
		return null;
	}
}
