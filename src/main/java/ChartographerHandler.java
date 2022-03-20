import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChartographerHandler implements HttpHandler {

    private static BigInteger lastId = BigInteger.ZERO;
    private final String workingDirectory;
    private final Map<String, Chart> chartsById;

    public ChartographerHandler(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        chartsById = new HashMap<>();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Objects.requireNonNull(exchange);
        Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
        if (!pathHasId(exchange.getRequestURI().getPath())) {
            if (exchange.getRequestMethod().equals("POST")) {
                handleCreate(exchange, query);
            } else {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1);
            }
            return;
        }
        String id = parseIdFromPath(exchange.getRequestURI().getPath());
        if (!chartsById.containsKey(id)) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
            return;
        }
        Chart chart = chartsById.get(id);
        synchronized (chart) {
            if (exchange.getRequestMethod().equals("POST")) {
                handleUpdate(exchange, query, chart);
            } else if (exchange.getRequestMethod().equals("GET")) {
                handleGet(exchange, query, chart);
            } else if (exchange.getRequestMethod().equals("DELETE")) {
                handleDelete(exchange, chart, id);
            } else {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1);
            }
        }
        exchange.close();
    }

    private void handleDelete(HttpExchange exchange, Chart chart, String id) throws IOException {
        chart.delete();
        chartsById.remove(id);
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);

    }

    private void handleGet(HttpExchange exchange, Map<String, String> query, Chart chart) throws IOException {
        if (invalidFragmentParameters(query, chart, 5000)) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1);
            return;
        }
        int width = Integer.parseInt(query.get("width"));
        int height = Integer.parseInt(query.get("height"));
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, BMP.sizeOfBmp(width, height));
        chart.getSegmentIntoStream(
                Integer.parseInt(query.get("x")),
                Integer.parseInt(query.get("y")),
                width,
                height,
                exchange.getResponseBody()
        );
    }

    private void handleUpdate(HttpExchange exchange, Map<String, String> query, Chart chart) throws IOException {
        if (invalidFragmentParameters(query, chart, Integer.MAX_VALUE)) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1);
            return;
        }
        chart.updateSegmentFromStream(
                Integer.parseInt(query.get("x")),
                Integer.parseInt(query.get("y")),
                Integer.parseInt(query.get("width")),
                Integer.parseInt(query.get("height")),
                exchange.getRequestBody()
        );
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
    }


    private void handleCreate(HttpExchange exchange, Map<String, String> query) throws IOException {
        boolean correct;
        try {
            int width = Integer.parseInt(query.get("width"));
            int height = Integer.parseInt(query.get("height"));
            correct = width >= 1 && height >= 1 && width <= 20000 && height <= 50000;
        } catch (NumberFormatException e) {
            correct = false;
        }
        if (!correct) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1);
            return;
        }
        String id = createNewChart(
                Integer.parseInt(query.get("width")),
                Integer.parseInt(query.get("height"))
        );
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_CREATED, id.length());
        try (OutputStreamWriter outputStream = new OutputStreamWriter(exchange.getResponseBody())) {
            outputStream.write(id);
        }
    }

    private boolean invalidFragmentParameters(Map<String, String> query, Chart chart, int limit) {
        boolean correct;
        try {
            int x = Integer.parseInt(query.get("x"));
            int y = Integer.parseInt(query.get("y"));
            int width = Integer.parseInt(query.get("width"));
            int height = Integer.parseInt(query.get("height"));
            correct = width >= 1 && height >= 1 && width <= limit && height <= limit;
            correct &= x < chart.getWidth() && y < chart.getHeight() && x + width > 0 && y + height > 0;
        } catch (NumberFormatException e) {
            correct = false;
        }
        return !correct;
    }

    private String createNewChart(int width, int height) throws IOException {
        String newId = generateNewId();
        File picture = new File(workingDirectory + "/" + newId + ".bmp");
        chartsById.put(newId, new Chart(picture, width, height));
        return newId;
    }


    private synchronized String generateNewId() {
        lastId = lastId.add(BigInteger.ONE);
        return lastId.toString();
    }

    private boolean pathHasId(String path) {
        return path.split("/").length == 3;
    }

    private String parseIdFromPath(String path) {
        return path.split("/")[2];
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
