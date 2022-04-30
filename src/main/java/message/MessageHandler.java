package message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface MessageHandler<MessageType>{
    Message<MessageType> readMessage(SocketChannel socket, ByteBuffer buffer) throws IOException, ClassNotFoundException;
    void writeMessage(Message<MessageType> message, SocketChannel socket, ByteBuffer buffer) throws IOException;

}