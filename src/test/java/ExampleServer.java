import lib.Command;
import lib.Data;
import lib.exceptions.ClientNotFoundException;
import lib.net.JServer;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ExampleServer extends JServer {

    private static Command<Integer> score = new Command<>("score");

    public static void main(String[] args) {
        Command.addCommand(score);

        ExampleServer exampleServer = new ExampleServer(1500);
        exampleServer.start();
        Scanner sc = new Scanner(System.in);
        sc.next();

    }

    public ExampleServer(int port) {
        super(port);
    }

    @Override
    public void display(String msg) {
        System.out.println(msg);
    }

    @Override
    public String sentToFormat(String message, List<String> recipients) {
        return "Sent to --> " + recipients.toString() + ": " + message;
    }

    @Override
    protected void runCustomCommand(Command command, String sentFrom) throws IOException {
        if (command.equals(score)) {
            score.setData(8);
            Data scoreMsg = new Data(score.getData());
            try {
                sendToSpecificClients(scoreMsg, Collections.singletonList(sentFrom));
            } catch (ClientNotFoundException e) {
                broadcast(new Data("Client not found"));
            }
        }
    }

    public static Command<Integer> getScore() {
        return score;
    }
}
