import com.macfaq.io.LittleEndianOutputStream;

import java.io.*;

public class Chart extends BMP {

    public Chart(File picture, int width, int height) throws IOException {
        super(picture, width, height);
    }

    public void updateSegmentFromStream(int x, int y, int segmentWidth, int segmentHeight, InputStream fragment) throws IOException {
        boolean bmpReverseMode = checkMode(fragment);
        int rowLength = (segmentWidth * bytePerPixel * 8 + 31) / 32 * 4;
        int segmentPadding = rowLength - segmentWidth * bytePerPixel;
        int startRow = Math.max(y, 0);
        int lastRow = Math.min(y + segmentHeight, getHeight()) - 1;
        int startCol = Math.max(x, 0);
        int lastCol = Math.min(x + segmentWidth, getWidth()) - 1;
        try (RandomAccessFile file = getRandomAccessFileData("rw")) {
            if (bmpReverseMode) {
                file.skipBytes((bytePerPixel * getWidth() + getPadding()) * lastRow + bytePerPixel * startCol);
                fragment.readNBytes((bytePerPixel * segmentWidth + segmentPadding) * (y + segmentHeight - 1 - lastRow));
                for (int row = lastRow; row > startRow; row--) {
                    readRow(x, fragment, segmentPadding, startCol, lastCol, segmentWidth, file);
                    file.seek(file.getFilePointer() - (long) bytePerPixel * (lastCol - startCol + getWidth() + 1) - getPadding());
                }
                readRow(x, fragment, segmentPadding, startCol, lastCol, segmentWidth, file);
            } else {
                file.skipBytes((bytePerPixel * getWidth() + getPadding()) * startRow + bytePerPixel * startCol);
                fragment.readNBytes((bytePerPixel * (segmentWidth) + segmentPadding) * (startRow - y));
                for (int row = startRow; row <= lastRow; row++) {
                    readRow(x, fragment, segmentPadding, startCol, lastCol, segmentWidth, file);
                    file.skipBytes(bytePerPixel * (-lastCol + startCol + getWidth() - 1) + getPadding());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Problem with editing file: " + getPath(), e);
        }
    }

    private void readRow(int x, InputStream fragment, int segmentPadding, int startCol, int lastCol, int segmentWeight, RandomAccessFile file) throws IOException {
        fragment.skip((long) (startCol - x) * bytePerPixel);
        for (int col = startCol; col <= lastCol; col++) {
            for (int i = 0; i < bytePerPixel; i++) {
                int b = fragment.read();
                file.write(b);
            }
        }
        fragment.skip((long) bytePerPixel * Math.max(x + segmentWeight - lastCol - 1, 0));
        fragment.skip(segmentPadding);
    }

    private boolean checkMode(InputStream fragment) throws IOException {
        boolean bmpReverseMode = false;
        fragment.readNBytes(10);
        int offset = fragment.read() + (fragment.read() << 8) + (fragment.read() << 16) + (fragment.read() << 24);
        int bcSize = fragment.read() + (fragment.read() << 8) + (fragment.read() << 16) + (fragment.read() << 24);
        if (bcSize > 12) {
            fragment.readNBytes(4);
            int fragmentHeight = fragment.read() + (fragment.read() << 8) + (fragment.read() << 16) + (fragment.read() << 24);
            if (fragmentHeight > 0) {
                bmpReverseMode = true;
            }
            fragment.readNBytes(offset - 26);
        } else {
            bmpReverseMode = true;
            fragment.readNBytes(offset - 18);
        }
        return bmpReverseMode;
    }

    public void getSegmentIntoStream(int x, int y, int segmentWidth, int segmentHeight, OutputStream outputStream) 
            throws IOException {
        LittleEndianOutputStream stream = new LittleEndianOutputStream(new BufferedOutputStream(outputStream));
        initHeaderIntoStream(stream, segmentWidth, segmentHeight);
        int rowLength = (segmentWidth * bytePerPixel * 8 + 31) / 32 * 4;
        int segmentPadding = rowLength - segmentWidth * bytePerPixel;
        int startRow = Math.max(y, 0);
        int lastRow = Math.min(y + segmentHeight, getHeight()) - 1;
        int startCol = Math.max(x, 0);
        int lastCol = Math.min(x + segmentWidth, getWidth()) - 1;
        try (RandomAccessFile file = getRandomAccessFileData("r")) {
            file.skipBytes(bytePerPixel * ((getWidth() + getPadding()) * y + x));
            for (int row = y; row < startRow; row++) {
                skipRow(x, segmentWidth, stream, segmentPadding);
            }
            for (int row = startRow; row <= lastRow; row++) {
                for (int col = x; col < startCol; col++) {
                    for (int i = 0; i < bytePerPixel; i++) {
                        stream.write(0);
                    }
                }
                for (int col = startCol; col <= lastCol; col++) {
                    for (int i = 0; i < bytePerPixel; i++) {
                        stream.write(file.read());
                    }
                }
                for (int col = lastCol + 1; col < x + segmentWidth; col++) {
                    for (int i = 0; i < bytePerPixel; i++) {
                        stream.write(0);
                    }
                }
                for (int i = 0; i < segmentPadding; i++) {
                    stream.write(0);
                }
                file.skipBytes(bytePerPixel * (Math.max(0, (getWidth() - x - segmentWidth)) + x) + getPadding());
            }
            for (int row = lastRow + 1; row < y + segmentHeight; row++) {
                skipRow(x, segmentWidth, stream, segmentPadding);
            }
            stream.flush();
        } catch (IOException e) {
            throw new IOException("Problem with reading file: " + getPath(), e);
        }
    }

    private void skipRow(int x, int segmentWidth, LittleEndianOutputStream stream, int segmentPadding) throws IOException {
        for (int col = x; col < x + segmentWidth; col++) {
            for (int i = 0; i < bytePerPixel; i++) {
                stream.write(0);
            }
        }
        for (int col = 0; col < segmentPadding; col++) {
            stream.write(0);
        }
    }


}
