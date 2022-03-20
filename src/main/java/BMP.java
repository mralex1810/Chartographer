import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class BMP {
    protected final static int bytePerPixel = 3;
    private static final int dataOffset = 54;
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

        try (OutputStream writer = new BufferedOutputStream(new FileOutputStream(picture))) {
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

    public static long sizeOfBmp(int width, int height) {
        return dataOffset + (((long) width * bytePerPixel * 8 + 31) / 32 * 4) * height;
    }

    public static void initHeaderIntoStream(OutputStream writer, int width, int height) throws IOException {
        writeCharIntoStream(0x4D42, writer); // signature
//        writer.writeInt(Long.sizeOfBmp(width, height)); // filesize
        writeLongAsUnsignedIntIntoStream(sizeOfBmp(width, height), writer);
        for (int i = 0; i < 4; i++) {
            writer.write(0); // reserved
        }
        writeIntIntoStream(dataOffset, writer); // pixels
        writeIntIntoStream(40, writer); // size of BITMAP-INFO
        writeIntIntoStream(width, writer);
        writeIntIntoStream(-height, writer);
        writeCharIntoStream(1, writer); // const 1
        writeCharIntoStream(24, writer); // size of pixel
        writeIntIntoStream(0, writer); // compress method
        writeIntIntoStream(0, writer); // size of data
        writeIntIntoStream(96, writer); // resolution
        writeIntIntoStream(96, writer); // resolution
        writeIntIntoStream(0, writer); // size of color table
        writeIntIntoStream(0, writer); // size of color table
    }

    private static void writeIntIntoStream(int value, OutputStream writer) throws IOException { //Little endian
        writer.write(value);
        writer.write(value >> 8);
        writer.write(value >> 16);
        writer.write(value >> 24);
    }

    private static void writeCharIntoStream(int value, OutputStream writer) throws IOException { //Little endian
        writer.write(value);
        writer.write(value >> 8);
    }

    private static void writeLongAsUnsignedIntIntoStream(long value, OutputStream writer) throws IOException { //Little endian
        writer.write((int) value);
        writer.write((int) (value >> 8));
        writer.write((int) (value >> 16));
        writer.write((int) (value >> 24));
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
