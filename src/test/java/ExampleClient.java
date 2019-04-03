import lib.Command;
import lib.Data;
import lib.net.JClient;

import java.io.IOException;
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
        exampleClient.broadcast(new Data(8));

    }

    @Override
    public void display(String message) {
        System.out.println(message);
    }

    @Override
    public String sendToAllDisplayFormat(String message, String sender) {
        return sender + ": " + message;
    }

    @Override
    public String sendToSpecificClientsFormat(String message, String sender, List<String> recipients) {
        if (recipients.contains(getUsername())) {
            recipients.remove(getUsername());
            recipients.add("You");
        }

        return "Server: " + "--> " + recipients + ": " + message;
    }

    @Override
    protected void runCommand(Command command) {

    }
}
