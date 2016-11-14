import java.net.*;

public class Client {
  private MigratableSocket socket;

  public Client(InetSocketAddress socketAddress) {
    socket = new MigratableSocket(socketAddress);
  }

  public static void main(String[] args) {
    System.err.println("---Running client---");
    Client c = new Client(new InetSocketAddress("localhost", 9030));
  }
}
