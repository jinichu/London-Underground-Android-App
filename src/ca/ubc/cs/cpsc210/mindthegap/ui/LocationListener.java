package ca.ubc.cs.cpsc210.mindthegap.ui;

import ca.ubc.cs.cpsc210.mindthegap.model.Station;

/**
 * Handles changes in user location
 */
public interface LocationListener {

    /**
     * Called when the user's location has changed
     *
     * @param nearest  station that is nearest to user (null if no station within StationManager.RADIUS metres)
     */
    void onLocationChanged(Station nearest);
}
