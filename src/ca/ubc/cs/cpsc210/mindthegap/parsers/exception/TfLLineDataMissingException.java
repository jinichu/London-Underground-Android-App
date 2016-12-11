package ca.ubc.cs.cpsc210.mindthegap.parsers.exception;

/**
 * Represents exception raised when expected data is missing from TfL Line JSON data.
 */
public class TfLLineDataMissingException extends Exception {
    public TfLLineDataMissingException() {
        super();
    }

    public TfLLineDataMissingException(String msg) {
        super(msg);
    }
}
