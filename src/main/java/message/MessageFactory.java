package message;

import java.util.List;

public interface MessageFactory<MessageType> {
    public Message<MessageType> constructMessage(MessageType messageType);
    public Message<MessageType> constructMessage(MessageType messageType, List<?> objects);
}
