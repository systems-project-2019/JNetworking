package lib.misc;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * @author Josh Hilbert
 * Defines the data object which is the type of object passed between servers and clients
 */
public class Data implements Serializable {

    public static final int COMMAND = 0, OBJECT = 2;
    public static final String FROM_SERVER = "Server";

    private int type;

    private Command command;
    private String sender;
    private byte[] object;

    private List<String> recipients;
    private boolean sendToAll = true;

    /**
     * Creates new data object of the command type
     *
     * @param command the command being stored
     * @param sender  the sender sending the data
     */
    public Data(Command command, String sender) {
        this.type = COMMAND;
        this.command = command;
        this.sender = sender;
    }

    /**
     * Creates new data object of the object type
     *
     * @param object the object being stored
     * @param sender the sender sending the data
     * @throws IOException
     */
    public Data(Object object, String sender) throws IOException {
        this.type = OBJECT;
        this.object = Interpreter.toByteArray(object);
        this.sender = sender;
    }

    /**
     * Creates new data object of the object type with a server as the sender
     *
     * @param object the object being stored
     * @throws IOException
     */
    public Data(Object object) throws IOException {
        this.type = OBJECT;
        this.object = Interpreter.toByteArray(object);
        this.sender = FROM_SERVER;
    }

    /**
     * @return the type of data being stored
     */
    public int getType() {
        return type;
    }

    /**
     * @return the command being stored in the data object
     */
    public Command getCommand() {
        return command;
    }

    /**
     * @return the recipients being stored in the data object
     */
    public List<String> getRecipients() {
        return recipients;
    }

    /**
     * Set the recipients of the data object
     *
     * @param recipients the new recipient list
     */
    public void setRecipients(List<String> recipients) {
        sendToAll = false;
        this.recipients = recipients;
    }

    /**
     * @return the sender of the data object
     */
    public String getSender() {
        return sender;
    }

    /**
     * @return true if the data is set to be sent to all clients
     */
    public boolean isSendToAll() {
        return sendToAll;
    }

    /**
     * Set whether the data will be sent to all clients
     *
     * @param sendToAll the new boolean
     */
    public void setSendToAll(boolean sendToAll) {
        this.sendToAll = sendToAll;
    }

    /**
     * @return the object stored in the data
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Object getObject() throws IOException, ClassNotFoundException {
        return Interpreter.toObject(object);
    }

    @Override
    public String toString() {
        if (type == COMMAND) {
            return command.getName();
        } else {
            try {
                return getObject().toString();
            } catch (IOException | ClassNotFoundException e) {
                return e.getMessage();
            }
        }
    }
}
