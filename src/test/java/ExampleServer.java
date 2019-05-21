import lib.misc.Command;
import lib.misc.Data;
import lib.exceptions.ClientNotFoundException;
import lib.net.JServer;

import java.io.IOException;
import java.util.Collections;

public class ExampleServer extends JServer {

    private static Command<Integer> score = new Command<>("score");

    public ExampleServer(int port) {
        super(port);
    }

    public static void main(String[] args) {
        Command.addCommand(score);

        ExampleServer exampleServer = new ExampleServer(1500);
        exampleServer.start();
    }

    public static Command<Integer> getScore() {
        return score;
    }

    @Override
    public void display(String s) {
        System.out.println(s);
    }

    @Override
    protected void runCustomCommand(Command command, String sentFrom) {
        if (command.equals(score)) {
            try {
                score.setObject(8);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Data scoreMsg = null;
            try {
                scoreMsg = new Data(score.getObject());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                sendToSpecificClients(scoreMsg, Collections.singletonList(sentFrom));
            } catch (ClientNotFoundException e) {
                try {
                    broadcast(new Data("Client not found"));
                } catch (ClientNotFoundException | IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
