package venturas.app.audio;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.sound.sampled.*;

import venturas.mtcp.io.*;
import venturas.mtcp.sockets.*;

public class MigAudioClient {

    private MigratoryInputStream ois;
    private MigratoryOutputStream os;
    private MSock socket;

    public static void main(String[] args) throws Exception {
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
        MigratoryInputStream in = socket.getInputStream();
        play(in);


        System.out.println("Client: end");
    }

    private static synchronized void play(final MigratoryInputStream in) throws Exception {
        //final AudioFormat format = new AudioFormat("PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, little-endian");
        final AudioFormat format = new AudioFormat(44100.0f, 16, 2, true, false);
        System.out.println(format.toString());
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine line = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
        line.open(format);
        line.start();
        byte tempBuffer[] = new byte[1024];

        while (true) {
            tempBuffer = in.readBytes();
            System.out.println(Arrays.toString(tempBuffer));
            line.write(tempBuffer, 0, tempBuffer.length);

        }
    }
}
