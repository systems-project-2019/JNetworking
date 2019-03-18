import lib.net.JServer;

import java.util.List;

public class TestServer extends JServer {

    public static void main(String[] args) {
        TestServer testServer = new TestServer(1500);
        testServer.start();
    }

    public TestServer(int port) {
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

}
