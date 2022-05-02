package client;

import message.Message;
import message.MessageFactory;
import message.MessageHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Client<MessageType> {
    protected SocketChannel clientSocket;
    protected final ByteBuffer sentMessageBuffer;
    protected final ByteBuffer receivedMessageBuffer;
    protected final ByteBuffer messageSizeBuffer;
    protected MessageHandler<MessageType> messageHandler;
    protected MessageFactory<MessageType> messageFactory;
    protected Thread asyncProcessingThread;
    protected int MAX_BUFFER_CAPACITY = 10000;
    protected int MAX_BANDWIDTH = 1024;
    protected final int MESSAGE_HEADER_SIZE = 4;
    protected AtomicBoolean running = new AtomicBoolean(false);

    public Client(String address, int port,
                  MessageHandler<MessageType> messageHandler,
                  MessageFactory<MessageType> messageFactory) throws IOException {

        this.clientSocket = SocketChannel.open(new InetSocketAddress(address, port));
        this.receivedMessageBuffer = ByteBuffer.allocate(MAX_BUFFER_CAPACITY);
        this.sentMessageBuffer = ByteBuffer.allocate(MAX_BUFFER_CAPACITY);
        this.messageSizeBuffer = ByteBuffer.allocate(MESSAGE_HEADER_SIZE);
        this.messageHandler = messageHandler;
        this.messageFactory = messageFactory;

        running.set(true);
    }

    protected void startAsyncProcessing() {
        asyncProcessingThread = new Thread(() -> {
            while (running.get()) {
                try {
                    asyncReadMessage();
                    asyncWriteMessage();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        asyncProcessingThread.start();
    }

    protected Message<MessageType> readMessage() throws IOException, ClassNotFoundException {
        var messageSize = messageHandler.readMessageSize(clientSocket, messageSizeBuffer);
        receivedMessageBuffer.limit(messageSize);

        return messageHandler.readMessage(clientSocket, receivedMessageBuffer);
    }

    public void disconnect() throws IOException, InterruptedException {
        clientSocket.close();
        running.set(false);
        asyncProcessingThread.join();
    }

    protected abstract void asyncWriteMessage();

    protected abstract void asyncReadMessage() throws IOException, ClassNotFoundException;

    protected abstract void handleMessage(Message<MessageType> message) throws IOException, ClassNotFoundException;
}
