package lib.net;

import lib.Command;
import lib.Data;
import lib.exceptions.ClientAlreadyExistsException;
import lib.exceptions.ClientNotFoundException;
import lib.exceptions.CommandNotFoundException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;


/*
 * The server that can be run both as a console application or a GUI
 */
public abstract class JServer {
    // a unique ID for each connection
    private int uniqueId;
    // an ArrayList to keep the list of the JClient
    private static ArrayList<ClientThread> clientThreads;
    // if I am in a GUI
    //private ServerGUI sg;
    // to display time
    private SimpleDateFormat sdf;
    // the port number to listen for connection
    private int port;
    // the boolean that will be turned of to stop the server
    private boolean keepGoing;

    public static String displayConnected;

    //private static LinkedList<JClient> clients = new LinkedList<>();

    //private Interpreter interpreter = new Interpreter(this);


    /*
     *  server constructor that receive the port to listen to for connection as parameter
     *  in console
     */
//    public JServer(int port) {
//        this(port, null);
//    }

    public JServer(int port) {
        // GUI or not
        //this.sg = sg;
        // the port
        this.port = port;
        // to display hh:mm:ss
        sdf = new SimpleDateFormat("HH:mm:ss");
        // ArrayList for the JClient list
        clientThreads = new ArrayList<>();
        //lib.Command.setServer(this);
    }

//    public void create() {
//        int portNumber = 1500;
//
//        // create a server object and connect it
//        JServer server = new JServer(portNumber);
//        server.connect();
//    }

    public void start() {
        keepGoing = true;
        /* create socket server and wait for connection requests */
        try
        {
            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);

            // infinite loop to wait for connections
            while(keepGoing)
            {
                // format message saying we are waiting
                display("Server waiting for Clients on port " + port + ".");

                Socket socket = serverSocket.accept();  	// accept connection
                // if I was asked to stop
                if(!keepGoing)
                    break;
                ClientThread t = new ClientThread(socket);  // make a thread of it
                if(clientAlreadyExists(t)) {
                    throw new ClientAlreadyExistsException();
                }
                clientThreads.add(t);									// save it in the ArrayList
                t.start();
            }
            // I was asked to stop
            try {
                serverSocket.close();
                for(int i = 0; i < clientThreads.size(); ++i) {
                    ClientThread tc = clientThreads.get(i);
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    }
                    catch(IOException ioE) {
                        // not much I can do
                    }
                }
            }
            catch(Exception e) {
                display("Exception closing the server and clientThreads: " + e);
            }
        }
        // something went bad
        catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        } catch (ClientAlreadyExistsException e) {
            display("The username chosen is already in use: " + e);
            //TODO: command to kill client
        }
    }

    private boolean clientAlreadyExists(ClientThread t) {
        for (ClientThread thread:clientThreads) {
            if (thread.getUsername().equals(t.username))
                return true;
        }
        return false;
    }

    //    /*
//     * For the GUI to stop the server
//     */
//    protected void stop() {
//        keepGoing = false;
//        // connect to myself as JClient to exit statement
//        // Socket socket = serverSocket.accept();
//        try {
//            new Socket("localhost", port);
//        }
//        catch(Exception e) {
//            // nothing I can really do
//        }
//    }
    /*
     * Display an event (not a message) to the console or the GUI
     */
    public abstract void display(String msg);
    /*
     *  to broadcast a message to all Clients
     */
    public synchronized void broadcast(Data data) {
        display(data.getSender() + ": " + data.getMessage());
        writeToClients(data);
    }

//    private LinkedList<String> sendToAll() {
//        LinkedList<String> allPlayers = new LinkedList<>();
//        for (ClientThread ct : clientThreads) {
//            allPlayers.add(ct.getName());
//        }
//        return allPlayers;
//    }

    public void sendToSpecificClients(String message, List<String> recipients) throws ClientNotFoundException {
        boolean recipientConnected = false;
        ClientThread sendTo = null;
        for (ClientThread ct : clientThreads) {
            if (recipients.contains(ct.username)) {
                sendTo = ct;
                recipientConnected = true;
            }
        }

        if (recipientConnected) {
            Data toSend = new Data(message, Data.FROM_SERVER);
            toSend.setRecipients(recipients);
            sendTo.write(toSend);
            display(sentToFormat(message, recipients));
        }
        else
            throw new ClientNotFoundException();
    }

    public abstract String sentToFormat(String message, List<String> recipients);

