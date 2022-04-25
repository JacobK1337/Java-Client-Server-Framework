package custom_implementations;

import message.Message;
import message.MessageHandler;
import project_utils.Serializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MessageHandlerImpl implements MessageHandler<CustomMessageType> {
    @Override
    public Message<CustomMessageType> readMessage(SocketChannel socket, ByteBuffer buffer) throws IOException, ClassNotFoundException {
        socket.read(buffer);

        var inputStream = new ByteArrayInputStream(buffer.array());
        ObjectInput inputObject = null;
        inputObject = new ObjectInputStream(inputStream);

        buffer.clear();
        return (Message<CustomMessageType>) inputObject.readObject();
    }

    @Override
    public void writeMessage(Message<CustomMessageType> message, SocketChannel socket, ByteBuffer buffer) throws IOException {
        buffer = ByteBuffer.wrap(Serializer.serialize(message));
        socket.write(buffer);
        buffer.clear();
    }
}
