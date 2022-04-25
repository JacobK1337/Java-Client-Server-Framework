import custom_implementations.CustomMessageType;
import custom_implementations.MessageFactoryImpl;
import custom_implementations.MessageHandlerImpl;
import project_utils.FileInfo;
import project_utils.LocalFile;
import message.Message;
import server.Server;
import java.io.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerImpl extends Server<CustomMessageType> {
    public ServerImpl(int port) throws IOException {
        super(port, new MessageFactoryImpl(), new MessageHandlerImpl());
    }

    @Override
    protected void sendDataToClients() throws IOException, InterruptedException {
        Thread.sleep(500);

        for (var file : requestedFiles) {

            System.out.println("Remaining bytes: " + file.getRemainingBytes());

            var bytesResponse =
                    messageFactory.constructMessage(
                            CustomMessageType.DOWNLOAD_FILE,
                            List.of(file.getClientFileToken(), file.readBytesFromFile(MAX_BANDWIDTH))
                    );

            messageHandler.writeMessage(bytesResponse, file.getReceiver(), sentMessageBuffer);
        }

        requestedFiles.removeIf(localFile -> localFile.getRemainingBytes() <= 0 || !localFile.getReceiver().isOpen());
    }

    @Override
    protected void handleMessage(SelectionKey key) throws IOException, ClassNotFoundException {
        requestedFilesEmptyLock.lock();
        var client = (SocketChannel) key.channel();
        var receivedRequest = messageHandler.readMessage(client, receivedMessageBuffer);

        switch (receivedRequest.getMessageType()) {
            case DOWNLOAD_FILE -> handleDownloadRequest(receivedRequest, client);
            case CHANGE_DIRECTORY -> handleChangeDirectoryRequest(receivedRequest, client);
            case DELETE_FILE -> System.out.println("DELETE_FILE");
        }
        requestedFilesEmptyLock.unlock();
    }


    private void handleDownloadRequest(Message<CustomMessageType> message, SocketChannel client) throws FileNotFoundException {
        while (!message.isBufferEmpty()) {
            var clientFileId = (Long) message.extractFromBuffer();
            var fileName = (String) message.extractFromBuffer();

            requestedFiles.add(new LocalFile(clientFileId, defaultServerPath + fileName, client));

            requestedFilesEmpty.signal();
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

        var serverResponse =
                messageFactory.constructMessage(CustomMessageType.CHANGE_DIRECTORY, filesInRequestedDirectory);

        messageHandler.writeMessage(serverResponse, client, sentMessageBuffer);
    }

}
