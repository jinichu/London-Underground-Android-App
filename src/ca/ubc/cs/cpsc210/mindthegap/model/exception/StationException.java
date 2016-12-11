package ca.ubc.cs.cpsc210.mindthegap.model.exception;

/**
 * Represents exception raised when errors occur with Stations
 */
public class StationException extends Exception {
    public StationException() {
        super();
    }

    public StationException(String msg) {
        super(msg);
    }
}

