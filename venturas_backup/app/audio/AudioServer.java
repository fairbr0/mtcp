import java.io.*;
import java.net.*;

public class AudioServer {
    public static void main(String[] args) throws IOException {
        if (args.length == 0)
            throw new IllegalArgumentException("expected sound file arg");
        File soundFile = AudioUtil.getSoundFile(args[0]);

        System.out.println("server: " + soundFile);

        try (ServerSocket serverSocker = new ServerSocket(6666);
            FileInputStream in = new FileInputStream(soundFile)) {
            if (serverSocker.isBound()) {
                Socket client = serverSocker.accept();
                OutputStream out = client.getOutputStream();

                byte buffer[] = new byte[2048];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
			 try {
				Thread.sleep(10);
			 System.out.println("Doing something");
			 } catch (InterruptedException e) {
				e.printStackTrace();
			 }
		}
            }
        }

        System.out.println("server: shutdown");
    }
}
