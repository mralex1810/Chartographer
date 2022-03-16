import com.macfaq.io.LittleEndianOutputStream;

import java.io.*;
import java.nio.file.Path;

public class Chart extends BMP {

    public Chart(File picture, int width, int height) throws IOException {
        super(picture, width, height);
    }

    public void updateSegmentFromStream(int x, int y, int segmentWidth, int segmentHeight, InputStream fragment)
            throws NotIntersectException, IOException {
        checkIntersection(x, y, segmentWidth, segmentHeight);
        fragment.readNBytes(10);
        int offset = fragment.read() + fragment.read() << 8;
        fragment.readNBytes(offset - 12);
        try (RandomAccessFile file = getRandomAccessFileData("w")) {
            file.skipBytes(3 * x * y);
            for (int row = y; row < Math.min(y + segmentHeight, height); row++) {
                for (int col = x; col < Math.min(x + segmentWidth, width); col++) {
                    for (int i = 0; i < 3; i++) {
                        file.write(fragment.read());
                    }
                }
                if (x + segmentHeight > height) {
                    fragment.readNBytes(x + segmentHeight - height);
                }
                file.skipBytes(Math.max(0, height - x - segmentHeight) + x);
            }
        } catch (IOException e) {
            throw new IOException("Problem with editing file: " + getPath(), e);
        }
    }

    public void getSegmentIntoStream(int x, int y, int segmentWidth, int segmentHeight, OutputStream outputStream)
            throws NotIntersectException, IOException {
        checkIntersection(x, y, segmentWidth, segmentHeight);
        LittleEndianOutputStream stream = new LittleEndianOutputStream(new BufferedOutputStream(outputStream));
        initHeaderIntoStream(stream, width, height);
        try (RandomAccessFile file = getRandomAccessFileData("r")) {
            file.skipBytes(3 * x * y);
            for (int row = y; row < Math.min(y + segmentHeight, height); row++) {
                for (int col = x; col < Math.min(x + segmentWidth, width); col++) {
                    for (int i = 0; i < 3; i++) {
                        stream.write(file.read());
                    }
                }
                for (int col = width; col < x + segmentWidth; col++) {
                    for (int i = 0; i < 3; i++) {
                        stream.write(0);
                    }
                }
                file.skipBytes(Math.max(0, height - x - segmentHeight) + x);
            }
            for (int row = y; row < Math.min(y + segmentHeight, height); row++) {
                for (int col = width; col < x + segmentWidth; col++) {
                    for (int i = 0; i < 3; i++) {
                        stream.write(0);
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Problem with reading file: " + getPath(), e);
        }
    }

    private void checkIntersection(int x, int y, int fragmentWidth, int fragmentHeight) throws NotIntersectException {
        if (x > width || y > height) {
            throw new NotIntersectException(String.format(
                    "x = %d, y = %d, width = %d, height = %d does not intersect with the chart: width = %d, height = %d",
                    x, y, fragmentWidth, fragmentHeight, width, height
            ));
        }
    }

}
