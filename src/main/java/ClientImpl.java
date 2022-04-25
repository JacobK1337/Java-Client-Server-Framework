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
    private  final String defaultClientPath = "C:\\Users\\kubcz\\Desktop\\ClientFiles\\";
    private  String currentServerPath = "";
    private final Map<Long, RemoteFile> requestedFiles = new HashMap<>();
    private List<FileInfo> filesInServerDirectory;

    public ClientImpl(String address, int port) throws IOException {
        super(address, port, new MessageHandlerImpl(), new MessageFactoryImpl());

        filesInServerDirectory = new ArrayList<>();
        var getServerFilesRequest =
                messageFactory.constructMessage(CustomMessageType.CHANGE_DIRECTORY, List.of(currentServerPath));

        messageHandler.writeMessage(getServerFilesRequest, clientSocket, requestBuffer);
    }

    public void addToRequestedFiles(FileInfo serverFile) throws IOException {
        requestedFiles.put(requestedFilesCounter++,
                new RemoteFile(
                        defaultClientPath + serverFile.getFileName(),
                        null,
                        serverFile.getFileSize())
        );
    }


    //publicly accessible for outside bababoeys
    public void sendRequest(Message<CustomMessageType> message) throws IOException {
        messageHandler.writeMessage(message, clientSocket, requestBuffer);
    }

    @Override
    protected void listenForMessages() throws IOException, ClassNotFoundException {
        var receivedResponse = messageHandler.readMessage(clientSocket, responseBuffer);
        switch (receivedResponse.getMessageType()) {
            case DOWNLOAD_FILE -> handleDownloadResponse(receivedResponse);
            case CHANGE_DIRECTORY -> handleChangeDirectoryResponse(receivedResponse);
        }
    }

    private void handleDownloadResponse(Message<CustomMessageType> message) throws IOException {

        var fileId = (Long) message.extractFromBuffer();
        var receivedBytes = (byte[]) message.extractFromBuffer();

        var currentFile = requestedFiles.get(fileId);

        currentFile.writeBytesToFile(receivedBytes);
        System.out.println("Remaining bytes: " + currentFile.getRemainingBytes());

        if (currentFile.getRemainingBytes() <= 0) {
            requestedFiles.get(fileId).close();
            requestedFiles.remove(fileId);
        }
    }

    private void handleChangeDirectoryResponse(Message<CustomMessageType> message) {
        filesInServerDirectory = message.extractAllFromBuffer()
                .stream()
                .map(obj -> (FileInfo) obj).collect(Collectors.toList());
    }


    public long getRequestedFilesCounter() {
        return requestedFilesCounter;
    }

    public List<FileInfo> getFilesInServerDirectory() {
        return filesInServerDirectory;
    }
}
