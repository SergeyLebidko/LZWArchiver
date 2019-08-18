package lzw;

public class Converters {

    //Метод возвращает строковое представление байта. Если передан null, то возвращает также null
    public static String convertByteToString(Byte b) {
        if (b == null) return null;
        String byteString = Integer.toBinaryString(b);
        while (byteString.length() < 8) {
            byteString = "0" + byteString;
        }
        byteString = byteString.substring(byteString.length() - 8);
        return byteString;
    }

    //Метод преобразовывает восьмисимвольную строку из символов 0 и 1 в соответствующее ей значение типа byte
    public static byte convertStringToByte(String str) {
        int result = 0;
        int mul = 1;
        for (int i = (str.length() - 1); i >= 0; i--) {
            if (str.charAt(i) == '1') {
                result = result | mul;
            }
            mul *= 2;
        }
        return (byte) result;
    }

}
