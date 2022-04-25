import java.io.IOException;

public class ServerApp {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        var serverInstance = new ServerImpl(5454);
        boolean running = true;
        while(running){
            serverInstance.listenForMessages();
        }
    }
}
