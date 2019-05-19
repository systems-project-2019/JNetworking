package lib.misc;

import java.io.*;

/**
 * @author Josh Hilbert
 * Class contains static methods to convert objects to byte arrays and from byte arrays to objects
 */
public class Interpreter {
    /**
     * Converts an object to a byte array
     *
     * @param obj an object
     * @return a byte array which represents the inputted object
     * @throws IOException
     */
    public static byte[] toByteArray(Object obj) throws IOException {
        byte[] bytes = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
        return bytes;
    }

    /**
     * Converts a byte array to an Object
     *
     * @param bytes a byte array
     * @return an Object derived from the inputted byte array
     * @throws IOException
     */
    public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return obj;
    }
}