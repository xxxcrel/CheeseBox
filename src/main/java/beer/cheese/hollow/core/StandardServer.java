package beer.cheese.hollow.core;

import beer.cheese.hollow.LifecycleException;
import beer.cheese.hollow.Server;
import beer.cheese.hollow.utils.LifecycleBase;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

public class StandardServer extends LifecycleBase implements Server {

    private int port = 5147;

    private String address = "localhost";


    public StandardServer() {
        super();
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public String getAddress() {
        return this.address;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    protected void initInternal() throws LifecycleException {

    }

    @Override
    protected void startInternal() throws LifecycleException {
        try(ServerSocket serverSocket = new ServerSocket(getPort())){
            Socket client = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String msg = in.readLine();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            out.write(msg);
        }catch (IOException e){
            e.printStackTrace();
        };
    }

    @Override
    protected void stopInternal() throws LifecycleException {

    }

    @Override
    protected void destroyInternal() throws LifecycleException {

    }
}
