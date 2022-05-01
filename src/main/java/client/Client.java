package client;

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
    protected Thread asyncWriteMessageThread;
    protected Thread asyncReadMessageThread;
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

    protected void startAsyncWriteMessage() {
        asyncWriteMessageThread = new Thread(() -> {
            while (running.get()) {
                asyncWriteMessage();
            }
        });
        asyncWriteMessageThread.start();
    }

    protected void startAsyncReadMessage() {
        asyncReadMessageThread = new Thread(() -> {
            while (running.get()) {
                try {
                    asyncReadMessage();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        asyncReadMessageThread.start();
    }


    public void disconnect() throws IOException, InterruptedException {
        clientSocket.close();
        running.set(false);
        asyncWriteMessageThread.join();
        asyncReadMessageThread.join();
    }

    protected abstract void asyncWriteMessage();

    protected abstract void asyncReadMessage() throws IOException, ClassNotFoundException;

    protected abstract void readMessage() throws IOException, ClassNotFoundException;
}
