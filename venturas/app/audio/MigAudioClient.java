import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class MigAudioClient {

    private QueuedObjectInputStream is;
    private QueuedObjectOutputStream os;
    private Socket socket;

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            // play a file passed via the command line
            File soundFile = AudioUtil.getSoundFile(args[0]);
            System.out.println("Client: " + soundFile);
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(soundFile))) {
                play(in);
              }
            }
        else {
            // play soundfile from server
            System.out.println("Client: reading from 127.0.0.1:6666");
            try (ShellSocket socket = new Socket("127.0.0.1", 9030)) {
                QueuedObjectInputStream in = new QueuedObjectInputStream(socket.getInputStream());
                play(in);
            }
        }
        System.out.println("Client: end");
    }



    private static synchronized void play(final QueuedObjectInputStream in) throws Exception {
        final String format = "PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, little-endian";
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine line = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
        line.open(format);
        line.start();
        byte tempBuffer[] = new byte[2048];

        while ((tempBuffer = in.readObject())) {
            if (cnt > 0) {
                line.write(tempBuffer, 0, cnt);
            }
        }
        line.drain();
        line.close();
    }
}
