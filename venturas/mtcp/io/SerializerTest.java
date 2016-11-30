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

    QueuedByteArrayOutputStream qbos = new QueuedByteArrayOutputStream(bos);
    QueuedByteArrayInputStream qbis = new QueuedByteArrayInputStream(bos);

    Conjoiner conj = new Conjoiner(qbis);
    Dicer dice = new Dicer(qbos);
    dice.writeObject(strings);
    LinkedList<String> list = (LinkedList<String>) conj.readObject();
    System.out.println(list.toString());
  }
}
