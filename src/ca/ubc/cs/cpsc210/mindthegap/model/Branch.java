package ca.ubc.cs.cpsc210.mindthegap.model;

import ca.ubc.cs.cpsc210.mindthegap.parsers.BranchStringParser;
import ca.ubc.cs.cpsc210.mindthegap.util.LatLon;


import java.util.Iterator;
import java.util.List;

/**
 * A branch of a line consisting of a path of lat/lon points.
 * These points are used to draw the branch on a map.  Note that the points used to
 * represent the branch are not necessarily co-located with stations.
 */
public class Branch implements Iterable<LatLon> {
    private List<LatLon> pts;

    /**
     * Constructs a Branch by parsing the points that define the branch from
     * the given string.
     *
     * @param lineString  string of coordinates representing points on branch
     */
    public Branch(String lineString) {
        pts = BranchStringParser.parseBranch(lineString);
    }

    /**
     * Get list of all points on this branch
     *
     * @return  all points on branch
     */
    public List<LatLon> getPoints() {
        return pts;
    }

    @Override
    public Iterator<LatLon> iterator() {
        return pts.iterator();
    }

    /**
     * Two branches are equal if their lists of points are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Branch geoPoints = (Branch) o;

        return pts.equals(geoPoints.pts);

    }

    /**
     * Two branches are equal if their lists of points are equal
     */
    @Override
    public int hashCode() {
        return pts.hashCode();
    }
}
