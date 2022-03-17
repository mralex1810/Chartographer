import org.junit.jupiter.api.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ChartographerTest {
    Set<String> ids = new HashSet<>();
    HttpClient client;
    String resourceDirectory;
    String id;
    Path tmpPicture;

    @BeforeAll
    static void setup() {
        Server.startServer(8080,
                Path.of(new File(".").getAbsolutePath(), "target", "test-classes", "working").toString());
    }

    @AfterAll
    static void tear() {
        Server.stopServer();
    }

    @BeforeEach
    void setupThis() throws URISyntaxException, IOException, InterruptedException {
        client = HttpClient.newBuilder().build();
        resourceDirectory = Path.of(new File("").getAbsolutePath(), "target", "test-classes", "files")
                .toAbsolutePath().toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/?width=100&height=100"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        ids.add(response.body());
        id = response.body();
        tmpPicture = Path.of(resourceDirectory, "tmp.bmp");
    }

    @AfterEach
    void tearThis() throws URISyntaxException, IOException, InterruptedException {
        for (String id : ids) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/chartas/" + id + "/"))
                    .DELETE()
                    .build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        }
        ids.clear();
    }

    @Test
    void testCreateOk() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/?width=10&height=10"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        ids.add(response.body());
        request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/?width=20000&height=1"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        ids.add(response.body());
        request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/?width=1&height=50000"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assertions.assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        ids.add(response.body());
        System.out.println("Create Ok tests passed");
    }

    @Test
    void testCreateBad() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/?width=0&height=10"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/?width=20001&height=1"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.discarding());
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/?width=1&height=50001"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.discarding());
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        System.out.println("Create Bad tests passed");
    }

    @Test
    void testUpdateOk() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/" + id + "/?x=0&y=0&width=10&height=10"))
                .POST(HttpRequest.BodyPublishers.ofFile(
                        Path.of(resourceDirectory, "black10.bmp")))
                .build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        System.out.println("Update Ok tests passed");
    }

    @Test
    void testUpdateBad() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/" + id + "/?x=200&y=0&width=10&height=10"))
                .POST(HttpRequest.BodyPublishers.ofFile(
                        Path.of(resourceDirectory, "black10.bmp")))
                .build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        System.out.println("Update Bad tests passed");
    }

    @Test
    void testUpdateNotFound() throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/-1/?x=0&y=0&width=10&height=10"))
                .POST(HttpRequest.BodyPublishers.ofFile(
                        Path.of(resourceDirectory, "black10.bmp")))
                .build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        System.out.println("Update NotFound tests passed");
    }

    @Test
    void testGetOk() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/" + id + "/?x=0&y=0&width=10&height=10"))
                .GET()
                .build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        System.out.println("Get Ok tests passed");
    }

    @Test
    void testGetBad() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/" + id + "/?x=200&y=0&width=10&height=10"))
                .GET()
                .build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        System.out.println("Get Bad tests passed");
    }

    @Test
    void testGetNotFound() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/-1/?x=200&y=0&width=10&height=10"))
                .GET()
                .build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        System.out.println("Get NotFound tests passed");
    }

    @Test
    void testDeleteOK() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/" + id + "/"))
                .DELETE()
                .build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        ids.remove(id);
        System.out.println("Delete Ok tests passed");
    }

    @Test
    void testDeleteNotFound() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/-1/"))
                .DELETE()
                .build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        System.out.println("Delete NotFound tests passed");

    }

    @Test
    void testFirstCase() throws IOException, InterruptedException, URISyntaxException {
        HttpResponse<Void> responseUpdate;
        HttpResponse<Path> responseGet;

        responseUpdate = doUpdate(id, 95, 1, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 86, 10, 10, 10, Path.of(resourceDirectory, "red10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 5, 110, 10, 10, Path.of(resourceDirectory, "blue10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 5, 90, 100, 100, Path.of(resourceDirectory, "green100.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 90, 90, 10, 10, Path.of(resourceDirectory, "red10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 95, 80, 100, 100, Path.of(resourceDirectory, "blue100.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 95, 85, 10, 10, Path.of(resourceDirectory, "green10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseGet = doGet(id, 85, 85, 25, 10);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "firstTest", "1.bmp")));

        responseUpdate = doUpdate(id, 80, 5,25, 10, tmpPicture);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());
        tmpPicture.toFile().delete();


        responseGet = doGet(id, 50, 50, 80, 80);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "firstTest", "2.bmp")));


        responseUpdate = doUpdate(id, 25, 30,80, 80, tmpPicture);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());
        tmpPicture.toFile().delete();

        responseGet = doGet(id, 90, 90, 10, 10);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "firstTest", "3.bmp")));
        tmpPicture.toFile().delete();

        responseGet = doGet(id, 0, 0, 100, 100);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "firstTest", "4.bmp")));
        tmpPicture.toFile().delete();

        responseGet = doGet(id, 50, 50, 100, 100);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "firstTest", "5.bmp")));
        tmpPicture.toFile().delete();

        responseGet = doGet(id, 56, 61, 46, 4);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "firstTest", "6.bmp")));
        tmpPicture.toFile().delete();

        responseGet = doGet(id, 2, 11, 120, 60);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "firstTest", "7.bmp")));
        tmpPicture.toFile().delete();

        System.out.println("First test case passed");
    }

    @Test
    void testSecondCase() throws IOException, InterruptedException, URISyntaxException {
        HttpResponse<Void> responseUpdate;
        HttpResponse<Path> responseGet;

        responseUpdate = doUpdate(id, 0, 0, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 5, 5, 10, 10, Path.of(resourceDirectory, "red10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 10, 10, 10, 10, Path.of(resourceDirectory, "blue10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 50, 50, 100, 100, Path.of(resourceDirectory, "green100.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 95, 95, 10, 10, Path.of(resourceDirectory, "red10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());


        responseGet = doGet(id, 0, 0, 10, 10);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "secondTest", "1.bmp")));
        tmpPicture.toFile().delete();

        responseGet = doGet(id, 45, 50, 10, 10);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "secondTest", "2.bmp")));
        tmpPicture.toFile().delete();

        responseGet = doGet(id, 0, 0, 100, 100);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "secondTest", "3.bmp")));
        tmpPicture.toFile().delete();

        responseGet = doGet(id, 90, 90, 15, 15);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "secondTest", "4.bmp")));
        tmpPicture.toFile().delete();
        System.out.println("Second test case passed");
    }

    private HttpResponse<Void> doUpdate(String id, int x, int y, int width, int height, Path path)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(String.format("http://localhost:8080/chartas/%s/?x=%d&y=%d&width=%d&height=%d",
                        id, x, y, width, height)))
                .POST(HttpRequest.BodyPublishers.ofFile(path))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.discarding());
    }

    private HttpResponse<Path> doGet(String id, int x, int y, int width, int height)
            throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(String.format("http://localhost:8080/chartas/%s/?x=%d&y=%d&width=%d&height=%d",
                        id, x, y, width, height)))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofFile(tmpPicture));
    }

    private boolean isEquals(Path first, Path second) throws IOException {
        try (InputStream firstStream = new BufferedInputStream(new FileInputStream(first.toFile()));
             InputStream secondStream = new BufferedInputStream(new FileInputStream(second.toFile()))) {
            while (true) {
                int firstRead = firstStream.read();
                int secondRead = secondStream.read();
                if (firstRead == -1) {
                    return secondRead == -1;
                }
                if (firstRead != secondRead) {
                    return false;
                }
            }
        }
    }


}
