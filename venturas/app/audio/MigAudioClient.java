package venturas.app.audio;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.sound.sampled.*;
import java.util.concurrent.*;

import venturas.mtcp.io.*;
import venturas.mtcp.sockets.*;

public class MigAudioClient {

    private MigratoryInputStream is;
    private MigratoryOutputStream os;
    private MigratoryObjectOutputStream oos;
    private MigratoryObjectInputStream ois;

    private MSock socket;
    private BlockingQueue<byte[]> byteBuffer;

    public static void main(String[] args) throws Exception {
        MigAudioClient client = new MigAudioClient();
		if (args.length != 1) {
            System.err.println("Error: Expected one argument");
			System.err.println("A colon separated pair of the address and port of the server to connected to");
			System.err.println("e.g. 'publicAddress:publicPort'");
			System.exit(1);
        }
		args = args[0].split(":");
        client.run(args[0], Integer.parseInt(args[1]));
    }

    public void run(String address, int port) throws Exception {
        this.socket = new MSock((new InetSocketAddress(address, port)).getAddress(), port);
        this.os = socket.getOutputStream();
        this.is = socket.getInputStream();
        this.oos = new MigratoryObjectOutputStream(this.os);
        this.ois = new MigratoryObjectInputStream(this.is);

        play();

        System.out.println("Client: end");
    }

    private synchronized void play() throws Exception {
        //final AudioFormat format = new AudioFormat("PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, little-endian");
        String msg = "Start";
        this.oos.writeObject(msg);
        System.out.println("Requesting server to start the stream");

        final AudioFormat format = new AudioFormat(44100.0f, 16, 2, true, false);
        System.out.println(format.toString());
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine line = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
        line.open(format);
        line.start();
        byte tempBuffer[] = new byte[1024];
        this.byteBuffer = new LinkedBlockingQueue<byte[]>();
        streamBytes();

        System.out.println("Waiting for buffer to fill");
        while (this.byteBuffer.size() < 177) {
			Thread.sleep(0);
            //block
        }
        System.out.println("Beginning to play");
		listenForBradburyForcedTimeout();
		while (true) {
			while (this.byteBuffer.size() > 0) {
            	line.write(this.byteBuffer.take(), 0, 1024);
        	}
			while (this.byteBuffer.size() < 177) {
				Thread.sleep(0);
	            //block
	        }
		}
    }

    private void streamBytes() throws Exception {
        (new Thread(() -> {
            try {
                //send message to start stream
                while (true) {
                    byte[] tempBuffer = is.readBytes();
                    this.byteBuffer.put(tempBuffer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }

	private void listenForBradburyForcedTimeout() {
		Scanner s = new Scanner(System.in);
		(new Thread(() -> {
			while (true) {
				String m = s.nextLine();
                if (!m.equals("M")) {
                    continue;
                }
				System.err.println("WILL FORCE A TIMEOUT!");
				socket.forceReadTimeout();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("ENTER thread open again");
			}
		})).start();
	}
}
