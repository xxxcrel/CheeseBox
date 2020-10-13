package beer.cheese.hollow.connector;

import beer.cheese.hollow.LifecycleException;
import beer.cheese.hollow.LifecycleState;
import beer.cheese.hollow.connector.endpoint.AbstractEndpoint;
import beer.cheese.hollow.connector.endpoint.JIoEndpoint;
import beer.cheese.hollow.juli.logging.Log;
import beer.cheese.hollow.juli.logging.LogFactory;
import beer.cheese.hollow.utils.LifecycleBase;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;

public class Connector extends LifecycleBase {

    private final Log log = LogFactory.getLog(getClass());

    protected Http11ConnectionHandler connectionHandler = new Http11ConnectionHandler();
    protected JIoEndpoint endpoint = new JIoEndpoint();

    @Override
    protected void fireLifecycleEvent(String type, Object data) {
        super.fireLifecycleEvent(type, data);
    }

    @Override
    protected void initInternal() throws LifecycleException {
        endpoint.setName(getName());
        endpoint.setHandler(connectionHandler);
        //目前没有用户线程, 全是daemon线程会直接退出程序
        endpoint.setDaemon(false);
        try {
            endpoint.init();
        } catch (Exception ex) {
            log.error("Error initializing endpoint", ex);
            throw new LifecycleException(ex);
        }
        if (log.isInfoEnabled())
            log.info("Initializing CheeseBreeze HTTP/1.1 on " + getName());
    }

    @Override
    protected void startInternal() throws LifecycleException {
        fireLifecycleEvent(CONFIGURE_START_EVENT, null);
        setState(LifecycleState.STARTING);
        try {
            endpoint.start();
        } catch (Exception ex) {
            log.error("Error starting endpoint", ex);
            throw new LifecycleException(ex);
        }
        if (log.isInfoEnabled())
            log.info("Starting CheeseBreeze HTTP/1.1 on " + getName());

    }

    @Override
    protected void stopInternal() throws LifecycleException {

    }

    @Override
    protected void destroyInternal() throws LifecycleException {

    }

    public int getMaxThreads() { return endpoint.getMaxThreads(); }

    public void setMaxThreads(int maxThreads) { endpoint.setMaxThreads(maxThreads); }

    public int getThreadPriority() { return endpoint.getThreadPriority(); }

    public void setThreadPriority(int threadPriority) { endpoint.setThreadPriority(threadPriority); }

    public int getBacklog() { return endpoint.getBacklog(); }

    public void setBacklog(int backlog) { endpoint.setBacklog(backlog); }

    public int getPort() { return endpoint.getPort(); }

    public void setPort(int port) { endpoint.setPort(port); }

    public InetAddress getAddress() { return endpoint.getAddress(); }

    public void setAddress(InetAddress ia) { endpoint.setAddress(ia); }

    public boolean getTcpNoDelay() { return endpoint.getTcpNoDelay(); }

    public void setTcpNoDelay(boolean tcpNoDelay) { endpoint.setTcpNoDelay(tcpNoDelay); }

    public int getSoLinger() { return endpoint.getSoLinger(); }

    public void setSoLinger(int soLinger) { endpoint.setSoLinger(soLinger); }

    public int getSoTimeout() { return endpoint.getSoTimeout(); }

    public void setSoTimeout(int soTimeout) { endpoint.setSoTimeout(soTimeout); }


    public String getName() {
        return createName("http", getAddress(), endpoint.getPort());
    }

    /**
     * An utility method, used to implement getName() in subclasses.
     */
    protected String createName(String prefix, InetAddress address, int port) {
        StringBuilder name = new StringBuilder(prefix);
        name.append('-');
        if (address != null) {
            String strAddr = address.toString();
            if (strAddr.startsWith("/")) {
                strAddr = strAddr.substring(1);
            }
            name.append(URLEncoder.encode(strAddr)).append('-');
        }
        if (port == 0) {
            // Auto binding is in use. Check if port is known
            name.append("auto-");
//            name.append(getNameIndex());
            port = endpoint.getLocalPort();
            if (port != -1) {
                name.append('-');
                name.append(port);
            }
        } else {
            name.append(port);
        }
        return name.toString();
    }

    protected static class Http11ConnectionHandler implements AbstractEndpoint.Handler {
        @Override
        public boolean process(Socket socket) {
            try (OutputStream out = socket.getOutputStream()) {
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(out), true);
                writer.println("Hello Nice to meet you");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}
