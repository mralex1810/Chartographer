import org.junit.jupiter.api.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        HttpResponse<String> response = doCreate(100, 100);
        Assertions.assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        ids.add(response.body());
        id = response.body();
        tmpPicture = Path.of(resourceDirectory, "tmp.bmp");
    }

    @AfterEach
    void tearThis() throws URISyntaxException, IOException, InterruptedException {
        tmpPicture.toFile().delete();
        for (String id : ids) {
            HttpResponse<Void> response = doDelete(id);
            Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        }
        ids.clear();
    }


    @Test
    void testCreateOk() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("Create Ok tests started");
        HttpResponse<String> response;
        response = doCreate(10, 10);
        Assertions.assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        ids.add(response.body());
        response = doCreate(20000, 1);
        Assertions.assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        ids.add(response.body());
        response = doCreate(1, 50000);
        Assertions.assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        ids.add(response.body());
        System.out.println("Create Ok tests passed");
    }

    @Test
    void testCreateBad() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("Create Bad tests started");
        HttpResponse<String> response;
        response = doCreate(0, 10);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doCreate(20001, 1);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doCreate(1, 50001);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        System.out.println("Create Bad tests passed");
    }

    @Test
    void testUpdateOk() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("Update Ok tests started");
        HttpResponse<Void> response;

        response = doUpdate(id, 0, 0, 5001, 1, Path.of(resourceDirectory, "5001x1.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        response = doUpdate(id, 0, 0, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        response = doUpdate(id, -5, -9, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        response = doUpdate(id, 95, 98, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        response = doUpdate(id, 80, -20, 100, 100, Path.of(resourceDirectory, "white100.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        response = doUpdate(id, 8, 10, 100, 100, Path.of(resourceDirectory, "red100.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        response = doUpdate(id, 90, 70, 100, 100, Path.of(resourceDirectory, "green100.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        response = doUpdate(id, 50, 50, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        System.out.println("Update Ok tests passed");
    }

    @Test
    void testUpdateBad() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("Update Bad tests started");
        HttpResponse<Void> response;
        response = doUpdate(id, -10, -10, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doUpdate(id, -100, -10, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doUpdate(id, 100, 100, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doUpdate(id, -100, -100, 100, 100, Path.of(resourceDirectory, "white100.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doUpdate(id, -10, -10, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doUpdate(id, 1000, -10, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doUpdate(id, -10, 0, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        System.out.println("Update Bad tests passed");
    }

    @Test
    void testUpdateNotFound() throws IOException, URISyntaxException, InterruptedException {
        System.out.println("Update NotFound tests started");
        HttpResponse<Void> response;
        response = doUpdate("-1", 0, 0, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        response = doUpdate("-1", -100, 0, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        response = doUpdate("-1", 0, 200, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        System.out.println("Update NotFound tests passed");
    }

    @Test
    void testGetOk() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("Get Ok tests started");
        testUpdateOk();
        HttpResponse<Path> response;
        response = doGet(id, 0, 0, 10, 10);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "getOkTest", "1.bmp")));
        tmpPicture.toFile().delete();
        response = doGet(id, -50, -50, 100, 100);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "getOkTest", "2.bmp")));
        tmpPicture.toFile().delete();
        response = doGet(id, -8, 6, 96, 10);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "getOkTest", "3.bmp")));
        tmpPicture.toFile().delete();
        response = doGet(id, -5, 15, 10, 91);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "getOkTest", "4.bmp")));
        tmpPicture.toFile().delete();
        response = doGet(id, 0, 0, 100, 100);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "getOkTest", "5.bmp")));
        tmpPicture.toFile().delete();
        response = doGet(id, -10, -10, 95, 95);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "getOkTest", "6.bmp")));
        tmpPicture.toFile().delete();
        response = doGet(id, 8, 6, 95, 1);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "getOkTest", "7.bmp")));
        tmpPicture.toFile().delete();
        response = doGet(id, 82, 59, 100, 100);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "getOkTest", "8.bmp")));
        tmpPicture.toFile().delete();
        response = doGet(id, -50, -50, 200, 200);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "getOkTest", "9.bmp")));
        tmpPicture.toFile().delete();
        System.out.println("Get Ok tests passed");
    }

    @Test
    void testGetBad() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("Get Bad tests started");
        HttpResponse<Path> response;
        response = doGet(id, 200, 0, 1, 1);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doGet(id, 10, 10, 0, 10);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doGet(id, -100, -100, 100, 100);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doGet(id, -50, -30, -1, 1);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doGet(id, -2, 1, 2, 1);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doGet(id, 10, 50, -100, 320);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doGet(id, 0, 100, 1, 1);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doGet(id, 1000, 1000, 5000, 5000);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doGet(id, 0, 0, 5001, 1);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        response = doGet(id, 0, 0, 1, 5001);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        System.out.println("Get Bad tests passed");
    }

    @Test
    void testGetNotFound() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("Get NotFound tests started");
        HttpResponse<Path> response;
        response = doGet("-1", 0, 1, 2, 3);
        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        response = doGet("-1", 10, 1, -5, 3);
        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        response = doGet("-1", 50, 1, 10, 3);
        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        response = doGet("-1", 0, 80, 1, 10);
        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        System.out.println("Get NotFound tests passed");
    }

    @Test
    void testDeleteOK() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("Delete Ok tests started");
        HttpResponse<Void> response;
        response = doDelete(id);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        ids.remove(id);
        System.out.println("Delete Ok tests passed");
    }

    @Test
    void testDeleteNotFound() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("Delete NotFound tests started");
        HttpResponse<Void> response;
        response = doDelete("-1");
        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        System.out.println("Delete NotFound tests passed");

    }

    @Test
    void testFirstCase() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("First test case started");
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

        responseUpdate = doUpdate(id, 80, 5, 25, 10, tmpPicture);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());
        tmpPicture.toFile().delete();


        responseGet = doGet(id, 50, 50, 80, 80);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "firstTest", "2.bmp")));


        responseUpdate = doUpdate(id, 25, 30, 80, 80, tmpPicture);
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
        System.out.println("Second test case started");
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

    @Test
    void testThirdCase() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("Third test case started");
        HttpResponse<Void> responseUpdate;
        HttpResponse<Path> responseGet;

        responseUpdate = doUpdate(id, -5, -5, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, -1, 90, 10, 10, Path.of(resourceDirectory, "red10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, -10, -10, 10, 10, Path.of(resourceDirectory, "blue10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 80, -54, 100, 100, Path.of(resourceDirectory, "green100.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 30, -3, 10, 10, Path.of(resourceDirectory, "red10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 35, -8, 10, 10, Path.of(resourceDirectory, "blue10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseUpdate = doUpdate(id, 95, 85, 10, 10, Path.of(resourceDirectory, "white10.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseGet = doGet(id, -10, 0, 120, 20);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "thirdTest", "1.bmp")));

        responseUpdate = doUpdate(id, -30, 30, 120, 20, tmpPicture);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());
        tmpPicture.toFile().delete();


        responseGet = doGet(id, -10, -30, 150, 150);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "thirdTest", "2.bmp")));


        responseUpdate = doUpdate(id, 50, 50, 150, 150, tmpPicture);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());
        tmpPicture.toFile().delete();

        responseGet = doGet(id, 90, 90, 10, 10);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "thirdTest", "3.bmp")));
        tmpPicture.toFile().delete();

        responseGet = doGet(id, 0, 0, 100, 100);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "thirdTest", "4.bmp")));
        tmpPicture.toFile().delete();

        responseGet = doGet(id, 50, 50, 100, 100);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "thirdTest", "5.bmp")));
        tmpPicture.toFile().delete();

        responseGet = doGet(id, 56, 61, 46, 4);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "thirdTest", "6.bmp")));
        tmpPicture.toFile().delete();

        responseGet = doGet(id, 2, 11, 120, 60);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "thirdTest", "7.bmp")));
        tmpPicture.toFile().delete();

        System.out.println("Third test case passed");
    }

    @Test
    void specialTest() throws IOException, URISyntaxException, InterruptedException {
        System.out.println("Special test started");
        HttpResponse<Void> responseUpdate;
        HttpResponse<Path> responseGet;

        responseUpdate = doUpdate(id, 5, 35, 15, 30, Path.of(resourceDirectory, "specialTest", "K.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());
        responseUpdate = doUpdate(id, 20, 35, 15, 30, Path.of(resourceDirectory, "specialTest", "O.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());
        responseUpdate = doUpdate(id, 35, 35, 15, 30, Path.of(resourceDirectory, "specialTest", "N.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());
        responseUpdate = doUpdate(id, 50, 35, 15, 30, Path.of(resourceDirectory, "specialTest", "T.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());
        responseUpdate = doUpdate(id, 65, 35, 15, 30, Path.of(resourceDirectory, "specialTest", "U.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());
        responseUpdate = doUpdate(id, 80, 35, 15, 30, Path.of(resourceDirectory, "specialTest", "R.bmp"));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseUpdate.statusCode());

        responseGet = doGet(id, 0, 0, 100, 100);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, responseGet.statusCode());
        Assertions.assertTrue(isEquals(tmpPicture, Path.of(resourceDirectory, "specialTest", "KONTUR.bmp")));
        tmpPicture.toFile().delete();
        System.out.println("Special test passed");
    }

    private HttpResponse<String> doCreate(int width, int height)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(String.format("http://localhost:8080/chartas/?width=%d&height=%d", width, height)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<Void> doDelete(String id) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/chartas/" + id + "/"))
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.discarding());
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
