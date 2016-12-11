package ca.ubc.cs.cpsc210.mindthegap.parsers;

import ca.ubc.cs.cpsc210.mindthegap.model.Arrival;
import ca.ubc.cs.cpsc210.mindthegap.model.Line;
import ca.ubc.cs.cpsc210.mindthegap.model.Station;
import ca.ubc.cs.cpsc210.mindthegap.parsers.exception.TfLArrivalsDataMissingException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * A parser for the data returned by the TfL station arrivals query
 */
public class TfLArrivalsParser extends TfLAbstractParser {

    /**
     * Parse arrivals from JSON response produced by TfL query.  All parsed arrivals are
     * added to given station assuming that corresponding JSON object as all of:
     * timeToStation, platformName, lineID and one of destinationName or towards.  If
     * any of the aforementioned elements is missing, the arrival is not added to the station.
     *
     * @param stn             station to which parsed arrivals are to be added
     * @param jsonResponse    the JSON response produced by TfL
     * @throws JSONException  when JSON response does not have expected format
     * @throws TfLArrivalsDataMissingException  when all arrivals are missing at least one of the following:
     * <ul>
     *     <li>timeToStation</li>
     *     <li>platformName</li>
     *     <li>lineId</li>
     *     <li>destinationName and towards</li>
     * </ul>
     */
    public static void parseArrivals(Station stn, String jsonResponse)
            throws JSONException, TfLArrivalsDataMissingException {
        JSONArray arrivals = new JSONArray(jsonResponse);
        int countMissing = 0;

        for(int index = 0; index < arrivals.length(); index++) {
            JSONObject jsonArrival = arrivals.getJSONObject(index);
            try {
                addArrivalToStn(stn, jsonArrival);
            } catch(TfLArrivalsDataMissingException e) {
                countMissing++;
            }
        }

        if (countMissing == arrivals.length()) {
            throw new TfLArrivalsDataMissingException("All arrivals missing expected data component");
        }
    }

    /**
     * Add arrival to station
     *
     * @param stn              station to which arrival is to be added
     * @param jsonArrival      JSON object representing arrival
     * @throws JSONException   when JSON object does not have expected format
     * @throws TfLArrivalsDataMissingException  when expected data component is missing from JSON data
     */
    private static void addArrivalToStn(Station stn, JSONObject jsonArrival)
            throws JSONException, TfLArrivalsDataMissingException {
        if (! (jsonArrival.has("timeToStation") && jsonArrival.has("platformName") && jsonArrival.has("lineId")
                && (jsonArrival.has("destinationName") || jsonArrival.has("towards"))))
            throw new TfLArrivalsDataMissingException("Required data missing from JSON response");

        int timeToStation = jsonArrival.getInt("timeToStation");

        String destination;
        if(jsonArrival.has("destinationName")) {
            destination = parseName(jsonArrival.getString("destinationName"));
        }
        else {
            destination = jsonArrival.getString("towards");
        }

        String platform = jsonArrival.getString("platformName");
        Line line = getLineFromId(stn, jsonArrival.getString("lineId"));

        stn.addArrival(line, new Arrival(timeToStation, destination, platform));
    }

    /**
     * Look up line operating through given station with given id; produce null if no such line is found.
     *
     * @param stn       the station
     * @param lineId    the line id
     * @return          line operating through station with given line id or null if no such line is found
     */
    private static Line getLineFromId(Station stn, String lineId) {
        Set<Line> lines = stn.getLines();

        for(Line next : lines) {
            if(next.getId().equals(lineId))
                return next;
        }

        return null;
    }
}
