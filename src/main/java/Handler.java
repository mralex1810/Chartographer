import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Handler implements HttpHandler {

    private final String workingDirectory;
    private final Map<Long, Chart> chartsById;
    private long maxId;

    public Handler(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        chartsById = new HashMap<>();
        maxId = 0;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Objects.requireNonNull(exchange);
        Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
        try {
            if (exchange.getRequestMethod().equals("POST")) {
                if (pathHasId(exchange.getRequestURI().getPath())) {
                    handleUpdate(exchange, query);
                } else {
                    handleCreate(exchange, query);
                }
            } else if (exchange.getRequestMethod().equals("GET")) {
                handleGet(exchange, query);
            } else if (exchange.getRequestMethod().equals("DELETE")) {
                handleDelete(exchange);
            } else {
                // TODO
            }
        } finally {
            exchange.close();
        }

    }

    private void handleDelete(HttpExchange exchange) {
        long id = parseIdFromPath(exchange.getRequestURI().getPath());
        System.out.printf("Delete %d\n", id);
        if (!chartsById.containsKey(id)) {
            //TODO
        }
        try {
            chartsById.get(id).delete();
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGet(HttpExchange exchange, Map<String, String> query) {
        long id = parseIdFromPath(exchange.getRequestURI().getPath());
        System.out.printf("Get %d\n", id);

        if (!chartsById.containsKey(id)) {
            //TODO
        }
        try {
            chartsById.get(id).getSegmentIntoStream(
                    Integer.parseInt(query.get("x")),
                    Integer.parseInt(query.get("y")),
                    Integer.parseInt(query.get("width")),
                    Integer.parseInt(query.get("height")),
                    exchange.getResponseBody()
            );
        } catch (NotIntersectException e) {
            e.printStackTrace(); //TODO
        } catch (IOException e) {
            e.printStackTrace(); //TODO
        }
    }

    private void handleUpdate(HttpExchange exchange, Map<String, String> query) {
        long id = parseIdFromPath(exchange.getRequestURI().getPath());
        System.out.printf("Update %d\n", id);
        if (chartsById.containsKey(id)) {
            // TODO
        }
        try {
            chartsById.get(id).updateSegmentFromStream(
                    Integer.parseInt(query.get("x")),
                    Integer.parseInt(query.get("y")),
                    Integer.parseInt(query.get("width")),
                    Integer.parseInt(query.get("height")),
                    exchange.getRequestBody()
            );
        } catch (NotIntersectException e) {
            e.printStackTrace(); //TODO
        } catch (IOException e) {
            e.printStackTrace(); //TODO
        }
    }

    private void handleCreate(HttpExchange exchange, Map<String, String> query) throws IOException {
        long id = -1;
        try {
            id = createNewChart(
                    Integer.parseInt(query.get("width")),
                    Integer.parseInt(query.get("height"))
            );
        } catch (IOException e) {
            e.printStackTrace(); //TODO
        }
        System.out.printf("Created %d\n", id);
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_CREATED, Long.toString(id).length());
        try (OutputStreamWriter outputStream = new OutputStreamWriter(exchange.getResponseBody())) {
            outputStream.write(Long.toString(id));
        }
    }

    private long createNewChart(int width, int height) throws IOException {
        long newId = generateNewId();
        File picture = new File(workingDirectory + "/" + newId + ".bmp");
        chartsById.put(newId, new Chart(picture, width, height));
        return newId;
    }


    private long generateNewId() {
        return maxId++;
    }

    private boolean pathHasId(String path) {
        return path.split("/").length == 3;
    }

    private long parseIdFromPath(String path) {
        return Long.parseLong(path.split("/")[2]);
    }

    private Map<String, String> queryToMap(String query) {
        if (query == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }
}
