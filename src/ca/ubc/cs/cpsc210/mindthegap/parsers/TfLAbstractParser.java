package ca.ubc.cs.cpsc210.mindthegap.parsers;

/**
 * Abstract parser for data returned by TfL API
 */
abstract class TfLAbstractParser {

    /**
     * Parse short name from full name of station.  Strips off "Underground Station"
     * (or just "Station", if "Underground" not included in name) from end of station name.
     * So,
     * - parseName("Euston Underground Station") produces "Euston"
     * - parseName("Covent Garden Station") produces "Covent Garden"
     *
     * @param fullName  full name of station
     *
     * @return short name of station
     */
    static String parseName(String fullName) {
        String shortName = fullName;
        int indexOfUndergroundStation = fullName.indexOf("Underground Station");
        if(indexOfUndergroundStation > 0)
            shortName = fullName.substring(0, indexOfUndergroundStation).trim();
        else {
            int indexOfStation = fullName.indexOf("Station");
            if(indexOfStation > 0)
                shortName = fullName.substring(0, indexOfStation).trim();
        }
        return shortName;
    }
}
