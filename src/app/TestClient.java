package app;

import lib.net.Client;

import java.util.Scanner;

public class TestClient extends Client {
    public TestClient(String server_ip, int port, String username) {
        super(server_ip, port, username);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        TestClient testClient = new TestClient("localhost", 1500, sc.next());
        testClient.connect();
        sc.next();
        testClient.getUsers();

        //testClient.broadcast("Testing123");
    }

    @Override
    public void display(String message) {
        System.out.println(message);
    }
}
