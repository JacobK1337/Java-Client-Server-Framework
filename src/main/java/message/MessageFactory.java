package message;

import java.util.List;

public class MessageFactory<MessageType> {
    public Message<MessageType> constructMessage(MessageType messageType){
        return new Message<MessageType>(messageType);
    }
    public Message<MessageType> constructMessage(MessageType messageType, List<?> objects){
        return new Message<MessageType>(messageType, objects);
    }
}
