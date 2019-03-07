package lib;

import lib.exceptions.CommandNotFoundException;
import lib.net.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Interpreter {

//    private Map<lib.Command, Runnable> commandMap = new HashMap<>();
//    private static lib.Command[] commandList = lib.Command.ALL_COMMANDS;
//    private ArrayList<Runnable> runnableList = new ArrayList<>();
//
//    private Server thisServer;
//
//    private String commandsInLine;
//    private String msgOut;
//    private String commandBase, commandSubstance;
//
//    public final byte SET_NAME = 0;
//
//    public Interpreter(Server thisServer) {
//        this.thisServer = thisServer;
//        fillRunnableList();
//        fillMap();
//    }
//
//    private void fillRunnableList() {
//        for (lib.Command command:lib.Command.ALL_COMMANDS) {
//
//        }
//    }
//
//    private void fillMap() {
//        for (int i = 0; i < commandList.length; i++) {
//            commandMap.put(commandList[i], runnableList.get(i));
//        }
//    }
//
//    public void interpretCommand(lib.Command command) throws CommandNotFoundException {
//        if (commandMap.containsKey(command)) {
//            commandMap.get(command).run();
//        } else
//            throw new CommandNotFoundException();
//    }
}
