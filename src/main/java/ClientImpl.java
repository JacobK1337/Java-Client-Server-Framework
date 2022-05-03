import client.Client;
import file.FileInfo;
import file.RemoteFile;
import message.Message;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientImpl extends Client<CustomMessageType> {
    private long requestedFilesCounter = 0;
    private final String defaultClientPath = "C:\\Users\\kubcz\\Desktop\\ClientFiles\\";
    private String currentServerPath = "";
    private final Map<Long, RemoteFile> requestedFiles = new HashMap<>();
    private List<FileInfo> filesInServerDirectory = new ArrayList<>();

    public ClientImpl(String address, int port) throws IOException {
        super(address, port);

        var getServerFilesRequest = constructMessage(CustomMessageType.CHANGE_DIRECTORY, List.of(currentServerPath));
        writeMessage(getServerFilesRequest);
        startAsyncProcessing();
    }

    @Override
    protected void asyncWriteMessage() {
    }

    @Override
    protected void asyncReadMessage() throws IOException, ClassNotFoundException {
        var receivedMessage = readMessage();
        handleMessage(receivedMessage);
    }

    @Override
    protected void handleMessage(Message<CustomMessageType> message) throws IOException {
        switch (message.getMessageType()) {
            case DOWNLOAD_FILE -> handleDownloadResponse(message);
            case CHANGE_DIRECTORY -> handleChangeDirectoryResponse(message);
            case REQUEST_ACCEPT -> handleRequestAcceptResponse(message);
        }
    }

    public void addToRequestedFiles(FileInfo serverFile) throws IOException {
        requestedFiles.put(requestedFilesCounter++,
                new RemoteFile(
                        defaultClientPath + serverFile.getFileName(),
                        null,
                        serverFile.getFileSize())
        );
    }


    public String getCurrentServerPath() {
        return currentServerPath;
    }

    private void handleDownloadResponse(Message<CustomMessageType> message) throws IOException {
        var fileId = (Long) message.extractFromBuffer();
        var receivedBytes = (byte[]) message.extractFromBuffer();

        var currentFile = requestedFiles.get(fileId);
        currentFile.writeBytesToFile(receivedBytes);
        System.out.println("Remaining bytes: " + currentFile.getRemainingBytes());

        if (currentFile.getRemainingBytes() <= 0) {
            System.out.println("File [" + fileId + "] successfully downloaded.");
            requestedFiles.get(fileId).close();
            requestedFiles.remove(fileId);

        }
    }

    private void handleChangeDirectoryResponse(Message<CustomMessageType> message) {
        filesInServerDirectory = message.extractAllFromBuffer()
                .stream()
                .map(obj -> (FileInfo) obj).collect(Collectors.toList());
    }

    private void handleRequestAcceptResponse(Message<CustomMessageType> message) {
        var response = (String) message.extractFromBuffer();
        System.out.println(response);
    }

    public long getRequestedFilesCounter() {
        return requestedFilesCounter;
    }

    public List<FileInfo> getFilesInServerDirectory() {
        return filesInServerDirectory;
    }
}
