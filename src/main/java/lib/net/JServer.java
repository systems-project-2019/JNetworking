package lib.net;

import lib.misc.Command;
import lib.misc.Data;
import lib.exceptions.ClientAlreadyExistsException;
import lib.exceptions.ClientNotFoundException;
import lib.exceptions.CommandNotFoundException;
import lib.misc.Time;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * @author Josh Hilbert
 * An abstract server class meant to be extended in a custom server classs
 * Loosely based on the work of user: pbl on 13 December 2011
 * https://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optional/
 */
public abstract class JServer {

    private static ArrayList<ClientThread> clientThreads;
    private int uniqueId;
    private int port;
    private boolean keepGoing;

    public JServer(int port) {
        this.port = port;
        clientThreads = new ArrayList<>();
    }

    /**
     * @return an ArrayList of ClientThread objects (all clients connected to the server)
     */
    public static ArrayList<ClientThread> getClientThreads() {
        return clientThreads;
    }

    /**
     * Starts the server
     */
    public void start() {
        keepGoing = true;
        /* create socket server and wait for connection requests */
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            // Infinite loop to wait for connections
            while (keepGoing) {
                display("Server waiting for Clients on port " + port + ".");

                Socket socket = serverSocket.accept();

                if (!keepGoing)
                    break;
                ClientThread t = new ClientThread(socket);
                if (clientAlreadyExists(t)) {
                    throw new ClientAlreadyExistsException();
                }
                clientThreads.add(t);
                t.start();
            }

            try {
                serverSocket.close();
                for (int i = 0; i < clientThreads.size(); ++i) {
                    ClientThread tc = clientThreads.get(i);
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    } catch (IOException ignored) {
                    }
                }
            } catch (Exception e) {
                display("Exception closing the server and clientThreads: " + e);
            }
        } catch (IOException e) {
            String msg = Time.getCurrentTime() + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        } catch (ClientAlreadyExistsException e) {
            display("The username chosen is already in use: " + e);
            //TODO: command to kill client
        }
    }

    /**
     * Checks if a client is already in the server
     *
     * @param t client thread
     * @return true if client is already in the server
     */
    private boolean clientAlreadyExists(ClientThread t) {
        for (ClientThread thread : clientThreads) {
            if (thread.getUsername().equals(t.username))
                return true;
        }
        return false;
    }

    /**
     * Shuts down the server
     */
    public void stop() {
        System.exit(0);
    }

    /**
     * Abstract method to display information coming from the server
     *
     * @param s String to be displayed
     */
    public abstract void display(String s);

    /*
     *  to broadcast a data object to all Clients
     */
    public synchronized void broadcast(Data data) throws ClientNotFoundException, IOException {
        if (allRecipientsConnectedToServer(data.getRecipients())) {
            if (data.isSendToAll()) {
                display(data.getSender() + ": " + data.toString());
            } else {
                display(data.getSender() + " --> " + data.getRecipients() + ": " + data.toString());
            }

            writeToClients(data);
        } else
            throw new ClientNotFoundException();
    }

    /**
     * Sends a data object to specified clients
     *
     * @param data       data object to be sent
     * @param recipients a list of recipients of the message
     * @throws ClientNotFoundException if any clients in the recipient list are not connected to the server
     */
    public void sendToSpecificClients(Data data, List<String> recipients) throws ClientNotFoundException {
        boolean recipientsConnected = false;
        ClientThread sendTo = null;
        for (ClientThread ct : clientThreads) {
            if (recipients.contains(ct.username)) {
                sendTo = ct;
                recipientsConnected = true;
            }
        }

        if (recipientsConnected) {
            data.setRecipients(recipients);
            sendTo.write(data);
            display(sentToFormat(data.toString(), recipients));
        } else
            throw new ClientNotFoundException();
    }

    /**
     * How a string will be formatted when it is being sent to multiple clients
     *
     * @param message    the string being sent
     * @param recipients list of all recipients of the string
     * @return a formatted string
     */
    public String sentToFormat(String message, List<String> recipients) {
        return "Sent to --> " + recipients.toString() + ": " + message;
    }

    /**
     * Helper method to send data to clients
     *
     * @param data the data object to be sent
     */
    private synchronized void writeToClients(Data data) {
        for (int i = clientThreads.size(); --i >= 0; ) {
            ClientThread ct = clientThreads.get(i);
            // try to write to the JClient if it fails remove it from the list
            if (!ct.write(data)) {
                clientThreads.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }

    /**
     * Checks if all recipients in a list are connected to the server
     *
     * @param recipients a list of recipients
     * @return true if all recipients are connected to the server
     */
    private boolean allRecipientsConnectedToServer(List<String> recipients) {
        LinkedList<String> connectedPlayers = new LinkedList<>();
        for (ClientThread player : clientThreads) {
            connectedPlayers.add(player.getUsername());
        }

        return connectedPlayers.containsAll(recipients);
    }

    /**
     * Remove a client from the server by id
     *
     * @param id of client to remove
     */
    synchronized void remove(int id) {
        for (int i = 0; i < clientThreads.size(); ++i) {
            ClientThread ct = clientThreads.get(i);
            if (ct.id == id) {
                clientThreads.remove(i);
                return;
            }
        }
    }

    /**
     * Runs a command from an incoming request made by a client
     *
     * @param command  the command requested by a client
     * @param sentFrom the client requesting the command
     * @throws CommandNotFoundException if the command does not exist
     * @throws ClientNotFoundException  if the client does not exist
     * @throws IOException
     */
    private void runCommand(Command command, String sentFrom) throws CommandNotFoundException, ClientNotFoundException, IOException {
        LinkedList<String> sender = new LinkedList<>();
        sender.add(sentFrom);

        if (command.equals(Command.CONNECTED_CLIENTS)) {
            Data toSend = new Data(clientThreads.toString());
            sendToSpecificClients(toSend, sender);
        } else if (existsCustomCommand(command)) {
            runCustomCommand(command, sentFrom);
        } else {
            String errorMsg = "Command " + command.getName() + " not found.";
            Data errorMsgToSend = new Data(errorMsg);
            sendToSpecificClients(errorMsgToSend, sender);
            throw new CommandNotFoundException(command.getName());
        }
    }

    /**
     * Checks if a custom command exists in the list of commands entered into the server
     *
     * @param command command being searched for in list
     * @return true if the command exists in the list
     */
    private boolean existsCustomCommand(Command command) {
        for (Command c : Command.getAllCommands()) {
            if (c.equals(command)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Define what will happen when a custom command is run
     *
     * @param command  command to be run
     * @param sentFrom client request was sent from
     */
    protected abstract void runCustomCommand(Command command, String sentFrom);

    /**
     * One instance of this thread will run for each client
     */
    public class ClientThread extends Thread {

        int id;
        String username;
        Data data;
        String date;
        private Socket socket;
        private ObjectInputStream sInput;
        private ObjectOutputStream sOutput;

        ClientThread(Socket socket) {

            id = ++uniqueId;
            this.socket = socket;
            /* Creating both Data Stream */
            display("Thread trying to create Object Input/Output Streams");
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());

                username = (String) sInput.readObject();
                display(username + " just connected.");
            } catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            } catch (ClassNotFoundException ignored) {
            }
            date = new Date().toString() + "\n";
        }

        // Main loop
        public void run() {
            // to loop until logout
            while (true) {
                try {
                    data = (Data) sInput.readObject();
                } catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    display(username + " has disconnected from the server.");
                    remove(id);
                    break;
                } catch (ClassNotFoundException e2) {
                    break;
                }
                // Receives data
                switch (data.getType()) {
                    case Data.COMMAND:
                        display(username + " requested " + data.getCommand().getName());
                        try {
                            runCommand(data.getCommand(), data.getSender());
                        } catch (CommandNotFoundException | ClientNotFoundException | IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case Data.OBJECT:
                        try {
                            broadcast(data);
                        } catch (ClientNotFoundException e) {
                            try {
                                Data clientNotFound = new Data("Message not sent: One or more clients not found " + e.toString());
                                clientNotFound.setRecipients(Collections.singletonList(data.getSender()));
                                sendToSpecificClients(clientNotFound, Collections.singletonList(data.getSender()));
                            } catch (IOException | ClientNotFoundException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                }
            }
            remove(id);
            close();
        }

        // try to stop everything
        void close() {
            // try to stop the connection
            try {
                if (sOutput != null) sOutput.close();
            } catch (Exception ignored) {
            }
            try {
                if (sInput != null) sInput.close();
            } catch (Exception ignored) {
            }
            try {
                if (socket != null) socket.close();
            } catch (Exception ignored) {
            }
        }

        /*
         * Write a Data message to the JClient output stream for either all clients or one client
         */
        private boolean write(Data msg) {
            if (!socket.isConnected()) {
                close();
                return false;
            }

            try {
                sOutput.writeObject(msg);
            } catch (IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }

        public Socket getSocket() {
            return socket;
        }

        @Override
        public String toString() {
            return username + ": " + socket.getInetAddress();
        }

        public String getUsername() {
            return username;
        }
    }

}

