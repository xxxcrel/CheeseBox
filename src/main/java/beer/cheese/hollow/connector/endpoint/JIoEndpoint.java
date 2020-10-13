package beer.cheese.hollow.connector.endpoint;

import beer.cheese.hollow.juli.logging.Log;
import beer.cheese.hollow.juli.logging.LogFactory;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * 根据Tomcat6改编
 */
public class JIoEndpoint extends AbstractEndpoint {
    private final Log log = LogFactory.getLog(getClass());
    /**
     * Associated server socket.
     */
    protected ServerSocket serverSocket = null;

    /**
     * Available works
     */
    protected WorkerStack workers = null;

    /**
     * Handling of accepted sockets.
     */
    protected Handler handler = null;

    public void setHandler(Handler handler) { this.handler = handler; }

    public Handler getHandler() { return handler; }

    /**
     * Name of the thread pool, which will be used for naming child threads.
     */
    protected String name = "TP";

    public void setName(String name) { this.name = name; }

    public String getName() { return name; }

    /**
     * Maximum amount of worker threads.
     */
    protected int maxThreads = 200;

    /**
     * 当前正在运行的Worker线程数量
     */
    protected int curThreadsBusy = 0;


    /**
     * 已有(缓存)的线程数量(正在运行的和被放进WorkerStack中总数量).
     */
    protected int curThreads = 0;

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        if (running) {
            synchronized (workers) {
                workers.resize(maxThreads);
            }
        }
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    /**
     * Priority of the acceptor and poller threads.
     */
    protected int threadPriority = Thread.NORM_PRIORITY;

    public void setThreadPriority(int threadPriority) { this.threadPriority = threadPriority; }

    public int getThreadPriority() { return threadPriority; }

    /**
     * The default is true - the created threads will be
     *  in daemon mode. If set to false, the control thread
     *  will not be daemon - and will keep the process alive.
     */
    protected boolean daemon = true;

    public void setDaemon(boolean b) { daemon = b; }

    public boolean getDaemon() { return daemon; }

    /**
     * Socket TCP no delay.
     */
    protected boolean tcpNoDelay = false;

    public boolean getTcpNoDelay() { return tcpNoDelay; }

    public void setTcpNoDelay(boolean tcpNoDelay) { this.tcpNoDelay = tcpNoDelay; }


    /**
     * Socket linger.
     */
    protected int soLinger = 100;

    public int getSoLinger() { return soLinger; }

    public void setSoLinger(int soLinger) { this.soLinger = soLinger; }


    /**
     * Socket timeout.
     */
    protected int soTimeout = -1;

    public int getSoTimeout() { return soTimeout; }

    public void setSoTimeout(int soTimeout) { this.soTimeout = soTimeout; }

    public void init() throws Exception {
        if (initialized) {
            return;
        }

        if (serverSocketFactory == null) {
            serverSocketFactory = ServerSocketFactory.getDefault();
        }

        if (serverSocket == null) {
            try {
                if (getAddress() == null) {
                    serverSocket = serverSocketFactory.createServerSocket(getPort(), getBacklog());
                } else {
                    serverSocket = serverSocketFactory.createServerSocket(getPort(), getBacklog(), getAddress());
                }
            } catch (BindException orig) {
                String msg;
                if (getAddress() == null)
                    msg = orig.getMessage() + " <null>:" + getPort();
                else
                    msg = orig.getMessage() + " " +
                            getAddress().toString() + ":" + getPort();
                BindException be = new BindException(msg);
                be.initCause(orig);
                throw be;
            }
        }

        if (workers == null) {
            workers = new WorkerStack(maxThreads);
        }
        initialized = true;
    }

    public void start() throws Exception {
        if (!initialized) {
            init();
        }

        if (!running) {
            running = true;
            paused = false;

            Thread acceptorThread = new Thread(new Acceptor(), getName() + "-Acceptor-" + 1);
            acceptorThread.setDaemon(daemon);
            acceptorThread.setPriority(threadPriority);
            acceptorThread.start();

        }
    }

    /**
     * Port in use
     * @return
     */
    @Override
    public int getLocalPort() {
        ServerSocket s = serverSocket;
        if (s == null) {
            return -1;
        } else {
            return s.getLocalPort();
        }
    }


