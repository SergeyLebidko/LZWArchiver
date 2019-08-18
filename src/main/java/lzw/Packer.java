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

public class Packer {

    private static final int MAX_TABLE_SIZE = 65535;

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

            String w = "";      //Прошлая последовательность
            String wc = "";     //Текущая последовательность
            String c;           //Текущий символ

            StringBuffer buffer = new StringBuffer();
            String nextCode;

            while ((c = reader.read()) != null) {
                wc = w + c;
                if (cTable.get(wc) != null) {
                    w = wc;
                    continue;
                } else {
                    buffer.append(cTable.get(w));
                    while (buffer.length() >= 8) {
                        writer.write(buffer.substring(0, 8));
                        buffer.delete(0, 8);
                    }
                    w = c;
                    if (cTable.size() < MAX_TABLE_SIZE) {
                        nextCode = "000000000000000000000000" + Integer.toBinaryString(cTable.size());
                        nextCode = nextCode.substring(nextCode.length() - 8, nextCode.length());
                        cTable.put(wc, nextCode);
                    }
                }
            }

            buffer.append(cTable.get(w));
//            if (buffer.length() % 12 != 0) {
//                buffer.append("0000");
//            }
            while (buffer.length() >= 8) {
                writer.write(buffer.substring(0, 8));
                buffer.delete(0, 8);
            }
            writer.forceWrite();

            System.out.println("Размер таблицы: " + cTable.size());

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
            value = "00000000" + key;
            cTable.put(key, value);
        }
    }

    private File createOutputFile(File inputFile) {
        return new File(inputFile.getParent(), getFileName(inputFile) + ".lzw");
    }

}
