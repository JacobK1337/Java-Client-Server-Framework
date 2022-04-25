package project_utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class RemoteFile {

    private final File localFile;
    private long remainingBytes;
    private FileOutputStream outputStream;
    private SocketChannel sender;

    public RemoteFile(String fileAddress, SocketChannel sender, long fileSize) throws IOException {

        localFile = new File(fileAddress);

        localFile.createNewFile();

        outputStream = new FileOutputStream(localFile, false);

        remainingBytes = fileSize;

        this.sender = sender;
    }

    public void writeBytesToFile(byte[] bytes) throws IOException {

        remainingBytes -= bytes.length;

        outputStream.write(bytes);

    }

    public long getRemainingBytes(){
        return remainingBytes;
    }

    public void close() throws IOException {
        outputStream.close();
    }
}
