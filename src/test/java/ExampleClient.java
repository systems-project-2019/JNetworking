import lib.Command;
import lib.net.JClient;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class ExampleClient extends JClient {
    public ExampleClient(String server_ip, int port, String username) {
        super(server_ip, port, username);
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        ExampleClient exampleClient = new ExampleClient("localhost", 1500, sc.next());
        exampleClient.connect();
        sc.next();
        //exampleClient.requestCommand(ExampleServer.getScore());
        exampleClient.sendTo("Hi", "Gavin", "Michael");
    }

    @Override
    public void display(String message) {
        System.out.println(message);
    }

    @Override
    public String displaySendToAllMessage(Object input, String sender) {
        return sender + ": " + input.toString();
    }

    @Override
    public String sendToSpecificClientsFormat(Object input, String sender, List<String> recipients) {
        List<String> updatedRecipients = new LinkedList<>(recipients);

        if (updatedRecipients.contains(getUsername())) {
            updatedRecipients.remove(getUsername());
            updatedRecipients.add("You");
        }

        return sender + ": --> " + updatedRecipients + ": " + input.toString();
    }

    @Override
    protected void runCommand(Command command) {

    }
}
