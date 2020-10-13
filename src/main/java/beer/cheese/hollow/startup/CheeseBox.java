package beer.cheese.hollow.startup;

import beer.cheese.hollow.LifecycleException;
import beer.cheese.hollow.Server;
import beer.cheese.hollow.connector.Connector;

import java.io.IOException;

public class CheeseBox {
    protected Server server;

    public static void main(String[] args) throws IOException {
        System.out.println("---------Startup CheeseBox----------");
        Connector connector = new Connector();
        connector.setPort(5147);
        try {
            connector.init();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }

        try {
            connector.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }
}


