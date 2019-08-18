package lzw;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import static lzw.Converters.*;

import static lzw.FileUtilities.*;

public class Packer {

    private static final int MAX_TABLE_SIZE = 65535;
    private static final int CODE_LENGTH = 16;

    private Map<String, String> cTable;

    public Packer() {
        cTable = new HashMap<>();
    }

    public void pack(File inputFile) throws Exception {
        //Проверяем переданный файл
        checkFile(inputFile);

        //Инициализируем кодовую таблицу
        initCodeTable();

        //Получаем имя для выходного файла
        File outputFile = createOutputFile(inputFile);

        try (FileChannel inputChannel = new FileInputStream(inputFile).getChannel();
             FileChannel outputChannel = new FileOutputStream(outputFile).getChannel()) {

            BufferReader reader = new BufferReader(inputChannel);
            BufferWriter writer = new BufferWriter(outputChannel);

            //Формируем заголовок файла
            createArchiveHeader(writer, getFileExtension(inputFile));

            String w = "";      //Последовательность на прошедшей итерации
            String wc;          //Текущая последовательность
            String c;           //Текущий символ

            StringBuffer buffer = new StringBuffer();
            String nextCode;

            while ((c = reader.get()) != null) {
                wc = w + c;
                if (cTable.get(wc) != null) {
                    w = wc;
                    continue;
                } else {
                    buffer.append(cTable.get(w));
                    while (buffer.length() >= 8) {
                        writer.put(buffer.substring(0, 8));
                        buffer.delete(0, 8);
                    }
                    w = c;
                    if (cTable.size() < MAX_TABLE_SIZE) {
                        nextCode = Integer.toBinaryString(cTable.size());
                        while (nextCode.length() < CODE_LENGTH) {
                            nextCode = "0" + nextCode;
                        }
                        nextCode = nextCode.substring(nextCode.length() - CODE_LENGTH);
                        cTable.put(wc, nextCode);
                    }
                }
            }

            buffer.append(cTable.get(w));
            while (buffer.length() >= 8) {
                writer.put(buffer.substring(0, 8));
                buffer.delete(0, 8);
            }
            writer.forceWrite();

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
            value = key;
            while (value.length() < CODE_LENGTH) {
                value = "0" + value;
            }
            cTable.put(key, value);
        }
    }

    private File createOutputFile(File inputFile) {
        return new File(inputFile.getParent(), getFileName(inputFile) + ".lzw");
    }

    private void createArchiveHeader(BufferWriter writer, String fileExtension) throws IOException {
        int extensionLength = fileExtension.getBytes().length;
        writer.put(convertByteToString((byte) extensionLength));

        if (extensionLength > 0) {
            for (byte b : fileExtension.getBytes()) {
                writer.put(b);
            }
        }
    }

}
