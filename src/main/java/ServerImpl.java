import custom_implementations.CustomMessageType;
import custom_implementations.MessageFactoryImpl;
import custom_implementations.MessageHandlerImpl;
import project_utils.FileInfo;
import project_utils.LocalFile;
import message.Message;
import server.Server;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerImpl extends Server<CustomMessageType> {

    ByteBuffer messageSizeBuffer = ByteBuffer.allocate(4);


    public ServerImpl(int port) throws IOException {
        super(port, new MessageFactoryImpl(), new MessageHandlerImpl());
    }

    @Override
    protected void sendDataToClients() throws IOException {
        for (var file : requestedFiles) {
            System.out.println("Remaining bytes: " + file.getRemainingBytes());
            var fileBytesMessage =
                    messageFactory.constructMessage(
                            CustomMessageType.DOWNLOAD_FILE,
                            List.of(file.getClientFileToken(), file.readBytesFromFile(MAX_BANDWIDTH))
                    );

            messageHandler.writeMessage(fileBytesMessage, file.getReceiver(), sentMessageBuffer);
        }

        requestedFiles.removeIf(localFile -> localFile.getRemainingBytes() <= 0 || !localFile.getReceiver().isOpen());
    }

    @Override
    protected void handleMessage(SelectionKey key) throws IOException, ClassNotFoundException {
        requestedFilesEmptyLock.lock();
        var client = (SocketChannel) key.channel();

        client.read(messageSizeBuffer);
        messageSizeBuffer.rewind();

        var messageSize = messageSizeBuffer.getInt();
        messageSizeBuffer.rewind();

        System.out.println("Received message size: " + messageSize);
        receivedMessageBuffer.limit(messageSize);

        var receivedRequest = messageHandler.readMessage(client, receivedMessageBuffer);
        receivedMessageBuffer.clear();

        switch (receivedRequest.getMessageType()) {
            case DOWNLOAD_FILE -> handleDownloadRequest(receivedRequest, client);
            case CHANGE_DIRECTORY -> handleChangeDirectoryRequest(receivedRequest, client);
            case DELETE_FILE -> handleDeleteRequest(receivedRequest, client);
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

        var filesInDirectoryResponse =
                messageFactory.constructMessage(CustomMessageType.CHANGE_DIRECTORY, filesInRequestedDirectory);

        messageHandler.writeMessage(filesInDirectoryResponse, client, sentMessageBuffer);
    }

    private void handleDeleteRequest(Message<CustomMessageType> message, SocketChannel client) throws IOException {
        var clientServerDirectory = (String) message.extractFromBuffer();
        var fileToDeleteName = (String) message.extractFromBuffer();
        var fileBeingDeleted = new File(defaultServerPath + clientServerDirectory + fileToDeleteName);

        if (fileBeingDeleted.delete()) {
            var fileDeletedResponse =
                    messageFactory.constructMessage(CustomMessageType.REQUEST_ACCEPT, List.of("File successfully deleted."));

            messageHandler.writeMessage(fileDeletedResponse, client, sentMessageBuffer);
        }

        else {
            var noSuchFileResponse =
                    messageFactory.constructMessage(CustomMessageType.REQUEST_ACCEPT, List.of("Couldn't delete file."));

            messageHandler.writeMessage(noSuchFileResponse, client, sentMessageBuffer);
        }
    }

}
