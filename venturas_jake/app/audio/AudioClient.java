import java.io.*;
import java.net.*;
import javax.sound.sampled.*;


public class AudioClient {
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
            try (Socket socket = new Socket("127.0.0.1", 6666)) {
                if (socket.isConnected()) {
                    InputStream in = new BufferedInputStream(socket.getInputStream());
                    play(in);
                }
            }
        }

        System.out.println("Client: end");
    }


    private static synchronized void play(final InputStream in) throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(in);
        try (Clip clip = AudioSystem.getClip()) {
            clip.open(ais);
            clip.start();
            Thread.sleep(100); // given clip.drain a chance to start
            clip.drain();
        }
    }
}
