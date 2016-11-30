package venturas.test.mtcp.io;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import venturas.mtcp.io.*;

public class SerializerTest {

  @Test
  public void SendObjectTest() throws Exception {
      String testString = "Who is the cuntiest of cunts?";
      LinkedList<String> strings = new LinkedList<String>();
      strings.add("jarred");
      strings.add("is");
      strings.add("a");
      strings.add("massive");
      strings.add("poo");


      BlockingQueue<byte[]> bos = new LinkedBlockingQueue<byte[]>();

      MigratoryOutputStream qbos = new MigratoryOutputStream(bos);
      MigratoryInputStream qbis = new MigratoryInputStream(bos);

      MigratoryObjectInputStream ois = new MigratoryObjectInputStream(qbis);
      MigratoryObjectOutputStream oos = new MigratoryObjectOutputStream(qbos);
      oos.writeObject(strings);
      LinkedList<String> list = (LinkedList<String>) ois.readObject();
      assertEquals(strings, list);
  }
}
