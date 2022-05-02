package file;

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
        this.localFile = new File(fileAddress);
        this.localFile.createNewFile();
        this.outputStream = new FileOutputStream(localFile, false);
        this.remainingBytes = fileSize;
        this.sender = sender;
    }

    public void writeBytesToFile(byte[] bytes) throws IOException {
        outputStream.write(bytes);
        remainingBytes -= bytes.length;
    }

    public long getRemainingBytes(){
        return remainingBytes;
    }

    public void close() throws IOException {
        outputStream.close();
    }
}
