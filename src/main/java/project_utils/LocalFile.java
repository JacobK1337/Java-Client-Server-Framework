package project_utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class LocalFile {

    private final long clientFileToken;
    private final File localFile;
    private long remainingBytes;
    private FileInputStream inputStream;
    private  SocketChannel receiver;

    public LocalFile(long clientFileToken, String localFileAddress, SocketChannel receiver) throws FileNotFoundException {

        this.clientFileToken = clientFileToken;

        localFile = new File(localFileAddress);

        inputStream = new FileInputStream(localFile);

        this.receiver = receiver;

        remainingBytes = localFile.length();
    }



    public byte[] readBytesFromFile(long size) throws IOException {
        var sizeInBytes = Math.min(size, remainingBytes);

        var readBytes = new byte[(int) sizeInBytes];

        inputStream.read(readBytes);

        remainingBytes -= sizeInBytes;

        return readBytes;
    }

    public SocketChannel getReceiver(){
        return receiver;
    }

    public long getClientFileToken(){
        return clientFileToken;
    }

    public long getRemainingBytes() {
        return remainingBytes;
    }

    public void close() throws IOException {
        inputStream.close();
    }

}
