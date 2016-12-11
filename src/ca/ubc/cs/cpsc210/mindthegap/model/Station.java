package ca.ubc.cs.cpsc210.mindthegap.model;

import ca.ubc.cs.cpsc210.mindthegap.util.LatLon;

import java.util.*;

/**
 * Represents a station on the underground with an id, name, location (lat/lon)
 * set of lines that stop at this station and a list of arrival boards.
 */
public class Station implements Iterable<ArrivalBoard> {
    private String name;
    private LatLon locn;
    private String id;
    private Set<Line> lines;
    private List<ArrivalBoard> arrivalBoards;

    /**
     * Constructs a station with given id, name and location.
     * Set of lines and list of arrival boards are empty.
     *
     * @param id    the id of this station (cannot by null)
     * @param name  name of this station
     * @param locn  location of this station
     */
    public Station(String id, String name, LatLon locn) {
        this.id = id;
        this.name = name;
        this.locn = locn;
        lines = new HashSet<Line>();
        arrivalBoards = new ArrayList<ArrivalBoard>();
    }

    public String getName() {
        return name;
    }

    public LatLon getLocn() {
        return locn;
    }

    public String getID() {
        return id;
    }

    public Set<Line> getLines() {
        return lines;
    }

    public int getNumArrivalBoards() {
        return arrivalBoards.size();
    }

    /**
     * Add line to set of lines with stops at this station.
     *
     * @param line  the line to add
     */
    public void addLine(Line line) {
        if(!lines.contains(line)) {
            lines.add(line);
            line.addStation(this);
        }
    }

    /**
     * Remove line from set of lines with stops at this station
     *
     * @param line the line to remove
     */
    public void removeLine(Line line) {
        if(lines.contains(line)) {
            lines.remove(line);
            line.removeStation(this);
        }
    }

    /**
     * Determine if this station is on a given line
     * @param line  the line
     * @return  true if this station is on given line
     */
    public boolean hasLine(Line line) {
        return lines.contains(line);
    }

    /**
     * Add train arrival travelling on a particular line in a particular direction to this station.
     * Arrival is added to corresponding arrival board based on the line on which it is
     * operating and the direction of travel (as indicated by platform prefix).  If the arrival
     * board for given line and travel direction does not exist, it is created and added to
     * arrival boards for this station.
     *
     * @param line    line on which train is travelling
     * @param arrival the train arrival to add to station
     */
    public void addArrival(Line line, Arrival arrival) {
        for(ArrivalBoard ab : arrivalBoards) {
            if(ab.getLine().equals(line) && ab.getTravelDirn().equals(arrival.getTravelDirn())) {
                ab.addArrival(arrival);
                return;
            }
        }

        ArrivalBoard ab = new ArrivalBoard(line, arrival.getTravelDirn());
        ab.addArrival(arrival);
        arrivalBoards.add(ab);
    }

    /**
     * Remove all arrival boards from this station
     */
    public void clearArrivalBoards() {
        arrivalBoards.clear();
    }

    /**
     * Two stations are equal if their ids are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Station that = (Station) o;

        return id.equals(that.id);

    }

    /**
     * Two stations are equal if their ids are equal
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public Iterator<ArrivalBoard> iterator() {
        return arrivalBoards.iterator();
    }
}
