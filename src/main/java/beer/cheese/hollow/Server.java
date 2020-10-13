package beer.cheese.hollow;

public interface Server extends Lifecycle{

    void setPort(int port);
    int getPort();
    String getAddress();
    void setAddress(String address);
}
