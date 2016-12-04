package venturas.app.audio;

import java.io.*;
import java.net.*;

import java.util.*;
import venturas.mtcp.io.*;
import venturas.mtcp.sockets.*;

import javax.sound.sampled.*;

public class MigAudioServer {
    MServerSock client;
    MigratoryInputStream is;
    MigratoryOutputStream os;
    MigratoryObjectOutputStream oos;
    MigratoryObjectInputStream ois;
    private boolean stream = false;

    public static void main(String[] args)
	throws FileNotFoundException, IOException, UnsupportedAudioFileException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException, InterruptedException, MTCPStateException {
        MigAudioServer server = new MigAudioServer();
        server.run(args);
    }

    public void run(String args[]) throws FileNotFoundException, IOException, UnsupportedAudioFileException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException, InterruptedException, MTCPStateException {
        if (args.length != 3) {
            System.err.println("Error: Expected four arguments (and the order as follows)");
			System.err.println("(i) PATH to the song (.WAV format)");
			System.err.println("(ii) THIS_SERVER'S_ADDRESS in the form: 'publicAddress:publicPort:privateAddress:privateport'");
			System.err.println("(iii) SEVER_POOL_ADDRESS_LIST, must include THIS_SERVER'S_ADDRESS. Formatted as comma seperated strings 'publicAddress:publicPort:privateAddress:privateport'");
			System.exit(1);
        }

        AudioFormat format = AudioUtils.getAudioFormat(args[0]);

        // try (RandomAccessFile fileStream = AudioUtils.getRandomAccessFile(args[0])) {

		/* Split the arguments strings and create MServerSock*/
        String[] me = args[1].split(":");
		String[] all = args[2].split(",");
		List<AddressMapping> otherServers = new LinkedList<>();
		for (String server : all) {
			String[] addrPort = server.split(":");
			AddressMapping apt = new AddressMapping(addrPort[0], Integer.parseInt(addrPort[1]), addrPort[2], Integer.parseInt(addrPort[3]));
			otherServers.add(apt);
		}
        MServerSock client = new MServerSock(Integer.parseInt(me[1]), Integer.parseInt(me[3]), otherServers);
        client.accept(); //accept both a client and a server (for later potential migration)
		//ABOVE DOES NOT BLOCK

		while (!client.hasClient()) {
			//block
			Thread.sleep(500);
			System.out.println("Waiting for client");
		}

		this.os = client.getOutputStream();
        this.is = client.getInputStream();
        this.oos = new MigratoryObjectOutputStream(this.os);
        this.ois = new MigratoryObjectInputStream(this.is);

		int count = 0;
		while (count != - 1) {
			System.err.println("ITERATION");
			while (!client.hasClient()) {
				//block
				Thread.sleep(500);
				System.out.println("Waiting for client");
			}

			// SERVER HAS JUST STARTED/RESTARTED
			State<Long> state = client.importState();
			long offset = 0;
			if (state.getSnapshot() == null) {
				System.err.println("Null snapshot, that suggests this is the start of stream!");
				// This is the first server to have ever been run, we are NOT picking up from a migration
				offset = 0;
				// (i) Check the client requested a Start
            	String msg = (String) ois.readObject();
				if (!msg.equals("Start")) {
	                throw new IOException("Did not receive Start message from client, instead got: " + msg);
	            }
				// (ii) Tell the client the audio format
	            oos.writeObject(format.toString());
			} else {
				// The client has migrated to us, use state snapshot plus buffers to gracefully pick up the service
				System.err.println("Fuck lads, it ain't empty. Set the offset then you cunt");
				System.err.println(state.toString());
				offset = state.getSnapshot() + SerializationUtils.arrayLength * state.getBufferOut().size();
			}

			// READ IN THE FILE AND SEEK (this is done in O(1) time) TO THE CORRECT PART OF THE FILE
			RandomAccessFile fileStream = AudioUtils.getRandomAccessFile(args[0]);
			fileStream.seek(offset);

			int forceExportState = 0;
			int forceMigration = 0;
			while (client.hasClient() && count != -1) { //TODO what if count becomes -1 ??????
				//DO THE WRITE
				byte[] bytesInFromFile = new byte[1024];
				count = fileStream.read(bytesInFromFile);
				byte[] bytesOutToClient = new byte[1024];
                System.arraycopy(bytesInFromFile, 0, bytesOutToClient, 0, 1024);
				try {
					this.os.writeBytes(bytesOutToClient);
					Thread.sleep(4);
				} catch (MTCPStreamMigratedException e) {
					//DON'T ACTUALLY DO ANYTHING WITH IT!!!
					e.printStackTrace();
					System.err.println("Gonna continue despite exception...");
				}
				forceExportState += 1;
				offset += SerializationUtils.arrayLength;
				if (forceExportState == 250) {
					//We export state after every 250 bytes
					forceExportState = 0;
					client.exportState(new State<Long>(offset));
				}
				forceMigration += 1;
				if (forceMigration == 1000) {
					forceMigration = 0;
					System.err.println("OOOOOHHHH FOOOOK ME, WE'RE GONNA SLEEP!");
					Thread.sleep(200);
				}
			}
		}
		System.err.println("Stream completed! You may now Ctrl-C and run ./kill.sh");
		while (!this.stream) {
			//block
		}
		System.out.println("server: shutdown");
    }
}
