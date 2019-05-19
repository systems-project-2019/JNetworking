package lib.exceptions;

/**
 * @author Josh Hilbert
 * Exception for if a command is not found
 */
public class CommandNotFoundException extends Exception {

    public CommandNotFoundException(String s) {
        super(s);
    }
}
