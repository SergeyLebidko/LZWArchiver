package lzw;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

import static lzw.Converters.*;

public class BufferReader {

    private static final int BUFFER_SIZE = 4096;

    private FileChannel channel;
    private ByteBuffer buffer;
    private LinkedList<Byte> list;

    public BufferReader(FileChannel channel) {
        this.channel = channel;
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        list = new LinkedList<>();
    }

    public String read() throws IOException {
        if (list.isEmpty()) {
            readFromFile();
        }
        return convertByteToString(list.pollFirst());
    }

    private void readFromFile() throws IOException {
        buffer.clear();
        int readBytes = channel.read(buffer);
        for (int i = 0; i < readBytes; i++) {
            list.add(buffer.get(i));
        }
    }

}
