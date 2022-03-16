import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChartographerHandler implements HttpHandler {

    private final String workingDirectory;
    private final Map<Long, Chart> chartsById;
    private long maxId;

    public ChartographerHandler(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        chartsById = new HashMap<>();
        maxId = 0;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Objects.requireNonNull(exchange);
        Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
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
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        long id = parseIdFromPath(exchange.getRequestURI().getPath());
        System.out.printf("Delete %d\n", id);
        if (!chartsById.containsKey(id)) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
            return;
        }
        chartsById.get(id).delete();
        chartsById.remove(id);
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        exchange.getResponseBody().flush();
    }

    private void handleGet(HttpExchange exchange, Map<String, String> query) throws IOException {
        long id = parseIdFromPath(exchange.getRequestURI().getPath());
        System.out.printf("Get %d\n", id);
        if (checkErrorHandle(id, exchange, query)) {
            return;
        }
        int width = Integer.parseInt(query.get("width"));
        int height = Integer.parseInt(query.get("height"));
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, BMP.sizeOfBmp(width, height));
        chartsById.get(id).getSegmentIntoStream(
                Integer.parseInt(query.get("x")),
                Integer.parseInt(query.get("y")),
                width,
                height,
                exchange.getResponseBody()
        );

    }

    private void handleUpdate(HttpExchange exchange, Map<String, String> query) throws IOException {
        long id = parseIdFromPath(exchange.getRequestURI().getPath());
        System.out.printf("Update %d\n", id);
        if (checkErrorHandle(id, exchange, query)) {
            return;
        }
        chartsById.get(id).updateSegmentFromStream(
                Integer.parseInt(query.get("x")),
                Integer.parseInt(query.get("y")),
                Integer.parseInt(query.get("width")),
                Integer.parseInt(query.get("height")),
                exchange.getRequestBody()
        );
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        exchange.getResponseBody().flush();
    }


    private void handleCreate(HttpExchange exchange, Map<String, String> query) throws IOException {
        long id;
        boolean correct;
        try {
            int width = Integer.parseInt(query.get("width"));
            int height = Integer.parseInt(query.get("height"));
            correct = width >= 1 && height >= 1 && width <= 20000 && height <= 50000;
        } catch (NumberFormatException e) {
            correct = false;
        }
        if (!correct) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
            return;
        }
        id = createNewChart(
                Integer.parseInt(query.get("width")),
                Integer.parseInt(query.get("height"))
        );
        System.out.printf("Created %d\n", id);
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_CREATED, Long.toString(id).length());
        try (OutputStreamWriter outputStream = new OutputStreamWriter(exchange.getResponseBody())) {
            outputStream.write(Long.toString(id));
        }
    }

    private boolean checkErrorHandle(long id, HttpExchange exchange, Map<String, String> query) throws IOException {
        if (!chartsById.containsKey(id)) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
            return true;
        }
        Chart chart = chartsById.get(id);
        if (invalidFragmentParameters(query, chart.getWidth(), chart.getHeight())) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
            return true;
        }
        return false;
    }

    private boolean invalidFragmentParameters(Map<String, String> query, int pictureWidth, int pictureHeight) {
        boolean correct;
        try {
            int x = Integer.parseInt(query.get("x"));
            int y = Integer.parseInt(query.get("y"));
            int width = Integer.parseInt(query.get("width"));
            int height = Integer.parseInt(query.get("height"));
            correct = width >= 1 && height >= 1 && width <= 5000 && height <= 5000 && x >= 0 && y >= 0;
            correct &= x < pictureWidth && y < pictureHeight;
        } catch (NumberFormatException e) {
            correct = false;
        }
        return !correct;
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