    /**
     * Set the options for the current socket.
     */
    protected boolean setSocketOptions(Socket socket) {
        // Process the connection
        int step = 1;
        try {

            // 1: Set socket options: timeout, linger, etc
            if (soLinger >= 0) {
                socket.setSoLinger(true, soLinger);
            }
            if (tcpNoDelay) {
                socket.setTcpNoDelay(tcpNoDelay);
            }
            if (soTimeout > 0) {
                socket.setSoTimeout(soTimeout);
            }

            // 2: SSL handshake
            step = 2;
            serverSocketFactory.handshake(socket);

        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                if (step == 2) {
                    log.debug("Handshake failed", t);
                } else {
                    log.debug("Unexpected error processing socket", t);
                }
            }
            // Tell to close the socket
            return false;
        }
        return true;
    }

    /**
     * Create (or allocate) and return an available processor for use in
     * processing a specific HTTP request, if possible.  If the maximum
     * allowed processors have already been created and are in use, return
     * <code>null</code> instead.
     */
    protected Worker createWorkerThread() {

        synchronized (workers) {
            if (workers.size() > 0) {
                curThreadsBusy++;
                return workers.pop();
            }
            if ((maxThreads > 0) && (curThreads < maxThreads)) {
                curThreadsBusy++;
                if (curThreadsBusy == maxThreads) {
                    log.info("Maximum number of threads " + maxThreads + " created for connector with address " + getAddress() + " and port " + getPort());
                }
                return (newWorkerThread());
            } else {
                if (maxThreads < 0) {
                    curThreadsBusy++;
                    return (newWorkerThread());
                } else {
                    return (null);
                }
            }
        }

    }


    /**
     * Create and return a new processor suitable for processing HTTP
     * requests and returning the corresponding responses.
     */
    protected Worker newWorkerThread() {

        Worker workerThread = new Worker();
        workerThread.start();
        return (workerThread);

    }


    /**
     * Return a new worker thread, and block while to worker is available.
     */
    protected Worker getWorkerThread() {
        // Allocate a new worker thread
        synchronized (workers) {
            Worker workerThread;
            while ((workerThread = createWorkerThread()) == null) {
                try {
                    workers.wait();
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
            return workerThread;
        }
    }


    /**
     * Recycle the specified Processor so that it can be used again.
     *
     * @param workerThread The processor to be recycled
     */
    protected void recycleWorkerThread(Worker workerThread) {
        synchronized (workers) {
            workers.push(workerThread);
            curThreadsBusy--;
            workers.notify();
        }
    }


    /**
     * Process given socket.
     */
    protected boolean processSocket(Socket socket) {
        try {
            getWorkerThread().assign(socket);
        } catch (Throwable t) {
            // This means we got an OOM or similar creating a thread, or that
            // the pool and its queue are full
            log.error("Error allocating socket processor", t);
            return false;
        }
        return true;
    }

    public class WorkerStack {

        protected Worker[] workers = null;
        protected int end = 0;

        public WorkerStack(int size) {
            workers = new Worker[size];
        }

        /**
         * Put the worker into the queue. If the queue is full (for example if
         * the queue has been reduced in size) the worker will be dropped.
         *
         * @param   worker  the worker to be appended to the queue (first
         *                  element).
         */
        public void push(Worker worker) {
            if (end < workers.length) {
                workers[end++] = worker;
            } else {
                curThreads--;
            }
        }

        /**
         * Get the first object out of the queue. Return null if the queue
         * is empty.
         */
        public Worker pop() {
            if (end > 0) {
                return workers[--end];
            }
            return null;
        }

        /**
         * Get the first object out of the queue, Return null if the queue
         * is empty.
         */
        public Worker peek() {
            return end > 0 ? workers[end - 1] : null;
        }

        /**
         * Is the queue empty?
         */
        public boolean isEmpty() {
            return (end == 0);
        }

        /**
         * How many elements are there in this queue?
         */
        public int size() {
            return (end);
        }

        /**
         * Resize the queue. If there are too many objects in the queue for the
         * new size, drop the excess.
         */
        public void resize(int newSize) {
            Worker[] newWorkers = new Worker[newSize];
            int len = workers.length;
            if (newSize < len) {
                len = newSize;
            }
            System.arraycopy(workers, 0, newWorkers, 0, len);
            workers = newWorkers;
        }
    }

    protected class Worker implements Runnable {

        protected Thread thread = null;
        protected boolean available = false;
        protected Socket socket = null;


        /**
         * Process an incoming TCP/IP connection on the specified socket.  Any
         * exception that occurs during processing must be logged and swallowed.
         * <b>NOTE</b>:  This method is called from our Connector's thread.  We
         * must assign it to our own thread so that multiple simultaneous
         * requests can be handled.
         *
         * @param socket TCP socket to process
         */
        synchronized void assign(Socket socket) {

            // Wait for the Processor to get the previous Socket
            while (available) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            // Store the newly available Socket and notify our thread
            this.socket = socket;
            available = true;
            notifyAll();

        }


        /**
         * Await a newly assigned Socket from our Connector, or <code>null</code>
         * if we are supposed to shut down.
         */
        private synchronized Socket await() {

            // Wait for the Connector to provide a new Socket
            while (!available) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            // Notify the Connector that we have received this Socket
            Socket socket = this.socket;
            available = false;
            notifyAll();

            return (socket);

        }


        /**
         * The background thread that listens for incoming TCP/IP connections and
         * hands them off to an appropriate processor.
         */
        public void run() {

            // Process requests until we receive a shutdown signal
            while (running) {

                // Wait for the next socket to be assigned
                Socket socket = await();
                if (socket == null)
                    continue;

                // Process the request from this socket
                if (!setSocketOptions(socket) || !handler.process(socket)) {
                    // Close socket
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                }

                // Finish up this request
                socket = null;
                recycleWorkerThread(this);

            }

        }


        /**
         * Start the background processing thread.
         */
        public void start() {
            thread = new Thread(this);
            thread.setName(getName() + "-" + (++curThreads));
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected class Acceptor implements Runnable {
        @Override
        public void run() {
            while (running) {
                while (paused) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                }

                try {
                    Socket socket = serverSocketFactory.acceptSocket(serverSocket);
                    serverSocketFactory.initSocket(socket);
                    if (!processSocket(socket)) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            //Ignore;
                        }
                    }
                } catch (IOException e) {

                }
            }
        }
    }
}
