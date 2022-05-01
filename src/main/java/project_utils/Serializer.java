package project_utils;

import java.io.*;

public abstract class Serializer {
    public static byte[] serialize(Object object) throws IOException {
        var byteOutputStream = new ByteArrayOutputStream();
        var objectOut = new ObjectOutputStream(new BufferedOutputStream(byteOutputStream));
        objectOut.flush();

        byte[] objectOutBytes;
        objectOut.writeObject(object);
        objectOut.close();

        objectOutBytes = byteOutputStream.toByteArray();
        byteOutputStream.close();

        return objectOutBytes;
    }


}
