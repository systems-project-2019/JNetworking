package lib;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Command<E> implements Serializable {

    private String name;
    private E data;

    public static final Command GET_CONNECTED_CLIENTS = new Command<>("getConnectedClients");

    private static List<Command> allCommands = new LinkedList<>();

//    public Command() {
//
//    }

    public Command(String name) {
        this.name = name;
    }


//    private static boolean commandExists(String c) {
//        assert getAllCommands() != null;
//        for (Command command : getAllCommands()) {
//            if (command.getName().equalsIgnoreCase(c))
//                return true;
//        }
//
//        return false;
//    }

    public static List<Command> getAllCommands() {
        return allCommands;
    }

    public static void setAllCommands(List<Command> allCommands) {
        Command.allCommands = allCommands;
    }

    public static void addCommand(Command command) {
        allCommands.add(command);
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
