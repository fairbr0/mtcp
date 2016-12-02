package venturas.mtcp.io;

public class MigratoryObjectOutputStream {

  MigratoryOutputStream os;
  private static int arrayLength = SerializationUtils.arrayLength;

  public MigratoryObjectOutputStream(MigratoryOutputStream os) throws Exception {
      this.os = os;
  }

  public void writeObject(Object o) throws Exception {
      System.err.println(o.toString());
    byte[] b = SerializationUtils.toByteArray(o);

	System.err.println("BYTEARR.LENGTH:" + b.length);

    double exactSize = (double)((b.length +0.0) / arrayLength);
    int size = (int)Math.ceil(exactSize);
    int padding = b.length % arrayLength;

    ByteObject values = new ByteObject();

    os.writeBytes(values.returnArray(padding, size, this.arrayLength));

    for (int i = 0; i < size - 1; i++) {
        byte[] buffer = new byte[arrayLength];
        System.arraycopy(b, i * arrayLength, buffer, 0, arrayLength);
        os.writeBytes(buffer);
    }
    byte[] buffer = new byte[arrayLength];
    System.arraycopy(b, (size-1) * arrayLength, buffer, 0, padding);
    os.writeBytes(buffer);

  }
}
