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

    public static void main(String[] args) throws Exception {
        MigAudioServer server = new MigAudioServer();
        server.run(args);
    }

    public void run(String args[]) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("expected sound file arg");
        }

        AudioFormat format = AudioUtil.getAudioFormat(args[0]);

        try (RandomAccessFile in = AudioUtil.getRandomAccessFile(args[0])) {

            String[] me = args[1].split(":");
    		String[] all = args[2].split(",");
    		List<AddressMapping> otherServers = new LinkedList<>();
    		for (String server : all) {
    			String[] addrPort = server.split(":");
    			AddressMapping apt = new AddressMapping(addrPort[0], Integer.parseInt(addrPort[1]), addrPort[2], Integer.parseInt(addrPort[3]));
    			otherServers.add(apt);
    		}

            MServerSock client = new MServerSock(Integer.parseInt(me[1]), Integer.parseInt(me[3]), otherServers);

            client.accept();
            while (!client.hasClient()) {
                //block;
				Thread.sleep(5);
            }
            this.os = client.getOutputStream();
            this.is = client.getInputStream();
            this.oos = new MigratoryObjectOutputStream(this.os);
            this.ois = new MigratoryObjectInputStream(this.is);

            System.out.println("Connected to client");
            String msg = (String) ois.readObject();
            if (!msg.equals("Start")) {
                System.out.println(msg);
                throw new IOException();
            }

            String formatString = format.toString();
            oos.writeObject(formatString);
            System.out.println("About to ");
            byte bufferin[] = new byte[1024];
            //Byte buffer = new Byte[2048];
            int count = 0;


			in.seek(10000000);

            while (count != - 1) {
                count = in.read(bufferin);
                //toBytes(bufferin, buffer);
                byte[] bufferOut = new byte[1024];
                System.arraycopy(bufferin, 0, bufferOut, 0, 1024);

                this.os.writeBytes(bufferOut);
                // try {
                // 	Thread.sleep(5);
     		// 	} catch (InterruptedException e) {
        		// 	e.printStackTrace();
    	        // }
            }

            while (!this.stream) {
                    //block
            }
        }
        System.out.println("server: shutdown");
    }
}

    /*public static void toBytes(byte[] buffer, Byte[] obuffer) {
        for (int i = 0; i < buffer.length; i++) {
            obuffer[i] = buffer[i];
        }
    }*/
