package lib.net;

import lib.misc.Command;
import lib.misc.Data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Josh Hilbert
 * An abstract client class meant to be extended in a custom client classs
 * Loosely based on the work of user: pbl on 13 December 2011
 * https://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optional/
 */
public abstract class JClient {

    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private String server, username;
    private int port;


    /**
     * Client constructor
     *
     * @param server_ip the server ip to try to connect to
     * @param port      the port of the server
     * @param username  the username of the connecting client
     */
    public JClient(String server_ip, int port, String username) {
        this.server = server_ip;
        this.port = port;
        this.username = username;
    }

    /**
     * Attempt to connect the client to the server
     */
    public void connect() {
        try {
            socket = new Socket(server, port);
        } catch (Exception ec) {
            display("Error connecting to server:" + ec);
        }

        display("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());

        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
        }

        new ListenFromServer().start();

        try {
            sOutput.writeObject(username);
        } catch (IOException eIO) {
            display("Exception logging in : " + eIO);
            disconnect();
        }
    }


    /**
     * Abstract method to display information coming from the client
     *
     * @param s String to be displayed
     */
    public abstract void display(String s);

    /*
     * Disconnect this instance of a client from the server
     */
    public void disconnect() {
        try {
            if (sInput != null) sInput.close();
        } catch (Exception ignored) {
        }
        try {
            if (sOutput != null) sOutput.close();
        } catch (Exception ignored) {
        }
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {
        }

    }

    /**
     * @return the username of the client
     */
    public String getUsername() {
        return username;
    }

    /**
     * Broadcasts an object to all clients
     *
     * @param object
     * @throws IOException
     */
    public void broadcast(Object object) throws IOException {
        sOutput.writeObject(new Data(object, username));
        display("You: " + object.toString());
    }

    /**
     * Sends an object to specific clients
     *
     * @param input   the object to send
     * @param clients an unlimited number of client parameters to send to
     */
    public void sendTo(Object input, String... clients) {
        LinkedList<String> recipients = new LinkedList<>(Arrays.asList(clients));

        Data toSend = null;
        try {
            toSend = new Data(input, this.username);
            toSend.setRecipients(recipients);
        } catch (IOException e) {
            display(e.getMessage());
        }

        try {
            sOutput.writeObject(toSend);
            display("You --> " + recipients + ": " + input);
        } catch (IOException e) {
            display("Error Sending Message: " + e);
        }
    }

    /**
     * Sends and object to specific clients
     *
     * @param input      the object to send
     * @param recipients a list of recipients
     */
    public void sendTo(Object input, List<String> recipients) {
        Data toSend = null;
        try {
            toSend = new Data(input, this.username);
            toSend.setRecipients(recipients);
        } catch (IOException e) {
            display(e.getMessage());
        }

        try {
            sOutput.writeObject(toSend);
            display("You --> " + recipients + ": " + input);
        } catch (IOException e) {
            display("Error Sending Message: " + e);
        }
    }

    /**
     * Requests the built in command of getting a list of all connected clients
     */
    public void getUsers() {
        requestCommand(Command.CONNECTED_CLIENTS);
    }

    /**
     * Requests a custom command
     *
     * @param command the requested command
     */
    public void requestCommand(Command command) {
        display("You: requested " + command.getName());
        try {
            sOutput.writeObject(new Data(command, username));
        } catch (IOException e) {
            display("Command: " + command.getName() + " send failed: " + e);
        }
    }

    /**
     * Defines a format for displaying a message sent by the client to all clients (can be overridden)
     *
     * @param input  the object being sent
     * @param sender the client sending the message
     * @return a formatted string
     */
    public String displaySendToAllMessage(Object input, String sender) {
        return sender + ": " + input.toString();
    }

    /**
     * Defines a format for displaying a message sent by the client to specific clients (can be overridden)
     *
     * @param input      the object being sent
     * @param sender     the client sending the message
     * @param recipients recipients of the message
     * @return a formatted string
     */
    public String sendToSpecificClientsFormat(Object input, String sender, List<String> recipients) {
        List<String> updatedRecipients = new LinkedList<>(recipients);

        if (updatedRecipients.contains(username)) {
            updatedRecipients.remove(username);
            updatedRecipients.add("You");
        }

        return sender + " --> " + updatedRecipients + ": " + input.toString();
    }

    /**
     * Define how custom commands will be run
     *
     * @param command the custom command to be run
     */
    protected abstract void runCommand(Command command);

    /*
     * Class that waits for data from the server
     */
    class ListenFromServer extends Thread {

        public void run() {
            while (true) {
                try {
                    Data input = (Data) sInput.readObject();
                    if (input.getType() == Data.COMMAND) {
                        runCommand(input.getCommand());
                    } else if (input.getType() == Data.OBJECT) {
                        if (!dataIsFromRecipient(input)) {
                            if (input.getRecipients().contains(username))
                                formatInputForDisplay(input);
                        }
                    }

                } catch (IOException e) {
                    display("Server has closed the connection: " + e);
                    break;
                } catch (ClassNotFoundException ignored) {
                }
            }
        }

        /**
         * Decides what type of formatting will be done to the incoming data
         *
         * @param input incoming data
         */
        private void formatInputForDisplay(Data input) {
            if (input.isSendToAll()) {
                try {
                    display(displaySendToAllMessage(input.getObject(), input.getSender()));
                } catch (IOException | ClassNotFoundException e) {
                    display(e.getMessage());
                }
            } else {
                try {
                    display(sendToSpecificClientsFormat(input.getObject(), input.getSender(), input.getRecipients()));
                } catch (IOException | ClassNotFoundException e) {
                    display(e.getMessage());
                }
            }
        }

        /**
         * Checks if this instance of a client sent the data being received
         *
         * @param input data being received
         * @return true if the sender of the data is this instance of client
         */
        private boolean dataIsFromRecipient(Data input) {
            if (input.getSender().equals(Data.FROM_SERVER))
                return false;
            return input.getSender().equals(username);
        }
    }
}
