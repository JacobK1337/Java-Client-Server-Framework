package client;

import message.MessageFactory;
import message.MessageHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class Client <MessageType> {
    protected SocketChannel clientSocket;
    protected ByteBuffer sentMessageBuffer;
    protected ByteBuffer receivedMessageBuffer;
    protected Thread messageReaderThread;
    protected volatile boolean running = false;
    protected MessageHandler<MessageType> messageHandler;
    protected MessageFactory<MessageType> messageFactory;
    protected int MAX_BUFFER_CAPACITY = 10000;
    protected int MAX_BANDWIDTH = 1024;

    public Client(String address, int port,
                  MessageHandler<MessageType> messageHandler,
                  MessageFactory<MessageType> messageFactory) throws IOException {

        clientSocket = SocketChannel.open(new InetSocketAddress(address, port));

        receivedMessageBuffer = ByteBuffer.allocate(MAX_BUFFER_CAPACITY);
        sentMessageBuffer = ByteBuffer.allocate(MAX_BUFFER_CAPACITY);

        this.messageHandler = messageHandler;
        this.messageFactory = messageFactory;
        running = true;

        runMessageReaderThread();
    }

    protected void runMessageReaderThread() {

        messageReaderThread = new Thread(() -> {
            try {
                while (running) {
                    listenForMessages();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

        messageReaderThread.start();
    }

    public void disconnect() throws IOException, InterruptedException {
        clientSocket.close();
        running = false;
        sentMessageBuffer = null;
        messageReaderThread.join();
    }

    protected abstract void listenForMessages() throws IOException, ClassNotFoundException;
}
