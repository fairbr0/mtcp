import java.io.*;
import java.net.*;
import venturas.mtcp.*;

public class AudioServer {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("expected sound file arg");
        }
        File soundFile = AudioUtil.getSoundFile(args[0]);

        System.out.println("server: " + soundFile);

        try (FileInputStream in = new FileInputStream(soundFile)) {
            ShellServerSocket client = new ShellServerSocket(9030,1030);
            client.accept();
            QueuedObjectOutputStream out = client.getOutputStream();

            byte bufferin[] = new byte[2048];
            //Byte buffer = new Byte[2048];
            int count;
            while ((count = in.read(bufferin)) != -1) {
                //toBytes(bufferin, buffer);
                out.writeObject(bufferin);
                try {
                    Thread.sleep(10);
      			    System.out.println("Doing something");
      			} catch (InterruptedException e) {
        			e.printStackTrace();
        		}
        	}
        }
        System.out.println("server: shutdown");
    }

    /*public static void toBytes(byte[] buffer, Byte[] obuffer) {
        for (int i = 0; i < buffer.length; i++) {
            obuffer[i] = buffer[i];
        }
    }*/
}
