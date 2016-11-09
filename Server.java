import java.net.*;
import java.io.*;

public class Server {
  private MServerSocket serverSocket;

  public Server(int portA, int portB) throws IOException, ClassNotFoundException {
    serverSocket = new MServerSocket(portA, portB);
  }

  public static void main(String args[]) throws IOException, ClassNotFoundException {

    System.err.println("--- Running server --");
    Server s = new Server(9030, 10030);
  }
}
