package lzw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import static lzw.Converters.*;
import static lzw.FileUtilities.*;

public class Unpacker {

    private static final int MAX_TABLE_SIZE = 65535;
    private static final int CODE_LENGTH = 16;

    private Map<String, String> cTable;
    private File outputFile;

    public Unpacker() {
        cTable = new HashMap<>();
    }

    public void unpack(File inputFile) throws Exception {
        //Инициаизируем кодовую таблицу
        initCodeTable();

        try (FileChannel inputChannel = new FileInputStream(inputFile).getChannel()) {

            BufferReader reader = new BufferReader(inputChannel);
            readHeader(reader, inputFile);

            try (FileChannel outputChannel = new FileOutputStream(outputFile).getChannel()) {

                BufferWriter writer = new BufferWriter(outputChannel);

                //Вставить код распаковки

            } catch (IOException ex) {
                throw ex;
            }
        } catch (IOException ex) {
            throw new Exception("Ошибка чтения/записи: " + ex.getMessage());
        }
    }

    private void initCodeTable() {
        cTable.clear();
        String key;
        String value;
        for (int i = 0; i < 256; i++) {
            key = convertByteToString((byte) i);
            while (key.length() < CODE_LENGTH) {
                key = "0" + key;
            }
            value = key.substring(key.length() - 8);
            cTable.put(key, value);
        }
    }

    private void readHeader(BufferReader reader, File inputFile) throws IOException {
        int extensionLength = convertStringToByte(reader.get());
        if (extensionLength == 0) {
            outputFile = new File(inputFile.getParent(), getFileName(inputFile));
            return;
        }

        byte[] extension = new byte[extensionLength];
        for (int i = 0; i < extensionLength; i++) {
            extension[i] = convertStringToByte(reader.get());
        }
        outputFile = new File(inputFile.getParent(), getFileName(inputFile) + "." + new String(extension));
    }

}
