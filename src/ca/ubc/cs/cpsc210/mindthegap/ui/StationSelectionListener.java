package ca.ubc.cs.cpsc210.mindthegap.ui;

import ca.ubc.cs.cpsc210.mindthegap.model.Station;

/**
 * Handles user selection of station on map
 */
public interface StationSelectionListener {

    /**
     * Called when user selects a station
     *
     * @param stn   station selected by user
     */
    void onStationSelected(Station stn);
}
