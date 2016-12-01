package venturas.mtcp.io;

public class MigratoryObjectInputStream {

  public MigratoryInputStream bytes;
  private static int arrayLength = SerializationUtils.arrayLength;

  public MigratoryObjectInputStream(MigratoryInputStream bytes) throws Exception {
    this.bytes = bytes;
  }

  public Object readObject() throws Exception{
    ByteObject a = new ByteObject();
    a.setValues(bytes.readBytes());

    //total length to read in bytes
    int length = ((a.length - 1) * this.arrayLength) + a.paddingSize; //+ a.paddingSize;
    byte[] returnArray = new byte[length];

    for (int i = 1; i < a.length; i++) {
        //arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        System.arraycopy(bytes.readBytes(), 0, returnArray, (i-1) * this.arrayLength, this.arrayLength);
    }

    System.arraycopy(bytes.readBytes(), 0, returnArray, (a.length-1) * this.arrayLength, a.paddingSize);

    return SerializationUtils.fromByteArray(returnArray);
  }
}
