import com.macfaq.io.LittleEndianOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class BMP {
    private static final int dataOffset = 54;
    protected final static int bytePerPixel = 3;
    private final int width;
    private final int height;
    private final File picture;
    private final int padding;


    public BMP(File picture, int width, int height) throws IOException {
        this(picture, width, height, 0, 0, 0);
    }

    public BMP(File picture, int width, int height, int red, int green, int blue) throws IOException {
        this.picture = picture;
        this.width = width;
        this.height = height;
        this.padding = (width * bytePerPixel * 8 + 31) / 32 * 4 - width * 3;

        try (LittleEndianOutputStream writer = new LittleEndianOutputStream(
                new BufferedOutputStream(new FileOutputStream(picture))
        )) {
            initHeaderIntoStream(writer, width, height);
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    writer.write(blue);
                    writer.write(green);
                    writer.write(red);
                }
                for (int i = 0; i < padding; i++) {
                    writer.write(0);
                }
            }
        }
    }

    public static void initHeaderIntoStream(LittleEndianOutputStream writer, int width, int height) throws IOException {
        writer.writeChar(0x4D42); // signature
        writer.writeInt(sizeOfBmp(width, height)); // filesize
        for (int i = 0; i < 4; i++) {
            writer.writeByte(0); // reserved
        }
        writer.writeInt(dataOffset); // pixels
        writer.writeInt(40); // size of BITMAP-INFO
        writer.writeInt(width);
        writer.writeInt(-height);
        writer.writeChar(1); // const 1
        writer.writeChar(24); // size of pixel
        writer.writeInt(0); // compress method
        writer.writeInt(0); // size of data
        writer.writeInt(96); // resolution
        writer.writeInt(96); // resolution
        writer.writeInt(0); // size of color table
        writer.writeInt(0); // size of color table
    }

    public static int sizeOfBmp(int width, int height) {
        return dataOffset + ((width * bytePerPixel * 8 + 31) / 32 * 4) * height;
    }

    public RandomAccessFile getRandomAccessFileData(String mode) throws IOException {
        RandomAccessFile file = new RandomAccessFile(picture, mode);
        file.seek(dataOffset);
        return file;
    }

    public String getPath() {
        return picture.getPath();
    }

    public void delete() throws IOException {
        try {
            Files.delete(Path.of(picture.getPath()));
        } catch (IOException e) {
            throw new IOException("Problem with deleting file: " + getPath(), e);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPadding() {
        return padding;
    }
}
