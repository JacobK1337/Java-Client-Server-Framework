import custom_implementations.CustomMessageType;
import custom_implementations.MessageFactoryImpl;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) throws IOException, InterruptedException {
        var clientInstance = new ClientImpl("localhost", 5454);

        boolean running = true;
        var scan = new Scanner(System.in);

        while(running){
            var command = scan.nextLine();

            var commandAndParam = command.split(" ");

            if(command.equals("exit")){
                running = false;
                clientInstance.disconnect();
            }
            else if(commandAndParam[0].equals("download")){
                var requestedFile =
                        clientInstance.getFilesInServerDirectory()
                        .stream()
                        .filter(serverFile -> serverFile.getFileName().equals(commandAndParam[1]))
                        .findFirst();

                var downloadRequest =
                        new MessageFactoryImpl().constructMessage(
                                CustomMessageType.DOWNLOAD_FILE,
                                List.of(clientInstance.getRequestedFilesCounter(), commandAndParam[1])
                        );

                clientInstance.addToRequestedFiles(
                        requestedFile.orElseThrow
                                (() -> new RuntimeException("No such file."))
                );

                clientInstance.sendRequest(downloadRequest);
            }
            else if(commandAndParam[0].equals("ls")){
                clientInstance
                        .getFilesInServerDirectory()
                        .forEach(serverFile -> System.out.println(serverFile.getFileName()));
            }

        }

    }
}