//    public synchronized void broadcastCommand(lib.Command command) {
//        // add HH:mm:ss and \n to the message
////        String time = sdf.format(new Date());
////        String messageLf = time + " " + message + "\n";
////        // display message on console or GUI
////        //if(sg == null)
////        System.out.print(messageLf);
////        else
////            sg.appendRoom(messageLf);     // append in the room window
//
//        // we loop in reverse order in case we would have to remove a JClient
//        // because it has disconnected
//        display(command.toString());
//        checkForDisconnectedClients(command.getActionName());
//    }

    private synchronized void writeToClients(Data data) {
        for(int i = clientThreads.size(); --i >= 0;) {
            ClientThread ct = clientThreads.get(i);
            // try to write to the JClient if it fails remove it from the list
            if(!ct.write(data)) {
                clientThreads.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }

    private boolean allRecipientsConnectedToServer(List<String> recipients) {
        for (ClientThread player : clientThreads) {
            for (String recipient : recipients) {
                if (player.getUsername().trim().equalsIgnoreCase(recipient.trim()))
                    return true;
            }
        }
        return false;
    }

    // for a client who logoff using the LOGOUT message
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for(int i = 0; i < clientThreads.size(); ++i) {
            ClientThread ct = clientThreads.get(i);
            // found it
            if(ct.id == id) {
                clientThreads.remove(i);
                return;
            }
        }
    }

//    public static LinkedList<JClient> getClients() {
//        return clients;
//    }
//
//    public static void setClients(LinkedList<JClient> clients) {
//        JServer.clients = clients;
//    }

    public static ArrayList<ClientThread> getClientThreads() {
        return clientThreads;
    }

    /*
     *  To run as a console application just open a console window and:
     * > java JServer
     * > java JServer portNumber
     * If the port number is not specified 1500 is used
     */
//    public static void main(String[] args) {
//        //connect server on port 1500 unless a PortNumber is specified
//        int portNumber = 1500;
//        switch(args.length) {
//            case 1:
//                try {
//                    portNumber = Integer.parseInt(args[0]);
//                }
//                catch(Exception e) {
//                    System.out.println("Invalid port number.");
//                    System.out.println("Usage is: > java JServer [portNumber]");
//                    return;
//                }
//            case 0:
//                break;
//            default:
//                System.out.println("Usage is: > java JServer [portNumber]");
//                return;
//
//        }
//        // create a server object and connect it
//        JServer server = new JServer(portNumber);
//        server.connect();
//    }

    /** One instance of this thread will run for each client */
    public class ClientThread extends Thread {
        // the socket where to listen/talk
        private Socket socket;
        private ObjectInputStream sInput;
        private ObjectOutputStream sOutput;
        // my unique id (easier for deconnection)
        int id;
        // the Username of the JClient
        String username;
        // the only type of message a will receive
        Data data;
        // the date I connect
        String date;

        // Constructor
        public ClientThread(Socket socket) {
            // a unique id
            id = ++uniqueId;
            this.socket = socket;
            /* Creating both Data Stream */
            System.out.println("Thread trying to create Object Input/Output Streams");
            try
            {
                // create output first
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                // read the username
                username = (String) sInput.readObject();
                display(username + " just connected.");
            }
            catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }
            // have to catch ClassNotFoundException
            // but I read a String, I am sure it will work
            catch (ClassNotFoundException e) {
            }
            date = new Date().toString() + "\n";
        }

        // what will run forever
        public void run() {
            // to loop until LOGOUT
            boolean keepGoing = true;
            while(keepGoing) {
                // read a String (which is an object)
                try {
                    data = (Data) sInput.readObject();
                }
                catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    display(username + " has disconnected from the server.");
                    remove(id); // TODO: remove the disconnected player
                    break;
                }
                catch(ClassNotFoundException e2) {
                    break;
                }
                // the messaage part of the Data
                String message = data.getMessage();

                // Switch on the type of message receive
                switch(data.getType()) {

                    case Data.MESSAGE:
                        broadcast(data);
                        break;
//                    case Data.LOGOUT:
//                        display(username + " disconnected with a LOGOUT message.");
//                        keepGoing = false;
//                        break;
                    case Data.COMMAND:
                        display(username + ": requested " + data.getCommand().getName());
                        try {
                            runServerCommand(data.getCommand(), data.getSender());
                        } catch (CommandNotFoundException | ClientNotFoundException e) { // client not found message?
                            e.printStackTrace();
                        }
                        break;
                }
            }
            // remove myself from the arrayList containing the list of the
            // connected Clients
            remove(id);
            close();
        }

        // try to close everything
        private void close() {
            // try to close the connection
            try {
                if(sOutput != null) sOutput.close();
            }
            catch(Exception e) {}
            try {
                if(sInput != null) sInput.close();
            }
            catch(Exception e) {}
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        /*
         * Write a Data message to the JClient output stream for either all clients or one client
         */
        private boolean write(Data msg) {
            // if JClient is still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
//                Data toBeSent = new Data(msg, data.getSender());
////                if (recipient != null) {
////                    toBeSent.setRecipients(recipient);
////                }
                sOutput.writeObject(msg);
            }
            // if an error occurs, do not abort just inform the user
            catch(IOException e) {
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

    private void runServerCommand(Command command, String sentFrom) throws CommandNotFoundException, ClientNotFoundException {
        LinkedList<String> sender = new LinkedList<>();
        sender.add(sentFrom);

        if (command.equals(Command.getPlayers)) {
            String allPlayers = "All Players: ";
//            for (JClient c : clients) {
//                allPlayers += c.getUsername() + " ";
//            }

            sendToSpecificClients(clientThreads.toString(), sender);
        } else if (command.equals(Command.sendTo)) {
            String recipientsAndMessage = (String) command.getData();
            try {
                String toReceive = recipientsAndMessage.substring(0, recipientsAndMessage.indexOf(" "));
                String message = recipientsAndMessage.substring(recipientsAndMessage.indexOf(" ") + 1);

                String[] recipients = toReceive.trim().split(",");

                List<String> recipientsList = Arrays.asList(recipients);

                sendToSpecificClients(message, recipientsList);

            } catch (Exception e) {
                sendToSpecificClients("Send Failed. Use the format /sendTo [player,player,...] [message]", sender);
                e.printStackTrace();
            }
        }
        else {
            sendToSpecificClients("Command not found.", sender);
            throw new CommandNotFoundException();
        }
    }

}

