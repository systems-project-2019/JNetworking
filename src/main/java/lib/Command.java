package lib;

import java.io.Serializable;
import java.util.LinkedList;

public class Command<E> implements Serializable {

    private String name;
    private E data;

    public static final Command GET_CONNECTED_CLIENTS = new Command<>("getConnectedClients");
    public static final Command<LinkedList<String>> SEND_TO = new Command<>("sendTo");


    public Command() {

    }

    public Command(String name) {
        this.name = name;
    }

    public static boolean containsCommand(String input) {
        String commandBase;

        if (input.contains(" ")) {
            commandBase = input.substring(0, input.indexOf(" "));
            return commandBase.length() > 2 && commandBase.substring(0, 1).equals("/") && commandExists(commandBase.substring(1));
        }

        return input.length() > 2 && input.substring(0, 1).equals("/") && commandExists(input.substring(1));
    }

    private static boolean commandExists(String c) {
        for (Command command : getAllCommands()) {
            if (command.getName().equalsIgnoreCase(c))
                return true;
        }

        return false;
    }

    public static Command[] getAllCommands() {
        //Command[] allCommands = new Command[]
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    public static Data sendCommand(String input, String sender) {
        Command command;
        if (input.contains(" ")) {
            command = new Command(input.substring(1, input.indexOf(" ")));
            command.setData(input.substring(input.indexOf(" ") + 1));
        } else {
            command = new Command(input.substring(1));
        }

        return new Data(command, sender);
    }

    public boolean equals(Command other) {
        return this.getName().equalsIgnoreCase(other.getName());
    }

}
