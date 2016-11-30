package venturas.test.mtcp.sockets;


import venturas.mtcp.io.*;
import venturas.mtcp.sockets.*;
import java.util.LinkedList;
import java.net.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SerializedShellServerSocketTest {

    // hangs
    @Test
    public void GetMappingTest() throws Exception
    {
        SerializedShellServerSocket socket = new SerializedShellServerSocket(9030);
        SerializedShellSocket s = new SerializedShellSocket((new InetSocketAddress("localhost", 9030)).getAddress(), 9030);
        LinkedList<AddressPortTuple> addresses = new LinkedList<AddressPortTuple>();

        AddressPortTuple tuple1 = new AddressPortTuple("localhost", 9030, 10030);
        addresses.add(tuple1);
        AddressPortTuple tuple2 = new AddressPortTuple("localhost", 9031, 10031);
        addresses.add(tuple2);

        socket.setOtherServers(addresses);

        InetAddress address = (new InetSocketAddress("localhost", 9031)).getAddress();
        Socket newSocket = socket.getSocketFromMapping(address, 9031);
        System.out.println(newSocket.getPort());
        assertEquals(10030, newSocket.getPort());
    }
}
