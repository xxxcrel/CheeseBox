package beer.cheese.hollow.connector.endpoint;

import java.net.InetAddress;
import java.net.Socket;

public abstract class AbstractEndpoint {

    /**
     * Running state of the endpoint.
     */
    protected volatile boolean running = false;


    /**
     * Will be set to true whenever the endpoint is paused.
     */
    protected volatile boolean paused = false;

    /**
     * Track the initialization state of the endpoint.
     */
    protected boolean initialized = false;


    private int port;
    public int getPort() { return port; }
    public void setPort(int port ) { this.port=port; }

    /**
     * Allows the server developer to specify the acceptCount (backlog) that
     * should be used for server sockets. By default, this value
     * is 100.
     */
    private int backlog = 100;
    public void setBacklog(int backlog) { if (backlog > 0) this.backlog = backlog; }
    public int getBacklog() { return backlog; }

    public abstract int getLocalPort();

    /**
     * Address for the server socket.
     */
    private InetAddress address;
    public InetAddress getAddress() { return address; }
    public void setAddress(InetAddress address) { this.address = address; }

    /**
     * Server socket factory.
     */
    protected ServerSocketFactory serverSocketFactory = null;
    public void setServerSocketFactory(ServerSocketFactory factory) { this.serverSocketFactory = factory; }
    public ServerSocketFactory getServerSocketFactory() { return serverSocketFactory; }



    public interface Handler{
        boolean process(Socket socket);
    }
}
