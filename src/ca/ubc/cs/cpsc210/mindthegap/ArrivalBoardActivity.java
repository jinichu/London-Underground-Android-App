package ca.ubc.cs.cpsc210.mindthegap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import ca.ubc.cs.cpsc210.mindthegap.model.ArrivalBoard;
import ca.ubc.cs.cpsc210.mindthegap.ui.ArrivalBoardListFragment;
import ca.ubc.cs.cpsc210.mindthegap.ui.ArrivalBoardListSelectionListener;

/**
 * Activity that allows user to choose arrival board
 */
public class ArrivalBoardActivity extends Activity implements ArrivalBoardListSelectionListener {
    private static final String LOG_TAG = "Arrival Board Tag";
    private static final String AG_TAG = "ArrivalBoardListFragment";
    private ArrivalBoardListFragment fragment;
    private String stnName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.arrival_group_list_layout);

        Intent i = getIntent();
        stnName = i.getStringExtra(getString(R.string.stn_name_key));
        setTitle(stnName);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setHomeButtonEnabled(false);
            actionBar.setSubtitle(R.string.arrival_group_activity_subtitle);
        }

        if(savedInstanceState != null) {
            Log.i(LOG_TAG, "restoring from instance state");
            fragment = (ArrivalBoardListFragment) getFragmentManager()
                    .findFragmentByTag(AG_TAG);
        }
        else if(fragment == null) {
            Log.i(LOG_TAG, "fragment was null");

            fragment = new ArrivalBoardListFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.arrival_group_list, fragment, AG_TAG).commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_to_right);
    }

    /**
     * Start activity that displays list of arrivals on selected arrivals board to user.
     *
     * @param selected  the selected arrivals board
     */
    @Override
    public void onArrivalBoardListItemSelected(ArrivalBoard selected) {
        String lineId = selected.getLine().getId();
        String travelDirn = selected.getTravelDirn();

        Intent i = new Intent(this, ArrivalsActivity.class);
        i.putExtra(getString(R.string.stn_name_key), stnName);
        i.putExtra(getString(R.string.line_id_key), lineId);
        i.putExtra(getString(R.string.travel_dirn_key), travelDirn);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_from_right, android.R.anim.fade_out);
    }
}
