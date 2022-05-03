package ClientServerIntegration;

import TestMessageType.TestMessageType;
import client.Client;
import org.junit.AfterClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

class ClientServerIntegrationTest {
    private static Client<TestMessageType> underTestClient;
    private static Server<TestMessageType> underTestServer;
    private static SocketChannel clientSocketAcceptedByServer;
    private static int port = 5454;
    private static boolean isSetup = false;

    @BeforeEach
    public void establishConnections() throws IOException {
        if (isSetup) return;
        underTestServer = new TestServerImpl(port);
        underTestClient = new TestClientImpl("localhost", port);
        clientSocketAcceptedByServer = underTestServer.acceptClient();
        isSetup = true;
    }

    @AfterClass
    public static void closeConnections() throws IOException, InterruptedException {
        underTestClient.disconnect();
        underTestServer.disconnect();
    }

    @Test
    void serverShouldReadWrittenMessage() throws IOException, ClassNotFoundException {
        //given
        var testMsg =
                underTestServer.constructMessage(TestMessageType.RESPONSE, List.of("blabla", 12));
        //when
        underTestClient.writeMessage(testMsg);

        //expected
        var receivedInServerMsg =
                underTestServer.readMessage(clientSocketAcceptedByServer);

        var receivedStr = (String) receivedInServerMsg.extractFromBuffer();
        var receivedInt = (Integer) receivedInServerMsg.extractFromBuffer();

        Assertions.assertEquals(receivedStr, "blabla");
        Assertions.assertEquals(12, (int) receivedInt);
    }

    @Test
    void clientShouldReadWrittenMessage() throws IOException, ClassNotFoundException {
        //given
        var testMsg =
                underTestServer.constructMessage(TestMessageType.RESPONSE, List.of("blabla", 12));

        //when
        underTestServer.writeMessage(testMsg, clientSocketAcceptedByServer);

        //expected
        var receivedInClientMsg =
                underTestClient.readMessage();

        var receivedStr = (String) receivedInClientMsg.extractFromBuffer();
        var receivedInt = (Integer) receivedInClientMsg.extractFromBuffer();

        Assertions.assertEquals(receivedStr, "blabla");
        Assertions.assertEquals(12, (int) receivedInt);
    }

}