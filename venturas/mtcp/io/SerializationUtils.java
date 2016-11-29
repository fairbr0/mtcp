// package venturas.mtcp.io;

import java.util.*;
import java.net.*;
import java.io.*;

//http://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array

public abstract class SerializationUtils {

    private static int arrayLength = 20;

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

    public static BytedObject dicer(Object o) {
        byte[] b = toByteArray(o);
        double exactSize = (double)((b.length +0.0) / arrayLength);
        int size = (int)Math.ceil(exactSize);
        int padding = b.length % arrayLength;
        byte[][] result = new byte[size][arrayLength];
        for (int i = 0; i < size - 1; i++) {
             System.arraycopy(b, i * arrayLength, result[i], 0, arrayLength);
        }
        System.err.println(((size-1)*arrayLength) + "," + (size-1) + "," + padding);
        System.arraycopy(b, (size-1) * arrayLength, result[size - 1], 0, padding);
        BytedObject bo = new BytedObject(result, padding);
        return bo;
    }

    private static Object conjoiner(BytedObject b) {
        int length = (b.length - 1) * arrayLength + b.paddingSize;
        byte[] returnArray = new byte[length];
        int size = b.length;
        for (int i = 0; i < size - 1; i++) {
            //arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
            System.arraycopy(b.byteArrays[i], 0, returnArray, i * arrayLength, arrayLength);
        }
        System.arraycopy(b.byteArrays[size-1], 0, returnArray, (size-1) * arrayLength, b.paddingSize);
        return fromByteArray(returnArray);
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

    public static void main(String[] args) {
        String lol = "lololololololswagyolo";
        BytedObject b = SerializationUtils.dicer(lol);
        System.err.println("padding(" + b.paddingSize + ")" + ", length(" + b.length + ") with arrays:");
        for (int i = 0; i < b.byteArrays.length; i++) {
            System.err.println(java.util.Arrays.toString(b.byteArrays[i]));
            System.err.println();
        }
        System.err.println(SerializationUtils.conjoiner(b));
    }
}

class BytedObject {
    byte[][] byteArrays;
    int paddingSize;
    int length;

    BytedObject(byte[][] byteArrays, int paddingSize) {
        this.byteArrays = byteArrays;
        this.paddingSize = paddingSize;
        this.length = byteArrays.length;
    }
}
