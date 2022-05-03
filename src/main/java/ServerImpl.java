import message.MessageFactory;
import message.MessageHandler;
import file.FileInfo;
import file.LocalFile;
import message.Message;
import server.Server;
import java.io.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerImpl extends Server<CustomMessageType> {
    private final Queue<LocalFile> requestedFiles = new LinkedBlockingQueue<>();
    protected final String defaultServerPath = "C:\\Users\\kubcz\\Desktop\\";

    public ServerImpl(int port) throws IOException {
        super(port);
        startAsyncProcessing();
    }

    @Override
    protected void asyncReadMessage() {
    }

    @Override
    protected void asyncWriteMessage() throws IOException {
        for (var file : requestedFiles) {
            System.out.println("Remaining bytes: " + file.getRemainingBytes());

            var fileBytesMessage =
                    constructMessage(
                            CustomMessageType.DOWNLOAD_FILE,
                            List.of(file.getClientFileToken(), file.readBytesFromFile(MAX_BANDWIDTH))
                    );
            writeMessage(fileBytesMessage, file.getReceiver());
        }
        requestedFiles.removeIf(localFile -> localFile.getRemainingBytes() <= 0 || !localFile.getReceiver().isOpen());
    }

    @Override
    protected void handleMessage(SelectionKey key) throws IOException, ClassNotFoundException {
        var client = (SocketChannel) key.channel();

        var receivedRequest = readMessage(client);
        switch (receivedRequest.getMessageType()) {
            case DOWNLOAD_FILE -> handleDownloadRequest(receivedRequest, client);
            case CHANGE_DIRECTORY -> handleChangeDirectoryRequest(receivedRequest, client);
            case DELETE_FILE -> handleDeleteRequest(receivedRequest, client);
        }
    }

    private void handleDownloadRequest(Message<CustomMessageType> message, SocketChannel client) throws FileNotFoundException {
        while (!message.isBufferEmpty()) {
            var clientFileId = (Long) message.extractFromBuffer();
            var fileName = (String) message.extractFromBuffer();

            requestedFiles.add(new LocalFile(clientFileId, defaultServerPath + fileName, client));
        }
    }

    private void handleChangeDirectoryRequest(Message<CustomMessageType> message, SocketChannel client) throws IOException {
        var requestedDirectory = (String) message.extractFromBuffer();

        var filesInRequestedDirectory =
                Stream.of(new File(defaultServerPath + requestedDirectory).listFiles())
                        .map(file ->
                                new FileInfo(file.getName(),
                                        file.length(),
                                        file.isFile() ? FileInfo.FileType.FILE : FileInfo.FileType.DIR)
                        )
                        .collect(Collectors.toList());

        var filesInDirectoryResponse =
                constructMessage(CustomMessageType.CHANGE_DIRECTORY, filesInRequestedDirectory);

        writeMessage(filesInDirectoryResponse, client);
    }

    private void handleDeleteRequest(Message<CustomMessageType> message, SocketChannel client) throws IOException {
        var clientServerDirectory = (String) message.extractFromBuffer();
        var fileToDeleteName = (String) message.extractFromBuffer();
        var fileBeingDeleted = new File(defaultServerPath + clientServerDirectory + fileToDeleteName);

        if (fileBeingDeleted.delete()) {
            var fileDeletedResponse =
                    constructMessage(CustomMessageType.REQUEST_ACCEPT, List.of("File successfully deleted."));


            writeMessage(fileDeletedResponse, client);
        } else {
            var noSuchFileResponse =
                    constructMessage(CustomMessageType.REQUEST_ACCEPT, List.of("Couldn't delete file."));

            writeMessage(noSuchFileResponse, client);
        }
    }

}
