package lzw;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

import static lzw.Converters.*;

public class BufferWriter {

    private static final int BUFFER_SIZE = 4096;

    private FileChannel channel;
    private ByteBuffer buffer;
    private LinkedList<Byte> list;

    public BufferWriter(FileChannel channel) {
        this.channel = channel;
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        list = new LinkedList<>();
    }

    public void put(String value) throws IOException {
        list.add(convertStringToByte(value));
        if (list.size() == BUFFER_SIZE) {
            writeToFile();
        }
    }

    public void put(byte value) throws IOException {
        list.add(value);
        if (list.size() == BUFFER_SIZE) {
            writeToFile();
        }
    }

    public void forceWrite() throws IOException {
        writeToFile();
    }

    private void writeToFile() throws IOException {
        buffer.clear();
        while (!list.isEmpty()) {
            buffer.put(list.pollFirst());
        }
        buffer.flip();
        channel.write(buffer);
    }

}
