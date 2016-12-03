package venturas.mtcp.io;

import java.util.*;
import java.net.*;
import java.io.*;

//http://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array

public class SerializationUtils {

    public static final int arrayLength = 1024;

    public static byte[] toByteArray(Object o) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            byte[] b = bos.toByteArray();
            return b;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                System.err.println("SERIALIZER IOEXCEPTION, StackOverflow says ignore it");
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Object fromByteArray(byte[] b) {
        ByteArrayInputStream bis = new ByteArrayInputStream(b);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                System.err.println("SERIALIZER IOEXCEPTION, StackOverflow says ignore it");
                e.printStackTrace();
            }
        }
        return null;
    }
}
