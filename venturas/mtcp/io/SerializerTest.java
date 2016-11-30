package venturas.mtcp.io;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class SerializerTest {

  public static void main(String[] args) throws Exception {
    String testString = "Who is the cuntiest of cunts?";
    LinkedList<String> strings = new LinkedList<String>();
    strings.add("jarred");
    strings.add("is");
    strings.add("a");
    strings.add("massive");
    strings.add("cunt");

    BlockingQueue<byte[]> bos = new LinkedBlockingQueue<byte[]>();

    MigratoryOutputStream qbos = new MigratoryOutputStream(bos);
    MigratoryInputStream qbis = new MigratoryInputStream(bos);

    MigratoryObjectInputStream ois = new MigratoryObjectInputStream(qbis);
    MigratoryObjectOutputStream oos = new MigratoryObjectOutputStream(qbos);
    oos.writeObject(strings);
    System.out.println("Wrote object to stream: " + strings.toString());
    LinkedList<String> list = (LinkedList<String>) ois.readObject();
    System.out.println("Recieved object from stream: " + list.toString());
  }
}
