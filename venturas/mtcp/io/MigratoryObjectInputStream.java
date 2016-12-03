package venturas.mtcp.io;

import venturas.mtcp.sockets.*;

public class MigratoryObjectInputStream {

  public MigratoryInputStream bytes;
  private static int arrayLength = SerializationUtils.arrayLength;

  public MigratoryObjectInputStream(MigratoryInputStream bytes) {
    this.bytes = bytes;
  }

  public Object readObject() throws InterruptedException {
    ByteObject a = new ByteObject();
	byte[] returnArray = null;
	try {
	    a.setValues(bytes.readBytes());

	    //total length to read in bytes
	    int length = ((a.size() - 1) * this.arrayLength) + a.paddingSize; //+ a.paddingSize;
		returnArray = new byte[length];

	    for (int i = 1; i < a.size(); i++) {
	        //arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
	        System.arraycopy(bytes.readBytes(), 0, returnArray, (i-1) * this.arrayLength, this.arrayLength);
	    }

	    System.arraycopy(bytes.readBytes(), 0, returnArray, (a.size()-1) * this.arrayLength, a.paddingSize);
	} catch (MTCPStreamMigratedException e) {
		e.printStackTrace();
	}

    return SerializationUtils.fromByteArray(returnArray);
  }
}
