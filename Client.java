import java.net.*;

public class Client {
  private MSock socket;

  public Client(InetSocketAddress socketAddress) {
    socket = new MSock(socketAddress);
  }

  public static void main(String[] args) {
    System.err.println("---Running client---");
    Client c = new Client(new InetSocketAddress("localhost", 9030));
  }
}
