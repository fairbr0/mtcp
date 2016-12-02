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
        client.run(args);
    }

    public void run(String args[]) throws Exception {
        /*if (args.length > 0) {
            // play a file passed via the command line
            File soundFile = AudioUtil.getSoundFile(args[0]);
            System.out.println("Client: " + soundFile);
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(soundFile))) {
                play(in);
              }
            // play soundfile from server*/
        System.out.println("Client: reading from 127.0.0.1:6666");
        MSock socket = new MSock((new InetSocketAddress("localhost", 9030)).getAddress(), 9030);
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
        while (this.byteBuffer.size() < 100) {
            //block
        }
        System.out.println("Beginning to play");
        while (this.byteBuffer.size() > 0) {
            line.write(this.byteBuffer.take(), 0, 1024);
        }

        return;
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
}
