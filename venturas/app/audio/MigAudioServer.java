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

    public void run(String args[])
	throws FileNotFoundException, IOException, UnsupportedAudioFileException, ClassNotFoundException, MTCPHandshakeException, MTCPMigrationException, InterruptedException, MTCPStateException {
        if (args.length == 0) {
            throw new IllegalArgumentException("expected sound file arg");
        }

        AudioFormat format = AudioUtils.getAudioFormat(args[0]);

        try (RandomAccessFile in = AudioUtils.getRandomAccessFile(args[0])) {

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
            client.accept(); //accept both a client and a server (for if we require migration) THIS DOES NOT BLOCK
            while (!client.hasClient()) {
                //block; //because accept is non blocking on MServerSock
				Thread.sleep(5);
            }
			System.out.println("Connected to client");

            this.os = client.getOutputStream();
            this.is = client.getInputStream();
            this.oos = new MigratoryObjectOutputStream(this.os);
            this.ois = new MigratoryObjectInputStream(this.is);

			State<Integer> state = client.importState();
			Integer streamOffset = 0;
			System.err.println("re constructed state----------");
			if (state.isEmpty()) {
				streamOffset = 0;
            	String msg = (String) ois.readObject();
				if (!msg.equals("Start")) {
	                throw new IOException("Did not receive Start message from client, instead got: " + msg);
	            }
			} else {
				System.err.println("Fuck lads, it ain't empty. Set the offset then you cunt");
				streamOffset = state.getSnapshot() + SerializationUtils.arrayLength * state.getBufferOut().size();
			}

            String formatString = format.toString();
            oos.writeObject(formatString);

            System.out.println("About to ");
            byte bytesInFromFile[] = new byte[1024];
            int count = 0;

			in.seek(streamOffset + 1000000);

            while (count != - 1) {
                count = in.read(bytesInFromFile);
				while ()
                byte[] bytesOutToClient = new byte[1024];
                System.arraycopy(bytesInFromFile, 0, bytesOutToClient, 0, 1024);
                try {
					this.os.writeBytes(bytesOutToClient);
				} catch (MTCPStreamMigratedException e) {
					state = serverSocket.importState();
				}
            }

            while (!this.stream) {
                    //block
            }
        }



		/*
		while (true) {
			try {
				while (!serverSocket.hasClient()) {
	            	log("waiting on client");
					Thread.sleep(500);
	        	}
				State<Integer> state = serverSocket.importState();
				System.err.println("re constructed state----------");

				Integer reconstructedState = null;
				if (!state.isEmpty()) {
					reconstructedState = state.getSnapshot();
					Iterator<byte[]> it = state.getBufferIn().iterator();
					while (it.hasNext()) {
						byte[] next = it.next();
						for (int i = 0; i < next.length; i++) {
							reconstructedState += (int)next[i];
						}
					}
					sum = reconstructedState;
				} else {
					sum = 0;
				}
				System.err.println(sum);
				System.err.println("----------");

				log("Got past accept call (remember, is non blocking)");
				MigratoryOutputStream qos = serverSocket.getOutputStream();
				MigratoryInputStream qis = serverSocket.getInputStream();

				log("Entering while...");

				while (true) {
					log("Okay, I'm gonna read something");

					byte[] b = qis.readBytes();
					log("Got " + b[0]);
					sum += (int)b[0];
					Thread.sleep(500);
					if (b[0] % 3 == 0) {
						serverSocket.exportState(new State<Integer>(sum));
					}
				}
			} catch (MTCPStreamMigratedException e) {
				System.err.println("(((SEERVER)))STREAM MIGRATED EXCEPTION!!!!!!!");
			}
		}
		*/










        System.out.println("server: shutdown");
    }
}

    /*public static void toBytes(byte[] buffer, Byte[] obuffer) {
        for (int i = 0; i < buffer.length; i++) {
            obuffer[i] = buffer[i];
        }
    }*/
