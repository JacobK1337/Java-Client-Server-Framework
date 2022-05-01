package server;

import message.MessageFactory;
import message.MessageHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Server<MessageType> {
    protected Selector selector;
    protected ServerSocketChannel serverSocket;
    protected final MessageHandler<MessageType> messageHandler;
    protected final MessageFactory<MessageType> messageFactory;
    protected final ByteBuffer receivedMessageBuffer;
    protected final ByteBuffer sentMessageBuffer;
    protected final ByteBuffer messageSizeBuffer;
    protected Thread asyncWriteMessageThread;
    protected Thread asyncReadMessageThread;
    protected int ID_COUNTER = 0;
    protected final int MAX_BUFFER_CAPACITY = 10000;
    protected final int MAX_BANDWIDTH = 1024;
    protected final int MESSAGE_HEADER_SIZE = 4;
    protected AtomicBoolean running = new AtomicBoolean(false);

    public Server(int port,
                  MessageHandler<MessageType> messageHandler,
                  MessageFactory<MessageType> messageFactory) throws IOException {
        this.selector = Selector.open();
        configureSocket(port);

        this.receivedMessageBuffer = ByteBuffer.allocate(MAX_BUFFER_CAPACITY);
        this.sentMessageBuffer = ByteBuffer.allocate(MAX_BUFFER_CAPACITY);
        this.messageSizeBuffer = ByteBuffer.allocate(MESSAGE_HEADER_SIZE);
        this.messageFactory = messageFactory;
        this.messageHandler = messageHandler;

        running.set(true);
    }

    private void configureSocket(int port) throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    protected void startAsyncWriteMessage() {
        asyncWriteMessageThread = new Thread(() -> {
            try {
                while (running.get()) {
                    asyncWriteMessage();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        asyncWriteMessageThread.start();
    }

    protected void startAsyncReadMessage() {
        asyncReadMessageThread = new Thread(() -> {
            while (running.get()) {
                asyncReadMessage();
            }
        });
        asyncReadMessageThread.start();
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
                    readMessage(key);
                }

            }
            selectorIter.remove();
        }
    }

    private void acceptClient() throws IOException {
        var client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ, ID_COUNTER++);
    }

    protected abstract void asyncWriteMessage() throws IOException, InterruptedException;

    protected abstract void asyncReadMessage();

    protected abstract void readMessage(SelectionKey key) throws IOException, ClassNotFoundException;
}
