package ca.ubc.cs.cpsc210.mindthegap.model.exception;

/**
 * Represents exception raised when errors occur with Bots
 */
public class BotException extends Exception {
    public BotException() {
        super();
    }

    public BotException(String msg) {
        super(msg);
    }
}
