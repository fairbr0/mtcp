package venturas.mtcp.io;

public class Dicer {

  QueuedByteArrayOutputStream os;
  private static int arrayLength = 2048;

  public Dicer(QueuedByteArrayOutputStream os) throws Exception {
      this.os = os;
  }

  public void writeObject(Object o) throws Exception {
    byte[] b = SerializationUtils.toByteArray(o);
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
      os.writeBytes(result[j]);
    }


  }
}
