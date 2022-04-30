package server;

import project_utils.LocalFile;
import message.MessageFactory;
import message.MessageHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Server <MessageType> {
    protected Thread dataSendThread;
    protected Selector selector;
    protected ServerSocketChannel serverSocket;
    protected Queue<LocalFile> requestedFiles;
    protected final MessageHandler<MessageType> messageHandler;
    protected final MessageFactory<MessageType> messageFactory;
    protected ByteBuffer receivedMessageBuffer;
    protected ByteBuffer sentMessageBuffer;
    protected final String defaultServerPath = "C:\\Users\\kubcz\\Desktop\\";
    protected final Lock requestedFilesEmptyLock = new ReentrantLock();
    protected final Condition requestedFilesEmpty = requestedFilesEmptyLock.newCondition();
    protected int ID_COUNTER = 0;
    protected final int MAX_BUFFER_CAPACITY = 10000;
    protected final int MAX_BANDWIDTH = 1024;
    protected volatile boolean running = false;

    public Server(int port,
                  MessageFactory<MessageType> messageFactory,
                  MessageHandler<MessageType> messageHandler) throws IOException {
        selector = Selector.open();
        configureSocket(port);

        requestedFiles = new LinkedBlockingQueue<>();
        receivedMessageBuffer = ByteBuffer.allocate(MAX_BUFFER_CAPACITY);
        sentMessageBuffer = ByteBuffer.allocate(MAX_BUFFER_CAPACITY);

        this.messageFactory = messageFactory;
        this.messageHandler = messageHandler;

        running = true;
        runDataSendThread();
    }

    private void configureSocket(int port) throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void runDataSendThread() {
        dataSendThread = new Thread(() -> {
            try {
                while (running) {
                    requestedFilesEmptyLock.lock();

                    while (requestedFiles.isEmpty())
                        requestedFilesEmpty.await();

                    sendDataToClients();
                    requestedFilesEmptyLock.unlock();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        dataSendThread.start();
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

    private void acceptClient() throws IOException {
        var client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ, ID_COUNTER++);
    }

    protected abstract void sendDataToClients() throws IOException, InterruptedException;
    protected abstract void handleMessage(SelectionKey key) throws IOException, ClassNotFoundException;

}
