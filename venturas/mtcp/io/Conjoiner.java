package venturas.mtcp.io;

public class Conjoiner {

  public QueuedByteArrayInputStream bytes;
  private static int arrayLength = 2048;

  public Conjoiner(QueuedByteArrayInputStream bytes) throws Exception {
    this.bytes = bytes;
  }

  public Object readObject() throws Exception{
    ByteObject a = new ByteObject();
    a.setValues(bytes.readBytes());

    //total length to read in bytes
    int length = ((a.length - 1) * this.arrayLength) + a.paddingSize; //+ a.paddingSize;
    System.out.println("array length = " + length);
    byte[] returnArray = new byte[length];
    System.out.println("length = " + a.length);
    System.out.println("paddingsize = " + a.paddingSize);

    for (int i = 1; i < a.length; i++) {
        //arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        System.arraycopy(bytes.readBytes(), 0, returnArray, (i-1) * this.arrayLength, this.arrayLength);
    }
    System.out.println("B length = " + a.length);
    System.out.println(this.arrayLength - a.paddingSize);
    System.arraycopy(bytes.readBytes(), 0, returnArray, (a.length-1) * this.arrayLength, a.paddingSize);
    System.out.println("Message recieved is" + java.util.Arrays.toString(returnArray));

    // byte[] resultNew = new byte[this.arrayLength*(a.length-1)];
    // //arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
    // System.arraycopy(returnArray, this.arrayLength, resultNew, 0, ((a.length-1)*this.arrayLength));

    //System.out.print(java.util.Arrays.toString(resultNew));
    return SerializationUtils.fromByteArray(returnArray);
  }
}
