package lib;

import lib.misc.Interpreter;
import lib.net.JServer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the JServer.
 */
public class Data implements Serializable {

    // The different types of message sent by the JClient
    // COMMAND to execute a command on the server
    // OBJECT an object to be sent on the server
    public static final int COMMAND = 0, OBJECT = 2;
    public static final String FROM_SERVER = "Server";

    private int type;

    private Command command;
    private String sender;
    private byte[] object;

    private List<String> recipients;
    private boolean sendToAll = true;

    public Data(Command command, String sender) {
        this.type = COMMAND;
        this.command = command;
        this.sender = sender;
    }

//    public Data(String message, String sender) {
//        this.type = 1;
//        this.message = message;
//        this.sender = sender;
//    }

    public Data(Object object, String sender) throws IOException {
        this.type = OBJECT;
        this.object = Interpreter.toByteArray(object);
        this.sender = sender;
    }

    public Data(Object object) throws IOException { // for server sender
        this.type = OBJECT;
        this.object = Interpreter.toByteArray(object);
        this.sender = FROM_SERVER; //(server)
    }

//    public Data(Object object) {
//        this.type = OBJECT;
//        this.object = In
//    }



    public int getType() {
        return type;
    }

    public Command getCommand() {
        return command;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        sendToAll = false;
        this.recipients = recipients;
    }

    public String getSender() {
        return sender;
    }

    public boolean isSendToAll() {
        return sendToAll;
    }

    public void setSendToAll(boolean sendToAll) {
        this.sendToAll = sendToAll;
    }

    public Object getObject() throws IOException, ClassNotFoundException {
        return Interpreter.toObject(object);
    }

    @Override
    public String toString() {
        if (type == COMMAND) {
            return command.getName();
        }
        else {
            try {
                return getObject().toString();
            } catch (IOException | ClassNotFoundException e) {
                return e.getMessage();
            }
        }
    }
}
