package ClientServerIntegration;

import TestMessageType.TestMessageType;
import server.Server;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class TestServerImpl extends Server<TestMessageType> {
    public TestServerImpl(int port) throws IOException {
        super(port);
    }

    @Override
    protected void asyncWriteMessage() throws IOException, InterruptedException {

    }

    @Override
    protected void asyncReadMessage() {

    }

    @Override
    protected void handleMessage(SelectionKey key) throws IOException, ClassNotFoundException {

    }
}
