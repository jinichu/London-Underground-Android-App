package ca.ubc.cs.cpsc210.mindthegap.ui;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;
import ca.ubc.cs.cpsc210.mindthegap.R;
import ca.ubc.cs.cpsc210.mindthegap.TfL.AndroidFileDataProvider;
import ca.ubc.cs.cpsc210.mindthegap.TfL.DataProvider;
import ca.ubc.cs.cpsc210.mindthegap.model.*;
import ca.ubc.cs.cpsc210.mindthegap.parsers.TfLLineParser;
import ca.ubc.cs.cpsc210.mindthegap.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a fragment used to display the map to the user
 */
public class MapDisplayFragment extends Fragment implements MapEventsReceiver, IMyLocationConsumer {
    private static final String MDF_TAG = "MDF_TAG";
    /** minimum change in distance to trigger update of user location */
    private static final float MIN_UPDATE_DISTANCE = 50.0f;
    /** zoom level for map */
    private int zoomLevel = 13;
    /** centre of map */
    private GeoPoint mapCentre = new GeoPoint(51.5012385,-0.1269373);
    /** the map view */
    private MapView mapView;
    /** overlays used to plot tube lines */
    private List<Polyline> tubeLineOverlays;
    /** overlay used to display location of user */
    private MyLocationNewOverlay locOverlay;
    /** overlay used to show station markers */
    private RadiusMarkerClusterer stnClusterer;
    /** window displayed when user selects a station */
    private StationInfoWindow stnInfoWindow;
    /** overlay that listens for user initiated events on map */
    private MapEventsOverlay eventsOverlay;
    /** overlay used to display text on a layer above the map */
    private TextOverlay textOverlay;
    /** location provider used to respond to changes in user location */
    private GpsMyLocationProvider locnProvider;
    /** station manager */
    private StationManager stnManager;
    /** marker for station that is nearest to user (null if no such station) */
    private Marker nearestStnMarker;
    /** location listener used to respond to changes in user location */
    private LocationListener locationListener;
    /** last known user location (null if not available) */
    private Location lastKnownFromInstanceState;
    /** list of tube lines to be displayed on map */
    private List<Line> tubeLines;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(MDF_TAG, "onCreate");
        eventsOverlay = new MapEventsOverlay(getActivity(), this);
        locnProvider = new GpsMyLocationProvider(getActivity());
        locnProvider.setLocationUpdateMinDistance(MIN_UPDATE_DISTANCE);
        stnManager = StationManager.getInstance();
        nearestStnMarker = null;
        tubeLines = parseLines();
        tubeLineOverlays = new ArrayList<Polyline>();
        stnClusterer = new RadiusMarkerClusterer(getActivity());
        Drawable clusterIconD = getResources().getDrawable(R.drawable.stn_cluster);
        Bitmap clusterIcon = ((BitmapDrawable) clusterIconD).getBitmap();
        stnClusterer.setIcon(clusterIcon);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        locationListener = (LocationListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final int TILE_SIZE = 256;
        Log.i(MDF_TAG, "onCreateView");

        if (savedInstanceState != null) {
            Log.i(MDF_TAG, "restoring from instance state");
            mapCentre = new GeoPoint(savedInstanceState.getDouble(getString(R.string.lat_key)),
                    savedInstanceState.getDouble(getString(R.string.lon_key)));
            zoomLevel = savedInstanceState.getInt(getString(R.string.zoom_key));
            lastKnownFromInstanceState = savedInstanceState.getParcelable(getString(R.string.locn_key));
        }
        else {
            Log.i(MDF_TAG, "savedInstanceState is null - new fragment created");
        }

        if (mapView == null) {
            mapView = new MapView(getActivity(), TILE_SIZE);
            mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
            mapView.setClickable(true);
            mapView.setBuiltInZoomControls(true);
            mapView.setMapListener(new TubeLineListener());

            GpsMyLocationProvider mapLocnProvider = new GpsMyLocationProvider(getActivity());
            mapLocnProvider.setLocationUpdateMinDistance(MIN_UPDATE_DISTANCE);
            locOverlay = new MyLocationNewOverlay(getActivity(), mapLocnProvider, mapView);
            stnInfoWindow = new StationInfoWindow((StationSelectionListener) getActivity(), mapView);
            createTextOverlay();

            // set default view for map
            final IMapController mapController = mapView.getController();

            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    else
                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    mapController.setZoom(zoomLevel);
                    mapController.setCenter(mapCentre);
                }
            });
        }

        markStations();
        plotLines(tubeLines);

        return mapView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(MDF_TAG, "onSaveInstanceState");

        outState.putDouble(getString(R.string.lat_key), mapView.getMapCenter().getLatitude());
        outState.putDouble(getString(R.string.lon_key), mapView.getMapCenter().getLongitude());
        outState.putInt(getString(R.string.zoom_key), mapView.getZoomLevel());

        // if location has been updated, use it; otherwise use last known locn restored from instance state
        Location lastKnown = locnProvider.getLastKnownLocation();
        if(lastKnown != null) {
            outState.putParcelable(getString(R.string.locn_key), locnProvider.getLastKnownLocation());
        }
        else {
            outState.putParcelable(getString(R.string.locn_key), lastKnownFromInstanceState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(MDF_TAG, "onResume");
        locnProvider.startLocationProvider(this);
        locOverlay.enableMyLocation();
        locOverlay.enableFollowLocation();
        mapView.setBuiltInZoomControls(true);

        Location lastKnownLocation = locnProvider.getLastKnownLocation();
        if (lastKnownLocation != null) {
            Log.i(MDF_TAG, "Restored from last known location");
            handleLocationChange(lastKnownLocation);
        }
        else if(lastKnownFromInstanceState != null) {
            Log.i(MDF_TAG, "Restored from instance state");
            handleLocationChange(lastKnownFromInstanceState);
            // force location overlay to redraw location icon
            locOverlay.onLocationChanged(lastKnownFromInstanceState, null);
        }
        else {
            Log.i(MDF_TAG, "Location cannot be recovered");
        }
        updateOverlays();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(MDF_TAG, "onPause");
        locOverlay.disableMyLocation();
        locnProvider.stopLocationProvider();
        mapView.setBuiltInZoomControls(false);
    }

    /**
     * Clear overlays and add route, station, location and events overlays
     */
    private void updateOverlays() {
        OverlayManager om = mapView.getOverlayManager();
        om.clear();
        om.addAll(tubeLineOverlays);
        om.add(stnClusterer);
        om.add(locOverlay);
        om.add(textOverlay);
        om.add(0, eventsOverlay);

        mapView.invalidate();
    }

    /**
     * Create text overlay to display credit to TfL
     */
    private void createTextOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(getActivity());
        textOverlay = new TextOverlay(rp, getResources().getString(R.string.tfl_open_data));
    }

    /**
     * Parse line data from files and add all stations on lines parsed to station manager.
     * Files containing line data are specified in LineMetaData enumeration.
     *
     * @return             list of lines parsed from files
     */
    private List<Line> parseLines() {
        List<Line> lines = new ArrayList<Line>();

        for(LineResourceData lineResourceData : LineResourceData.values()) {
            Line line = parseLine(lineResourceData);
            if(line != null) {
                stnManager.addStationsOnLine(line);
                lines.add(line);
            }
        }

        return lines;
    }

    /**
     * Parse single line from file
     *
     * @param lineResourceData   meta data for line to be parsed
     * @return               line parsed from file
     */
    private Line parseLine(LineResourceData lineResourceData) {
        String fileName = lineResourceData.getFileName();
        String filePrefix = fileName.substring(0, fileName.lastIndexOf("."));

        DataProvider dataProvider = new AndroidFileDataProvider(getActivity(), filePrefix);
        Line line = null;

        try {
            String lineData = dataProvider.dataSourceToString();
            line = TfLLineParser.parseLine(lineResourceData, lineData);
        } catch (Exception e) {
            Log.e(MDF_TAG, e.getMessage(), e);
            Toast.makeText(getActivity(), "Unable to display " + lineResourceData + " line", Toast.LENGTH_LONG).show();
        }

        return line;
    }

    /**
     * Plot a list of lines onto map using each line's colour
     *
     * @param tubeLines   list of lines to be plotted
     */
    private void plotLines(List<Line> tubeLines) {
        for (Line next : tubeLines) {

            for (Branch branch : next.getBranches()) {
                List<GeoPoint> geoPoints = new ArrayList<>();
                for (LatLon latLon : branch.getPoints()) {
                    GeoPoint geoPoint = new GeoPoint(latLon.getLatitude(), latLon.getLongitude());
                    geoPoints.add(geoPoint);
                }

                Polyline polyLine = new Polyline(mapView.getContext());

                polyLine.setPoints(geoPoints);
                polyLine.setWidth(getLineWidth(zoomLevel));
                polyLine.setColor(next.getColour());
                tubeLineOverlays.add(polyLine);

            }
        }
    }


    /**
     * Mark all stations in station manager onto map.
     */
    private void markStations() {
        Drawable stnIconDrawable = getResources().getDrawable(R.drawable.stn_icon);

        for (Station next : stnManager) {

            GeoPoint position = new GeoPoint(next.getLocn().getLatitude(), next.getLocn().getLongitude());

            Marker marker = new Marker(mapView);
            marker.setInfoWindow(stnInfoWindow);
            marker.setIcon(stnIconDrawable);
            marker.setTitle(next.getName());
            marker.setPosition(position);
            marker.setRelatedObject(next);

            stnClusterer.add(marker);
        }
    }

    /**
     * Update marker of nearest station (called when user's location has changed).  If nearest is null,
     * no station is marked as the nearest station.
     *
     * @param nearest   station nearest to user's location (null if no station within StationManager.RADIUS metres)
     */
    private void updateMarkerOfNearest(Station nearest) {
        Drawable stnIconDrawable = getResources().getDrawable(R.drawable.stn_icon);
        Drawable closestStnIconDrawable = getResources().getDrawable(R.drawable.closest_stn_icon);

        // set icon to default
        if (nearest == null) {
            nearestStnMarker.setIcon(stnIconDrawable);
        }

        if (nearest != null) {
            stnClusterer.getItems().clear();

            for (Station next: stnManager) {
                if (next.equals(nearest)) {

                    GeoPoint position = new GeoPoint(nearest.getLocn().getLatitude(), nearest.getLocn().getLongitude());

                    nearestStnMarker = new Marker(mapView);
                    nearestStnMarker.setIcon(closestStnIconDrawable);
                    nearestStnMarker.setInfoWindow(stnInfoWindow);
                    nearestStnMarker.setPosition(position);
                    nearestStnMarker.setTitle(nearest.getName());
                    nearestStnMarker.setRelatedObject(nearest);

                    if (!stnClusterer.getItems().contains(nearestStnMarker)) {
                        stnClusterer.add(nearestStnMarker);
                    }

                    stnClusterer.invalidate();
                }

                else {
                    GeoPoint position = new GeoPoint(next.getLocn().getLatitude(), next.getLocn().getLongitude());

                    Marker marker = new Marker(mapView);
                    marker.setInfoWindow(stnInfoWindow);
                    marker.setIcon(stnIconDrawable);
                    marker.setTitle(next.getName());
                    marker.setPosition(position);
                    marker.setRelatedObject(next);

                    stnClusterer.add(marker);
                }
            }
        }
    }


    /**
     * Find nearest station to user, update nearest station text view and update markers on user location change
     *
     * @param location   the location of the user
     */
    private void handleLocationChange(Location location) {

        LatLon latLon = new LatLon(location.getLatitude(), location.getLongitude());
        Station nearest;
        nearest = stnManager.findNearestTo(latLon);

        locationListener.onLocationChanged(nearest);
        updateMarkerOfNearest(nearest);
    }


    /**
     * Get width of line used to plot tube line based on zoom level
     * @param zoomLevel   the zoom level of the map
     * @return            width of line used to plot tube line
     */
    private float getLineWidth(int zoomLevel) {
        if(zoomLevel > 14)
            return 10.0f;
        else if(zoomLevel > 10)
            return 5.0f;
        else
            return 2.0f;
    }

    /**
     * Close info windows when user taps map.
     */
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        StationInfoWindow.closeAllInfoWindowsOn(mapView);
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        return false;
    }

    /**
     * Called when user's location has changed - handle location change and repaint map
     *
     * @param location               user's location
     * @param iMyLocationProvider    location provider
     */
    @Override
    public void onLocationChanged(Location location, IMyLocationProvider iMyLocationProvider) {
        Log.i(MDF_TAG, "onLocationChanged");

        handleLocationChange(location);
        mapView.invalidate();
    }

    /**
     * Custom listener for zoom events.  Changes width of line used to plot
     * tube line based on zoom level.
     */
    private class TubeLineListener implements MapListener {

        @Override
        public boolean onScroll(ScrollEvent scrollEvent) {
            return false;
        }

        @Override
        public boolean onZoom(ZoomEvent zoomEvent) {
            tubeLineOverlays.clear();
            plotLines(tubeLines);
            updateOverlays();
            return false;
        }
    }
}
