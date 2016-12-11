package ca.ubc.cs.cpsc210.mindthegap.parsers;

import ca.ubc.cs.cpsc210.mindthegap.model.*;
import ca.ubc.cs.cpsc210.mindthegap.parsers.exception.TfLLineDataMissingException;
import ca.ubc.cs.cpsc210.mindthegap.util.LatLon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A parser for the data returned by TFL line route query
 */
public class TfLLineParser extends TfLAbstractParser {

    /**
     * Parse line from JSON response produced by TfL.  No stations added to line if TfLLineDataMissingException
     * is thrown.
     *
     * @param lmd              line meta-data
     * @return                 line parsed from TfL data
     * @throws JSONException   when JSON data does not have expected format
     * @throws TfLLineDataMissingException when
     * <ul>
     *  <li> JSON data is missing lineName, lineId or stopPointSequences elements </li>
     *  <li> or, for a given sequence: </li>
     *    <ul>
     *      <li> the stopPoint array is missing, or </li>
     *      <li> all station elements are missing one of name, lat, lon or stationId elements </li>
     *    </ul>
     * </ul>
     */
    public static Line parseLine(LineResourceData lmd, String jsonResponse)
            throws JSONException, TfLLineDataMissingException {
        JSONObject rootJSON = new JSONObject(jsonResponse);

        if (!(rootJSON.has("lineName") && rootJSON.has("lineId"))) {
            throw new TfLLineDataMissingException("JSON data missing required data elements");
        }

        String lineName = rootJSON.getString("lineName");
        String lineId = rootJSON.getString("lineId");
        Line tubeLine = new Line(lmd, lineId, lineName);

        addBranches(rootJSON, tubeLine);
        addStations(rootJSON, tubeLine);

        return tubeLine;
    }

    /**
     * Add stations parsed from JSON response to a given line
     *
     * @param rootJSON        the JSON response produced by TfL query
     * @param tubeLine        the line to which stations are to be added
     * @throws JSONException  when JSON data does not have expected format
     * @throws TfLLineDataMissingException  when JSON data is missing expected element (for stopPointSequences
     * data, exception thrown if ANY ONE of the sequences is completely missing data)
     */
    private static void addStations(JSONObject rootJSON, Line tubeLine)
            throws JSONException, TfLLineDataMissingException {
        if (!rootJSON.has("stopPointSequences")) {
            throw new TfLLineDataMissingException("stopPointSequences missing from JSON response");
        }

        JSONArray sequences = rootJSON.getJSONArray("stopPointSequences");

        try {
            for (int index = 0; index < sequences.length(); index++) {
                JSONObject sequence = sequences.getJSONObject(index);
                addSequenceToLine(sequence, tubeLine);
            }
        } catch(TfLLineDataMissingException e) {
            tubeLine.clearStations();
            throw e;
        }
    }

    /**
     * Add sequence of stop points to line
     *
     * @param sequence        JSON object containing sequence of stop points
     * @param tubeLine        line to which sequence of stop points is to be added
     * @throws JSONException  when JSON data does not have expected format
     * @throws TfLLineDataMissingException  when JSON data is missing expected element (for stopPoint
     * data, exception thrown only if ALL stopPoints have missing data)
     */
    private static void addSequenceToLine(JSONObject sequence, Line tubeLine)
            throws JSONException, TfLLineDataMissingException {
        if (!sequence.has("stopPoint")) {
            throw new TfLLineDataMissingException("stopPoint array missing from stopPointSequences");
        }

        int countMissing = 0;
        JSONArray stopPoints = sequence.getJSONArray("stopPoint");

        for(int index = 0; index < stopPoints.length(); index++) {
            JSONObject stopPoint = stopPoints.getJSONObject(index);
            try {
                addStationToLine(tubeLine, stopPoint);
            }
            catch(TfLLineDataMissingException e) {
                countMissing++;
            }
        }

        if (countMissing == stopPoints.length()) {
            throw new TfLLineDataMissingException("All stations missing required data");
        }
    }

    /**
     * Add stations to line
     *
     * @param tubeLine        line to which stations are to be added
     * @param jsonStation     JSOn object representing station (stop point)
     * @throws JSONException  when JSON data does not have expected format
     * @throws TfLLineDataMissingException  when JSON data is missing expected element
     */
    private static void addStationToLine(Line tubeLine, JSONObject jsonStation)
            throws JSONException, TfLLineDataMissingException {
        if (!(jsonStation.has("name") && jsonStation.has("lat") && jsonStation.has("lon")
                && jsonStation.has("stationId"))) {
            throw new TfLLineDataMissingException("Required data missing from stopPoint");
        }

        String fullName = jsonStation.getString("name");
        String shortName = parseName(fullName);
        double lat = jsonStation.getDouble("lat");
        double lon = jsonStation.getDouble("lon");
        String id = jsonStation.getString("stationId");
        LatLon locn = new LatLon(lat, lon);

        Station lookup = StationManager.getInstance().getStationWithId(id);
        if(lookup != null) {
            tubeLine.addStation(lookup);
        }
        else {
            tubeLine.addStation(new Station(id, shortName, locn));
        }
    }

    /**
     * Add branches to tube line
     *
     * @param rootJSON        the JSON object that contains the branch data
     * @param tubeLine        the line to which branches are to be added
     * @throws JSONException  when JSON data does not have expected format
     * @throws TfLLineDataMissingException  when JSON data is missing expected element
     */
    private static void addBranches(JSONObject rootJSON, Line tubeLine)
            throws JSONException, TfLLineDataMissingException {
        if (!rootJSON.has("lineStrings")) {
            throw new TfLLineDataMissingException("Required data missing from JSON response");
        }

        JSONArray lineStrings = rootJSON.getJSONArray("lineStrings");

        for(int index = 0; index < lineStrings.length(); index++) {
            String branchString = lineStrings.getString(index);
            tubeLine.addBranch(new Branch(branchString));
        }
    }

}
