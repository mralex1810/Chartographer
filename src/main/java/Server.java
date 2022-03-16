import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new AssertionError("Please, specify path to working directory");
        }
        String workingDirectory = args[0];
        try {
            HttpServer server = HttpServer.create();
            server.bind(
                    new InetSocketAddress(8080),
                    0
            );
            server.createContext("/chartas", new ChartographerHandler(workingDirectory));
            server.start();
        } catch (IOException e) {
            System.err.println("Problem with IO: " + e.getMessage());
            e.printStackTrace();
        }


    }
}
