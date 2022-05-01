import client.Client;
import custom_implementations.CustomMessageType;
import custom_implementations.MessageFactoryImpl;
import custom_implementations.MessageHandlerImpl;
import project_utils.FileInfo;
import project_utils.RemoteFile;
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
        super(address, port, new MessageHandlerImpl(), new MessageFactoryImpl());

        var getServerFilesRequest =
                messageFactory.constructMessage(CustomMessageType.CHANGE_DIRECTORY, List.of(currentServerPath));
        messageHandler.writeMessage(getServerFilesRequest, clientSocket, sentMessageBuffer);

    }

    @Override
    protected void concurrentDataTransfer(){

    }

    public void addToRequestedFiles(FileInfo serverFile) throws IOException {
        requestedFiles.put(requestedFilesCounter++,
                new RemoteFile(
                        defaultClientPath + serverFile.getFileName(),
                        null,
                        serverFile.getFileSize())
        );
    }

    public void sendRequest(Message<CustomMessageType> message) throws IOException {
        messageHandler.writeMessage(message, clientSocket, sentMessageBuffer);
    }

    public String getCurrentServerPath() {
        return currentServerPath;
    }

    @Override
    protected void readMessage() throws IOException, ClassNotFoundException {
        var messageSize = messageHandler.readMessageSize(clientSocket, messageSizeBuffer);
        receivedMessageBuffer.limit(messageSize);
        var receivedMessage = messageHandler.readMessage(clientSocket, receivedMessageBuffer);

        switch (receivedMessage.getMessageType()) {
            case DOWNLOAD_FILE -> handleDownloadResponse(receivedMessage);
            case CHANGE_DIRECTORY -> handleChangeDirectoryResponse(receivedMessage);
            case REQUEST_ACCEPT -> handleRequestAcceptResponse(receivedMessage);
        }
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
