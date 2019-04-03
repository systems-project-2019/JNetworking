import lib.Command;
import lib.Data;
import lib.net.JServer;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ExampleServer extends JServer {

    public static void main(String[] args) {
        Command.addCommand(Commands.WAZA_COMMAND);

        ExampleServer exampleServer = new ExampleServer(1500);
        exampleServer.start();
        Scanner sc = new Scanner(System.in);
        sc.next();
        try {
            exampleServer.broadcast(new Data(8, Data.FROM_SERVER));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
    protected void runCustomCommand(Command command, String sentFrom) {
        if (command.equals(new Command("waza"))) {
            broadcast(new Data("WAZA"));
        }
    }

}
