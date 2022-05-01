package message;

import client.Client;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class Message<MessageType> implements Serializable {
    private MessageType messageType;
    private Queue<Object> memBuffer;
    private Client sender;

    public Message(MessageType messageType) {
        this.messageType = messageType;
        memBuffer = new LinkedList<>();
    }

    public Message(MessageType messageType, List<?> objects){
        this.messageType = messageType;
        memBuffer = new LinkedList<>(objects);
    }

    public long getBufferSize() {
        return memBuffer.size();
    }

    public void insertToBuffer(List<?> objects) {
        memBuffer.addAll(objects);
    }

    public Object extractFromBuffer() {
        return memBuffer.poll();
    }

    public boolean isBufferEmpty() {
        return memBuffer.isEmpty();
    }

    public List<Object> extractAllFromBuffer() {
        var allObjects = memBuffer.stream().toList();
        memBuffer.clear();

        return allObjects;
    }

    public MessageType getMessageType() {
        return messageType;
    }


}