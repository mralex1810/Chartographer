import java.io.File;
import java.io.IOException;

public class BMPCreator {
    public static void main(String[] args) throws IOException {
        new BMP(new File("/home/alex/Programming/Chartographer/src/test/resources/black100.bmp"), 100, 100, 0, 0, 0);
        new BMP(new File("/home/alex/Programming/Chartographer/src/test/resources/black10.bmp"), 10, 10, 0, 0, 0);
        new BMP(new File("/home/alex/Programming/Chartographer/src/test/resources/red100.bmp"), 100, 100, 255, 0, 0);
        new BMP(new File("/home/alex/Programming/Chartographer/src/test/resources/red10.bmp"), 10, 10, 255, 0, 0);
        new BMP(new File("/home/alex/Programming/Chartographer/src/test/resources/green100.bmp"), 100, 100, 0, 255, 0);
        new BMP(new File("/home/alex/Programming/Chartographer/src/test/resources/green10.bmp"), 10, 10, 0, 255, 0);
        new BMP(new File("/home/alex/Programming/Chartographer/src/test/resources/blue100.bmp"), 100, 100, 0, 0, 255);
        new BMP(new File("/home/alex/Programming/Chartographer/src/test/resources/blue10.bmp"), 10, 10, 0, 0, 255);
        new BMP(new File("/home/alex/Programming/Chartographer/src/test/resources/white100.bmp"), 100, 100, 255, 255, 255);
        new BMP(new File("/home/alex/Programming/Chartographer/src/test/resources/white10.bmp"), 10, 10, 255, 255, 255);
    }
}
