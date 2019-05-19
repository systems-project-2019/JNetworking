package lib.misc;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @param <E> the data type of the object the command holds
 *            Defines a command object which is used to request servers and clients do certain tasks
 * @author Josh Hilbert
 */
public class Command<E> implements Serializable {

    public static final Command CONNECTED_CLIENTS = new Command<>("getConnectedClients");
    private static List<Command> allCommands = new LinkedList<>();
    private String name;
    private E data;

    /**
     * @param name the name of the command
     */
    public Command(String name) {
        this.name = name;
    }

    /**
     * @return a list of all the registered commands
     */
    public static List<Command> getAllCommands() {
        return allCommands;
    }

    /***
     * Set the entire list of commands
     * @param allCommands the new list of all commands
     */
    public static void setAllCommands(List<Command> allCommands) {
        Command.allCommands = allCommands;
    }

    /**
     * Add one command to the existing list of commands
     *
     * @param command the command to add
     */
    public static void addCommand(Command command) {
        allCommands.add(command);
    }

    /**
     * @return the name of the command
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of an existing command
     *
     * @param name the new name of the command
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the data type associated with the command
     */
    public E getData() {
        return data;
    }

    /**
     * Set the data type associated with the command
     *
     * @param data the new data type of the command
     */
    public void setData(E data) {
        this.data = data;
    }

    /**
     * Check if two commands are the same command
     *
     * @param other the other command
     * @return true if both commands are the same
     */
    public boolean equals(Command other) {
        return this.getName().equalsIgnoreCase(other.getName());
    }

}
