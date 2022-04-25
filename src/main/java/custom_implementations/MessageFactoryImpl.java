package custom_implementations;

import message.Message;
import message.MessageFactory;

import java.util.List;

public class MessageFactoryImpl implements MessageFactory<CustomMessageType> {
    @Override
    public Message<CustomMessageType> constructMessage(CustomMessageType customMessageType) {
        return new Message<CustomMessageType>(customMessageType);
    }
    @Override
    public Message<CustomMessageType> constructMessage(CustomMessageType customMessageType, List<?> objects) {
        return new Message<CustomMessageType>(customMessageType, objects);
    }
}
