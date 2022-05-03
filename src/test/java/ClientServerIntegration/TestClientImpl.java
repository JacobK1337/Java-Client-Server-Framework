package ClientServerIntegration;

import TestMessageType.TestMessageType;
import client.Client;
import message.Message;

import java.io.IOException;

public class TestClientImpl extends Client<TestMessageType> {
    public TestClientImpl(String address, int port) throws IOException {
        super(address, port);
    }

    @Override
    protected void asyncWriteMessage() {

    }

    @Override
    protected void asyncReadMessage() throws IOException, ClassNotFoundException {

    }

    @Override
    protected void handleMessage(Message<TestMessageType> message) throws IOException, ClassNotFoundException {

    }
}
