package message;

import utils.Serializer;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MessageHandler<MessageType> {
    public Message<MessageType> readMessage(SocketChannel socket, ByteBuffer buffer) throws IOException, ClassNotFoundException {

        int countRead = 0;
        while (countRead < buffer.limit()) {
            countRead += socket.read(buffer);
        }

        try (
                var inputStream = new ByteArrayInputStream(buffer.array());
                var inputObject = new ObjectInputStream(new BufferedInputStream(inputStream))
        ) {
            buffer.clear();
            return (Message<MessageType>) inputObject.readObject();
        }
    }

    public int readMessageSize(SocketChannel socket, ByteBuffer buffer) throws IOException {
        socket.read(buffer);
        buffer.rewind();

        var messageSize = buffer.getInt();
        buffer.rewind();

        return messageSize;

    }

    public void writeMessage(Message<MessageType> message, SocketChannel socket, ByteBuffer buffer) throws IOException {
        var serializedMessage = Serializer.serialize(message);
        final int MESSAGE_HEADER_SIZE = 4;

        buffer.limit(MESSAGE_HEADER_SIZE + serializedMessage.length);
        buffer.putInt(serializedMessage.length);
        buffer.put(serializedMessage);
        buffer.rewind();

        int countWritten = 0;
        while (countWritten < buffer.limit()) {
            countWritten += socket.write(buffer);
        }

        buffer.rewind();
    }

}
