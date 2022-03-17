import com.macfaq.io.LittleEndianOutputStream;

import java.io.*;

public class Chart extends BMP {

    public Chart(File picture, int width, int height) throws IOException {
        super(picture, width, height);
    }

    public void updateSegmentFromStream(int x, int y, int segmentWidth, int segmentHeight, InputStream fragment)
            throws IOException {
        fragment.readNBytes(10);
        int offset = fragment.read() + (fragment.read() << 8) + (fragment.read() << 16) + (fragment.read() << 24);
        fragment.readNBytes(offset - 14);
        int rowLength = (segmentWidth * bytePerPixel * 8 + 31) / 32 * 4;
        int segmentPadding = rowLength - segmentWidth * bytePerPixel;
        try (RandomAccessFile file = getRandomAccessFileData("rw")) {
            file.skipBytes(bytePerPixel * ((getWidth() + getPadding()) * y + x));
            for (int row = y; row < Math.min(y + segmentHeight, getHeight()); row++) {
                for (int col = x; col < Math.min(x + segmentWidth, getWidth()); col++) {
                    for (int i = 0; i < bytePerPixel; i++) {
                        file.write(fragment.read());
                    }
                }
                if (x + segmentWidth > getWidth()) {
                    fragment.readNBytes(bytePerPixel * (x + segmentWidth - getWidth()));
                }
                fragment.readNBytes(segmentPadding);
                file.skipBytes(bytePerPixel * (Math.max(0, (getWidth() - x - segmentWidth)) + x) + getPadding());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Problem with editing file: " + getPath(), e);
        }
    }

    public void getSegmentIntoStream(int x, int y, int segmentWidth, int segmentHeight, OutputStream outputStream)
            throws IOException {
        LittleEndianOutputStream stream = new LittleEndianOutputStream(new BufferedOutputStream(outputStream));
        initHeaderIntoStream(stream, segmentWidth, segmentHeight);
        int rowLength = (segmentWidth * bytePerPixel * 8 + 31) / 32 * 4;
        int segmentPadding = rowLength - segmentWidth * bytePerPixel;
        try (RandomAccessFile file = getRandomAccessFileData("r")) {
            file.skipBytes(bytePerPixel * ((getWidth() + getPadding()) * y + x));
            for (int row = y; row < Math.min(y + segmentHeight, getHeight()); row++) {
                for (int col = x; col < Math.min(x + segmentWidth, getWidth()); col++) {
                    for (int i = 0; i < bytePerPixel; i++) {
                        stream.write(file.read());
                    }
                }
                for (int i = 0; i < bytePerPixel * ((x + segmentWidth - getWidth())); i++) {
                    stream.write(0);
                }
                for (int i = 0; i < segmentPadding; i++) {
                    stream.write(0);
                }
                file.skipBytes(bytePerPixel * (Math.max(0, (getWidth() - x - segmentWidth)) + x) + getPadding());
            }
            for (int row = Math.min(y + segmentHeight, getHeight()); row < y + segmentHeight; row++) {
                for (int col = x; col < x + segmentWidth; col++) {
                    for (int i = 0; i < bytePerPixel; i++) {
                        stream.write(0);
                    }
                }
                for (int col = 0; col < segmentPadding; col++) {
                    stream.write(0);
                }
            }
            stream.flush();
        } catch (IOException e) {
            throw new IOException("Problem with reading file: " + getPath(), e);
        }
    }

}
