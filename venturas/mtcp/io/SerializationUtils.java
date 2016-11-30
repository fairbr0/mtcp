package venturas.mtcp.io;

import java.util.*;
import java.net.*;
import java.io.*;

//http://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array

public class SerializationUtils {

    private static int arrayLength = 2048;

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

    public byte[][] dicer(Object o) {
        byte[] b = toByteArray(o);
        System.out.println(java.util.Arrays.toString(b));
        double exactSize = (double)((b.length +0.0) / arrayLength);
        int size = (int)Math.ceil(exactSize);
        int padding = b.length % arrayLength;
        byte[][] result = new byte[size+1][arrayLength];

				ByteObject values = new ByteObject();
				result[0] = values.returnArray(padding, size, this.arrayLength);
				//arraycopy(Object src, int srcPos, Object dest, int destPos, int length)

        for (int i = 0; i < size - 1; i++) {
             System.arraycopy(b, i * arrayLength, result[i + 1], 0, arrayLength);
        }
        System.err.println(((size)*arrayLength) + "," + (size-1) + "," + padding);
        System.arraycopy(b, (size-1) * arrayLength, result[size], 0, padding);

				for(int j = 0; j<result.length; j++) {
					System.out.println(java.util.Arrays.toString(result[j]));
				}

        return result;
    }

    private Object conjoiner(byte[][] b) {
				ByteObject a = new ByteObject();
				a.setValues(b[0]);

        //total length to read in bytes
        int length = ((a.length - 1) * this.arrayLength) + a.paddingSize; //+ a.paddingSize;
				System.out.println("array length = " + length);
        byte[] returnArray = new byte[length];
				System.out.println("length = " + a.length);
				System.out.println("paddingsize = " + a.paddingSize);
        int size = b.length;

				System.err.println("LOL"+java.util.Arrays.toString(b[0]));
        for (int i = 1; i < a.length; i++) {
            //arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
            System.arraycopy(b[i], 0, returnArray, (i-1) * this.arrayLength, this.arrayLength);
        }
				System.out.println("B length = " + a.length);
				System.out.println(this.arrayLength - a.paddingSize);
        System.arraycopy(b[a.length], 0, returnArray, (a.length-1) * this.arrayLength, a.paddingSize);
				System.out.println("Message recieved is" + java.util.Arrays.toString(returnArray));

				// byte[] resultNew = new byte[this.arrayLength*(a.length-1)];
				// //arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
				// System.arraycopy(returnArray, this.arrayLength, resultNew, 0, ((a.length-1)*this.arrayLength));

				//System.out.print(java.util.Arrays.toString(resultNew));
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

    public static  void main(String[] args) {
        String lol = "lololololololswagyolo";
				SerializationUtils thing = new SerializationUtils();
        byte[][] b = thing.dicer(lol);
				System.out.println("Thing diced");
        // for (int i = 0; i < b.byteArrays.length; i++) {
        //     System.err.println(java.util.Arrays.toString(b.byteArrays[i]));
        //     System.err.println();
        // }
        System.err.println(thing.conjoiner(b));
				System.out.println("Thing conjoined");
    }
}

class ByteObject {
		int paddingSize;
		int length;

		public ByteObject () {
			this.paddingSize = 0;
			this.length = 0;
		}

		public void setValues(byte[] array) {

			byte[] paddingSize = new byte[4];
			byte[] length = new byte[4];
			//arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
			System.arraycopy(array, 0, paddingSize, 0, 4);
			System.arraycopy(array, 4, length, 0, 4);

			this.paddingSize = (int) byteToInt(paddingSize);
			System.out.println("Padding = " + this.paddingSize);
			this.length = (int) byteToInt(length);
			System.out.println("Length = " + this.length);
		}

		public byte[] returnArray(int paddingSize, int length, int arrayLength) {
			byte[] returnArray = new byte[arrayLength];
			byte[] paddingArray = intToByteArray(paddingSize);
			byte[] lengthArray = intToByteArray(length);

			System.arraycopy(paddingArray, 0, returnArray, 0, 4);
			System.arraycopy(lengthArray, 0, returnArray, 4, 4);
			System.out.println("Padding Array = " + paddingSize + " " +  java.util.Arrays.toString(paddingArray));
			System.out.println("Length Array = "+ length + " " + java.util.Arrays.toString(lengthArray));
			System.out.println("returned Array = " + java.util.Arrays.toString(returnArray));
			return returnArray;
		}

		public byte[] intToByteArray(int value) {
    	return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)value};
					}

		public long byteToInt(byte[] bytes) {
        int val = 0;
        for (int i = 0; i < bytes.length; i++) {
            val=val<<8;
            val=val|(bytes[i] & 0xFF);
        }
        return val;
    }

}
