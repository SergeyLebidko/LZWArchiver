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

    private static final int CODE_LENGTH = 16;
    private static final int MAX_TABLE_SIZE = (int) (Math.pow(2, CODE_LENGTH));

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

                //Начинаем распаковку
                String code;             //Текущий прочитанный код
                String v;                //Текущая раскодированная последовательность байт
                String pv = null;        //Последовательность байт, раскодированная на предыдущем шаге
                String key;              //

                String b;                                    //Временная переменная для хранения отдельных байтов
                StringBuffer buffer = new StringBuffer();    //Буфер для временного хранения последовательностей байтов

                while (true) {

                    while (true) {
                        b = reader.get();
                        if (b == null) break;

                        buffer.append(b);
                        if (buffer.length() >= CODE_LENGTH) break;
                    }
                    if (buffer.length() < CODE_LENGTH) break;

                    code = buffer.substring(0, CODE_LENGTH);
                    buffer.delete(0, CODE_LENGTH);

                    v = cTable.get(code);

                    if (v == null) {
                        v = pv + pv.substring(0, 8);
                    }

                    for (int i = 0; i < v.length(); i += 8) {
                        writer.put(v.substring(i, i + 8));
                    }

                    if (pv != null & cTable.size() < MAX_TABLE_SIZE) {
                        key = Integer.toBinaryString(cTable.size());
                        while (key.length() < CODE_LENGTH) {
                            key = "0" + key;
                        }
                        key = key.substring(key.length() - CODE_LENGTH);
                        cTable.put(key, pv + v.substring(0, 8));
                    }

                    pv = v;
                }

                writer.forceWrite();
            } catch (IOException ex) {
                throw ex;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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

    private void showCTable() {
        int number = 0;
        for (String key : cTable.keySet()) {
            System.out.print(String.format("%-6s", number) + ". " + key + " == " + cTable.get(key));
            if (number < 256) {
                System.out.print(" == " + (char) convertStringToByte(cTable.get(key)));
            }
            System.out.println();
            number++;
        }
    }

}
