package cn.whc.shiro.couchbase.utils;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.codec.Base64;

import java.io.*;

/**
 * @author: whc
 * @Description:
 * @Date: Create in 15:45 2018/9/18
 */
public class SerializeUtils extends SerializationUtils {

    private final static int BYTE_ARRAY_OUTPUT_STREAM_SIZE = 256;

    public static String serializeToString(Serializable obj) {
        try {
            byte[] serialize = serialize(obj);
            return Base64.decodeToString(serialize);
        } catch (Exception e) {
            throw new RuntimeException("serialize session error:", e);
        }
    }

    public static String serializeToString(Object obj) {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(BYTE_ARRAY_OUTPUT_STREAM_SIZE);
        if (!(obj instanceof Serializable)) {
            throw new SerializationException("require a Serializable payload but not type of [" +
                    obj.getClass().getName() + "]");
        }
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            return Base64.encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("serialize error,object=" + obj, e);
        }
    }

    public static <T> T deserializeFromString(String base64) {
        try {
            byte[] bytes = Base64.decode(base64);
            return deserialize(bytes);
        } catch (Exception e) {
            throw new RuntimeException("deserialize session error:", e);
        }
    }
}
