package venturas.app.audio;

import java.io.*;
import java.net.*;

import java.util.*;
import venturas.mtcp.io.*;
import venturas.mtcp.sockets.*;

public class MigAudioServer {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("expected sound file arg");
        }
        File soundFile = AudioUtil.getSoundFile(args[0]);

        System.out.println("server: " + soundFile);

        try (FileInputStream in = new FileInputStream(soundFile)) {
            MServerSock client = new MServerSock(9030, 10030, null);
            client.accept();
            while (!client.hasClient()) {
                //block;
                Thread.sleep(5);
            }
            System.out.println("about to stream");
            MigratoryOutputStream out = client.getOutputStream();

            byte bufferin[] = new byte[1024];
            //Byte buffer = new Byte[2048];
            int count;
            while ((count = in.read(bufferin)) != -1) {
                //toBytes(bufferin, buffer);
                byte[] bufferOut = new byte[1024];
                System.arraycopy(bufferin, 0, bufferOut, 0, 1024);

                out.writeBytes(bufferOut);
                System.out.println(Arrays.toString(bufferOut));
                try {
                    Thread.sleep(0);
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
