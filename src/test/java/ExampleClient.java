import lib.misc.Command;
import lib.net.JClient;

import java.io.IOException;
import java.util.Scanner;

public class ExampleClient extends JClient {
    public ExampleClient(String server_ip, int port, String username) {
        super(server_ip, port, username);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ExampleClient exampleClient = new ExampleClient("localhost", 1500, sc.next());
        exampleClient.connect();
        sc.next();
        exampleClient.requestCommand(ExampleServer.getScore());
        //exampleClient.sendTo("Hi", "Gavin", "Michael");
//        try {
//            exampleClient.broadcast("Hi");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        while (true) {
//            String input = sc.next();
//            if (input.equalsIgnoreCase("exit")) {
//                exampleClient.disconnect();
//            }
//            try {
//                exampleClient.broadcast(input);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void display(String s) {
        System.out.println(s);
    }

    @Override
    protected void runCommand(Command command) {

    }
}
