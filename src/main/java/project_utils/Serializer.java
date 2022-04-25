package project_utils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

public abstract class Serializer {
    public static byte[] serialize(Object object) {
        var byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = null;
        byte[] objectOutBytes = null;

        try {

            objectOut = new ObjectOutputStream(byteOutputStream);
            objectOut.writeObject(object);
            objectOut.flush();

            objectOutBytes = byteOutputStream.toByteArray();

            byteOutputStream.close();
            objectOut.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return objectOutBytes;
    }
}
