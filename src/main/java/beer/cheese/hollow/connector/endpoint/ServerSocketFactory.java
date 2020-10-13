package beer.cheese.hollow.connector.endpoint;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * This class creates server sockets.  It may be subclassed by other
 * factories, which create particular types of server sockets.  This
 * provides a general framework for the addition of public socket-level
 * functionality.  It is the server side analogue of a socket factory,
 * and similarly provides a way to capture a variety of policies related
 * to the sockets being constructed.
 *
 * <P> Like socket factories, server Socket factory instances have
 * methods used to create sockets. There is also an environment
 * specific default server socket factory; frameworks will often use
 * their own customized factory.
 *
 */
public abstract class ServerSocketFactory implements Cloneable
{
    //
    // NOTE:  JDK 1.1 bug in class GC, this can get collected
    // even though it's always accessible via getDefault().
    //
    private static ServerSocketFactory theFactory;


    /**
     * Creates a server socket factory.
     */
    protected ServerSocketFactory() { /* NOTHING */ }

    /**
     * Returns a copy of the environment's default socket factory.
     *
     * @return the <code>ServerSocketFactory</code>
     */
    public static ServerSocketFactory getDefault()
    {
        synchronized (javax.net.ServerSocketFactory.class) {
            if (theFactory == null) {
                //
                // Different implementations of this method could
                // work rather differently.  For example, driving
                // this from a system property, or using a different
                // implementation than JavaSoft's.
                //
                theFactory = new DefaultServerSocketFactory();
            }
        }

        return theFactory;
    }


    /**
     * Returns a server socket bound to the specified port.
     * The socket is configured with the socket options
     * (such as accept timeout) given to this factory.
     * <P>
     * If there is a security manager, its <code>checkListen</code>
     * method is called with the <code>port</code> argument as its
     * argument to ensure the operation is allowed. This could result
     * in a SecurityException.
     *
     * @param port the port to listen to
     * @return the <code>ServerSocket</code>
     * @throws IOException for networking errors
     * @throws SecurityException if a security manager exists and its
     *         <code>checkListen</code> method doesn't allow the operation.
     * @throws IllegalArgumentException if the port parameter is outside the
     *         specified range of valid port values, which is between 0 and
     *         65535, inclusive.
     * @see    SecurityManager#checkListen
     * @see java.net.ServerSocket#ServerSocket(int)
     */
    public abstract ServerSocket createServerSocket(int port)
            throws IOException, InstantiationException;


    /**
     * Returns a server socket bound to the specified port, and uses the
     * specified connection backlog.  The socket is configured with
     * the socket options (such as accept timeout) given to this factory.
     * <P>
     * The <code>backlog</code> argument must be a positive
     * value greater than 0. If the value passed if equal or less
     * than 0, then the default value will be assumed.
     * <P>
     * If there is a security manager, its <code>checkListen</code>
     * method is called with the <code>port</code> argument as its
     * argument to ensure the operation is allowed. This could result
     * in a SecurityException.
     *
     * @param port the port to listen to
     * @param backlog how many connections are queued
     * @return the <code>ServerSocket</code>
     * @throws IOException for networking errors
     * @throws SecurityException if a security manager exists and its
     *         <code>checkListen</code> method doesn't allow the operation.
     * @throws IllegalArgumentException if the port parameter is outside the
     *         specified range of valid port values, which is between 0 and
     *         65535, inclusive.
     * @see    SecurityManager#checkListen
     * @see java.net.ServerSocket#ServerSocket(int, int)
     */
    public abstract ServerSocket
    createServerSocket(int port, int backlog)
            throws IOException, InstantiationException;


    /**
     * Returns a server socket bound to the specified port,
     * with a specified listen backlog and local IP.
     * <P>
     * The <code>ifAddress</code> argument can be used on a multi-homed
     * host for a <code>ServerSocket</code> that will only accept connect
     * requests to one of its addresses. If <code>ifAddress</code> is null,
     * it will accept connections on all local addresses. The socket is
     * configured with the socket options (such as accept timeout) given
     * to this factory.
     * <P>
     * The <code>backlog</code> argument must be a positive
     * value greater than 0. If the value passed if equal or less
     * than 0, then the default value will be assumed.
     * <P>
     * If there is a security manager, its <code>checkListen</code>
     * method is called with the <code>port</code> argument as its
     * argument to ensure the operation is allowed. This could result
     * in a SecurityException.
     *
     * @param port the port to listen to
     * @param backlog how many connections are queued
     * @param ifAddress the network interface address to use
     * @return the <code>ServerSocket</code>
     * @throws IOException for networking errors
     * @throws SecurityException if a security manager exists and its
     *         <code>checkListen</code> method doesn't allow the operation.
     * @throws IllegalArgumentException if the port parameter is outside the
     *         specified range of valid port values, which is between 0 and
     *         65535, inclusive.
     * @see    SecurityManager#checkListen
     * @see java.net.ServerSocket#ServerSocket(int, int, java.net.InetAddress)
     */
    public abstract ServerSocket
    createServerSocket(int port, int backlog, InetAddress ifAddress)
            throws IOException, InstantiationException;
    public void initSocket( Socket s ) {
    }

    /**
     Wrapper function for accept(). This allows us to trap and
     translate exceptions if necessary

     @exception IOException;
     */
    public abstract Socket acceptSocket(ServerSocket socket)
            throws IOException;

    /**
     Extra function to initiate the handshake. Sometimes necessary
     for SSL

     @exception IOException;
     */
    public abstract void handshake(Socket sock)
            throws IOException;
}