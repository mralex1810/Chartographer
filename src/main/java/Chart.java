import java.io.*;

public class Chart extends BMP {

    public Chart(File picture, int width, int height) throws IOException {
        super(picture, width, height);
    }

    public void updateSegmentFromStream(int x, int y, int segmentWidth, int segmentHeight, InputStream fragment)
            throws IOException {
        boolean bmpReverseMode = checkModeAndSkipHeader(fragment);
        int rowLength = (segmentWidth * bytePerPixel * 8 + 31) / 32 * 4;
        int segmentPadding = rowLength - segmentWidth * bytePerPixel;
        int startRow = Math.max(y, 0);
        int lastRow = Math.min(y + segmentHeight, getHeight()) - 1;
        int startCol = Math.max(x, 0);
        int lastCol = Math.min(x + segmentWidth, getWidth()) - 1;
        try (RandomAccessFile file = getRandomAccessFileData("rw")) {
            if (bmpReverseMode) {
                file.skipBytes((bytePerPixel * getWidth() + getPadding()) * lastRow + bytePerPixel * startCol);
                fragment.readNBytes((bytePerPixel * segmentWidth + segmentPadding) *
                        (y + segmentHeight - 1 - lastRow));
                for (int row = lastRow; row > startRow; row--) {
                    readRow(x, fragment, segmentPadding, startCol, lastCol, segmentWidth, file);
                    file.seek(file.getFilePointer() -
                            (long) bytePerPixel * (lastCol - startCol + getWidth() + 1) - getPadding());
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

    private void readRow(int x, InputStream fragment, int segmentPadding, int startCol, int lastCol, int segmentWeight,
                         RandomAccessFile file) throws IOException {
        fragment.readNBytes((startCol - x) * bytePerPixel);
        for (int col = startCol; col <= lastCol; col++) {
            for (int i = 0; i < bytePerPixel; i++) {
                int b = fragment.read();
                file.write(b);
            }
        }
        fragment.readNBytes(bytePerPixel * Math.max(x + segmentWeight - lastCol - 1, 0));
        fragment.readNBytes(segmentPadding);
    }

    private boolean checkModeAndSkipHeader(InputStream fragment) throws IOException {
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
        OutputStream stream = new BufferedOutputStream(outputStream);
        initHeaderIntoStream(stream, segmentWidth, segmentHeight);
        int rowLength = (segmentWidth * bytePerPixel * 8 + 31) / 32 * 4;
        int segmentPadding = rowLength - segmentWidth * bytePerPixel;
        int startRow = Math.max(y, 0);
        int lastRow = Math.min(y + segmentHeight, getHeight()) - 1;
        int startCol = Math.max(x, 0);
        int lastCol = Math.min(x + segmentWidth, getWidth()) - 1;
        try (RandomAccessFile file = getRandomAccessFileData("r")) {
            file.skipBytes(bytePerPixel * ((getWidth() + getPadding()) * startRow + startCol));
            for (int row = y; row < startRow; row++) {
                skipRow(segmentWidth, segmentPadding, stream);
            }
            for (int row = startRow; row <= lastRow; row++) {
                skipBytesOnWrite((startCol - x) * bytePerPixel, stream);
                for (int col = startCol; col <= lastCol; col++) {
                    for (int i = 0; i < bytePerPixel; i++) {
                        stream.write(file.read());
                    }
                }
                skipBytesOnWrite((x + segmentWidth - (lastCol + 1)) * bytePerPixel + segmentPadding, stream);
                file.skipBytes(bytePerPixel * (-lastCol + startCol + getWidth() - 1) + getPadding());
            }
            for (int row = lastRow + 1; row < y + segmentHeight; row++) {
                skipRow(segmentWidth, segmentPadding, stream);
            }
            stream.flush();
        } catch (IOException e) {
            throw new IOException("Problem with reading file: " + getPath(), e);
        }
    }

    private void skipBytesOnWrite(int size, OutputStream outputStream) throws IOException {
        for (int i = 0; i < size; i++) {
            outputStream.write(0);
        }
    }

    private void skipRow(int segmentWidth, int segmentPadding, OutputStream stream) throws IOException {
        skipBytesOnWrite(segmentWidth * bytePerPixel + segmentPadding, stream);
    }


}
