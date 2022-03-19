import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Server {

    private static HttpServer server;

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new AssertionError("Please, specify path to working directory");
        }
        String workingDirectory = args[0];
        startServer(8080, workingDirectory);

    }

    public static void startServer(int port, String workingDirectory) {
        try {
            server = HttpServer.create();
            server.bind(
                    new InetSocketAddress(port),
                    0
            );
            server.createContext("/chartas", new ChartographerHandler(workingDirectory));
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
        } catch (IOException e) {
            System.err.println("Problem with IO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void stopServer() {
        server.stop(0);
    }
}
