package server;

import message.Message;
import message.MessageFactory;
import message.MessageHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Server<MessageType> {
    private final Selector selector;
    private ServerSocketChannel serverSocket;
<<<<<<< HEAD
    private final MessageHandler<MessageType> messageHandler = new MessageHandler<>();
    private final MessageFactory<MessageType> messageFactory = new MessageFactory<>();
=======
    private final MessageHandler<MessageType> messageHandler;
    private final MessageFactory<MessageType> messageFactory;
>>>>>>> 52274a7996a38a7b34dfb390a3fe398932691b14
    private Thread asyncProcessingThread;
    protected final ByteBuffer receivedMessageBuffer;
    protected final ByteBuffer sentMessageBuffer;
    protected final ByteBuffer messageSizeBuffer;
    protected int ID_COUNTER = 0;
    protected final int MAX_BUFFER_CAPACITY = 10000;
    protected final int MAX_BANDWIDTH = 1024;
    protected final int MESSAGE_HEADER_SIZE = 4;
    protected AtomicBoolean running = new AtomicBoolean(false);

<<<<<<< HEAD
    public Server(int port) throws IOException {
=======
    public Server(int port,
                  MessageHandler<MessageType> messageHandler,
                  MessageFactory<MessageType> messageFactory) throws IOException {
>>>>>>> 52274a7996a38a7b34dfb390a3fe398932691b14
        this.selector = Selector.open();
        configureSocket(port);

        this.receivedMessageBuffer = ByteBuffer.allocate(MAX_BUFFER_CAPACITY);
        this.sentMessageBuffer = ByteBuffer.allocate(MAX_BUFFER_CAPACITY);
        this.messageSizeBuffer = ByteBuffer.allocate(MESSAGE_HEADER_SIZE);

        running.set(true);
    }

    private void configureSocket(int port) throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    protected void startAsyncProcessing() {
        asyncProcessingThread = new Thread(() -> {
            try {
                while (running.get()) {
                    asyncReadMessage();
                    asyncWriteMessage();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        asyncProcessingThread.start();
    }

    public Message<MessageType> readMessage(SocketChannel client) throws IOException, ClassNotFoundException {
        var messageSize = messageHandler.readMessageSize(client, messageSizeBuffer);
        receivedMessageBuffer.limit(messageSize);

        return messageHandler.readMessage(client, receivedMessageBuffer);
    }

    public void writeMessage(Message<MessageType> message, SocketChannel client) throws IOException {
        messageHandler.writeMessage(message, client, sentMessageBuffer);
    }

    public Message<MessageType> constructMessage(MessageType type, List<?> objects){
        return messageFactory.constructMessage(type, objects);
    }

    public void listenForMessages() throws IOException, ClassNotFoundException {
        selector.select();
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> selectorIter = selectedKeys.iterator();

        while (selectorIter.hasNext()) {
            SelectionKey key = selectorIter.next();
            if (key.channel().isOpen()) {

                if (key.isAcceptable()) {
                    acceptClient();
                }
                if (key.isReadable()) {
                    handleMessage(key);
                }

            }
            selectorIter.remove();
        }
    }

    public SocketChannel acceptClient() throws IOException {
        var client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ, ID_COUNTER++);
        return client;
    }

    public void disconnect() throws IOException, InterruptedException {
        serverSocket.close();
        running.set(false);
        if(asyncProcessingThread != null)
            asyncProcessingThread.join();
    }

<<<<<<< HEAD
=======
    public void disconnect() throws IOException, InterruptedException {
        serverSocket.close();
        running.set(false);
        asyncProcessingThread.join();
    }

>>>>>>> 52274a7996a38a7b34dfb390a3fe398932691b14
    protected abstract void asyncWriteMessage() throws IOException, InterruptedException;

    protected abstract void asyncReadMessage();

    protected abstract void handleMessage(SelectionKey key) throws IOException, ClassNotFoundException;
}
