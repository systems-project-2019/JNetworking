package app;

import lib.net.Server;

public class TestServer extends Server {

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

}
