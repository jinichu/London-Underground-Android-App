package ca.ubc.cs.cpsc210.mindthegap.parsers.exception;

/**
 * Represents exception raised when expected data is missing from TfL Arrivals JSON response.
 */
public class TfLArrivalsDataMissingException extends Exception {
    public TfLArrivalsDataMissingException() {
        super();
    }

    public TfLArrivalsDataMissingException(String msg) {
        super(msg);
    }
}
