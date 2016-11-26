package venturas.mtcp.sockets;


/**
 * THIS FILE IS NOT IN USE BUT IS KEPT AS AN INSPIRATION FOR WHAT FEATURES A
 * TEAM IMPLEMENTING A CLIENT/SERVER MTCP SYSTEM WOULD EXPECT FROM THEIR SOCKETS
 */



import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.net.SocketImplFactory;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.OutputStream;

public interface IClientSocket {
	public void bind(SocketAddress bindpoint);
	public void close();
	public void connect(SocketAddress endpoint);
	public void connect(SocketAddress endpoint, int timeout);
	public SocketChannel getChannel();
	public InetAddress getInetAddress();
	public InputStream getInputStream();
	public boolean getKeepAlive();
	public InetAddress getLocalAddress();
	public int getLocalPort();
	public SocketAddress getLocalSocketAddress();
	public boolean getOOBInline();
	public OutputStream getOutputStream();
	public int getPort();
	public int getReceiveBufferSize();
	public SocketAddress getRemoteSocketAddress();
	public boolean getReuseAddress();
	public int getSendBufferSize();
	public int getSoLinger();
	public int getSoTimeout();
	public boolean getTcpNoDelay();
	public int getTrafficClass();
	public boolean isBound();
	public boolean isClosed();
	public boolean isConnected();
	public boolean isInputShutdown();
	public boolean isOutputShutdown();
	public void sendUrgentData(int data);
	public void setKeepAlive(boolean on);
	public void setOOBInline(boolean on);
	public void setPerformancePreferences(int connectionTime, int latency, int bandwidth);
	public void setReceiveBufferSize(int size);
	public void setReuseAddress(boolean on);
	public void setSendBufferSize(int size);
	public void shutdownInput();
	public void shutdownOutput();;
	public String toString();



	// public static void setSocketImplFactory(SocketImplFactory fac);




	public void setSoLinger(boolean on, int linger);
	public void setSoTimeout(int timeout);
	public void setTcpNoDelay(boolean on);
	public void setTrafficClass(int tc);
}
