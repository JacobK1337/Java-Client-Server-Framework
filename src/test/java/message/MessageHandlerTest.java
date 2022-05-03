package message;

import TestMessageType.TestMessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import utils.Serializer;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MessageHandlerTest {

    @Mock
    private SocketChannel socketChannel;

    private MessageHandler<TestMessageType> underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new MessageHandler<>();
    }

    @Test
    void readMessage() throws IOException, ClassNotFoundException {
        //given
        ByteBuffer writtenMsgBuff = ByteBuffer.allocate(1000);
        var testMessage = new Message<TestMessageType>(TestMessageType.REQUEST, List.of(123));
        var serializedMessage = Serializer.serialize(testMessage);

        writtenMsgBuff.limit(serializedMessage.length);
        writtenMsgBuff.put(serializedMessage);
        writtenMsgBuff.rewind();


        //mocking bytes from the socket
        //when
        var receivedMsgBuff = ByteBuffer.allocate(1000);
        when(socketChannel.read((ByteBuffer) any()))
                .thenAnswer(inv -> {
                    var args = inv.getArguments();
                    var bufferToMock = (ByteBuffer) args[0];
                    bufferToMock.clear();
                    bufferToMock.put(writtenMsgBuff.array());
                    return null;
                })
                .thenReturn(Integer.MAX_VALUE);

        //expected
        var readMessage = underTest.readMessage(socketChannel, receivedMsgBuff);

        assertEquals(readMessage.getMessageType(), TestMessageType.REQUEST);
        assertEquals((int) readMessage.extractFromBuffer(), 123);
    }

    @Test
    void readMessageSize() throws IOException {
        //given
        final int MSG_HEADER_SIZE = 4;
        ByteBuffer writtenMsgBuff = ByteBuffer.allocate(MSG_HEADER_SIZE);

        //mocking bytes from the socket
        //when
        when(socketChannel.read((ByteBuffer) any()))
                .thenAnswer(inv ->{
                    var args = inv.getArguments();
                    var bufferToMock = (ByteBuffer) args[0];
                    bufferToMock.putInt(123);
                    bufferToMock.rewind();
                    return null;
                })
                .thenReturn(0);

        //expected
        var size = underTest.readMessageSize(socketChannel, writtenMsgBuff);
        assertEquals(size, 123);
    }

    @Test
    void writeMessage() throws IOException {
        //given
        ByteBuffer writtenMsgBuff = ByteBuffer.allocate(1000);
        var testMessage = new Message<TestMessageType>(TestMessageType.REQUEST, List.of(123));
        var serializedMessage = Serializer.serialize(testMessage);
        var sizeOfMessageAndHeader = 4 + serializedMessage.length;

        //when
        when(socketChannel.write((ByteBuffer) any()))
                .thenReturn(Integer.MAX_VALUE);

        underTest.writeMessage(testMessage, socketChannel, writtenMsgBuff);

        //expected
        //limit of the buffer should be set to 4 + message size in bytes
        assertEquals(writtenMsgBuff.limit(), sizeOfMessageAndHeader);
    }
}