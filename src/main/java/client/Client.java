package client;

import message.Message;
import message.MessageFactory;
import message.MessageHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Client<MessageType> {
    private final SocketChannel clientSocket;
<<<<<<< HEAD
    private final MessageHandler<MessageType> messageHandler = new MessageHandler<>();
    private final MessageFactory<MessageType> messageFactory = new MessageFactory<>();
=======
    private final MessageHandler<MessageType> messageHandler;
    private final MessageFactory<MessageType> messageFactory;
>>>>>>> 52274a7996a38a7b34dfb390a3fe398932691b14
    protected final ByteBuffer sentMessageBuffer;
    protected final ByteBuffer receivedMessageBuffer;
    protected final ByteBuffer messageSizeBuffer;
    protected Thread asyncProcessingThread;
    protected final int MAX_BUFFER_CAPACITY = 10000;
    protected final int MAX_BANDWIDTH = 1024;
    protected final int MESSAGE_HEADER_SIZE = 4;
    protected final AtomicBoolean running = new AtomicBoolean(false);
<<<<<<< HEAD
=======

    public Client(String address, int port,
                  MessageHandler<MessageType> messageHandler,
                  MessageFactory<MessageType> messageFactory) throws IOException {
>>>>>>> 52274a7996a38a7b34dfb390a3fe398932691b14

    public Client(String address, int port) throws IOException {
        this.clientSocket = SocketChannel.open(new InetSocketAddress(address, port));
        this.receivedMessageBuffer = ByteBuffer.allocate(MAX_BUFFER_CAPACITY);
        this.sentMessageBuffer = ByteBuffer.allocate(MAX_BUFFER_CAPACITY);
        this.messageSizeBuffer = ByteBuffer.allocate(MESSAGE_HEADER_SIZE);


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

    public Message<MessageType> readMessage() throws IOException, ClassNotFoundException {
        var messageSize = messageHandler.readMessageSize(clientSocket, messageSizeBuffer);
        receivedMessageBuffer.limit(messageSize);

        return messageHandler.readMessage(clientSocket, receivedMessageBuffer);
    }

    public void writeMessage(Message<MessageType> message) throws IOException {
        messageHandler.writeMessage(message, clientSocket, sentMessageBuffer);
    }

    public Message<MessageType> constructMessage(MessageType type, List<?> objects){
        return messageFactory.constructMessage(type, objects);
    }

    public void disconnect() throws IOException, InterruptedException {

        clientSocket.close();
        running.set(false);
<<<<<<< HEAD
        if(asyncProcessingThread != null)
            asyncProcessingThread.join();
    }


=======
        asyncProcessingThread.join();
    }

>>>>>>> 52274a7996a38a7b34dfb390a3fe398932691b14
    protected abstract void asyncWriteMessage();

    protected abstract void asyncReadMessage() throws IOException, ClassNotFoundException;

    protected abstract void handleMessage(Message<MessageType> message) throws IOException, ClassNotFoundException;
}
