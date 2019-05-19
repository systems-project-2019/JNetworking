package lib.net;

import lib.Command;
import lib.Data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/*
 * The JClient that can be run both as a console or a GUI
 */
public abstract class JClient {

    // for I/O
    private ObjectInputStream sInput;		// to read from the socket
    private ObjectOutputStream sOutput;		// to write on the socket
    private Socket socket;

    // if I use a GUI or not
    //private ClientGUI cg;

    // the server, the port and the username
    private String server, username;
    private int port;


    public JClient(String server_ip, int port, String username) {
        this.server = server_ip;
        this.port = port;
        this.username = username;
    }

    /*
     * To connect the dialog
     */
    public void connect() {
        // try to connect to the server
        try {
            socket = new Socket(server, port);
        }
        // if it failed not much I can so
        catch(Exception ec) {
            display("Error connecting to server:" + ec);
        }

        display("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());

        /* Creating both Data Stream */
        try
        {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
        }

        // creates the Thread to listen from the server
        new ListenFromServer().start();
        // Send our username to the server this is the only message that we
        // will send as a String. All other messages will be Data objects
        try
        {
            sOutput.writeObject(username);
        }
        catch (IOException eIO) {
            display("Exception logging in : " + eIO);
            disconnect();
        }
        // success we inform the caller that it worked
    }



    public abstract void display(String message);

    /*
     * To send a message to the server
     */
    public void sendMessage(Data output) {
        try {
            sOutput.writeObject(output);
        }
        catch(IOException e) {
            display("Exception writing to server: " + e);
            e.printStackTrace();
        }
    }

    /*
     * When something goes wrong
     * Close the Input/Output streams and disconnect not much to do in the catch clause
     */
    private void disconnect() {
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {} // not much else I can do
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {} // not much else I can do
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {} // not much else I can do

        // inform the GUI
//        if(cg != null)
//            cg.connectionFailed();

    }

    public String getUsername() {
        return username;
    }

//    public void broadcast(String message) throws IOException {
//        sOutput.writeObject(new Data(message, username));
//        display("You: " + message);
//    }

    public void broadcast(Object object) throws IOException {
        sOutput.writeObject(new Data(object, username));
        display("You: " + object.toString());
    }

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

    public void getUsers() {
        requestCommand(Command.GET_CONNECTED_CLIENTS);
    }

    public void requestCommand(Command command) {
        display("You: requested " + command.getName());
        try {
            sOutput.writeObject(new Data(command, username));
        } catch (IOException e) {
            display("Command: " + command.getName() + " send failed: " + e);
        }
    }

    public abstract String displaySendToAllMessage(Object message, String sender);

    public abstract String sendToSpecificClientsFormat(Object message, String sender, List<String> recipients);


    /*
     * a class that waits for the message from the server and append them to the JTextArea
     * if we have a GUI or simply System.out.println() it in console mode
     */
    class ListenFromServer extends Thread {

        public void run() {
            while(true) {
                try {
                    Data input = (Data) sInput.readObject();
                    // if console mode print the message and add back the prompt
                    //if(cg == null) {
                    //TODO: make this a switch block

                    if (input.getType() == Data.COMMAND) {
                        runCommand(input.getCommand());
                    }
//                    else if (input.getType() == Data.MESSAGE) {
//                        String msg = input.getMessage();
//                        if (!dataIsFromRecipient(input)) {
//                            if (input.isSendToAll())
//                                display(displaySendToAllMessage(input, input.getSender()));
//                            else
//                                display(sendToSpecificClientsFormat(input, input.getSender(), input.getRecipients()));
//                        }
//                    }
                    else if (input.getType() == Data.OBJECT) {
                        if (!dataIsFromRecipient(input)) {
                            if (input.getRecipients().contains(username))
                                formatInputForDisplay(input);
                        }
                    }

                }
                catch(IOException e) {
                    display("Server has closed the connection: " + e);
                    break;
                }

                catch(ClassNotFoundException ignored) {
                }
            }
        }

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

        private boolean thisContainedInRecipientList(Data input) {
            for (String player : input.getRecipients()) {
                if (player.equals(username))
                    return true;
            }
            return false;
        }

        private boolean dataIsFromRecipient(Data input) {
            if (input.getSender().equals(Data.FROM_SERVER))
                return false;
            return input.getSender().equals(username);
        }
    }

    protected abstract void runCommand(Command command);
}
