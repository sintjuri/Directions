package com.onettm.directions;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;


public class CompassActivity extends FragmentActivity implements ListDialog.Callbacks {

    private Model model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new Model();
        setContentView(R.layout.main);
        loadPref();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

  /*
   * Because it's onlt ONE option in the menu.
   * In order to make it simple, We always start SettingsActivity
   * without checking.
   */

        Intent intent = new Intent();
        intent.setClass(CompassActivity.this, SettingsActivity.class);
        startActivityForResult(intent, 0);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadPref();
    }

    private void loadPref() {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String zonePreference = mySharedPreferences.getString("pref_radius", "5");
        //TODO initialize preference
        //prefCheckBox.setChecked(my_checkbox_preference);

    }

    @Override
    public LocationItem[] getDestinations() {
        //TODO change to real implementation
        System.err.println("TIME getDestinations 1 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        Location currentLocation = model.getData().getLocation();
        System.err.println("TIME getDestinations 2 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        DistanceComparator<LocationItem> dc = new DistanceComparator<LocationItem>(currentLocation);

        System.err.println("TIME getDestinations 3 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        long cur_lat = (long) (currentLocation.getLatitude() * 10000000);
        long cur_lon = (long) (currentLocation.getLongitude() * 10000000);
        System.err.println("TIME getDestinations 4 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        Cursor c = DirectionsApplication.getInstance().getDb().rawQuery(String.format("select tag.v, node.lat, node.lon from node \n" +
                "join tag_node on node.id=tag_node.node\n" +
                "join tag on tag.id=tag_node.tag\n" +
                "where tag.k='name'\n" +
                "and node.lat > %d - 400000 and node.lat < %d + 400000\n" +
                "and node.lon > %d - 700000 and node.lon < %d + 700000", cur_lat, cur_lat, cur_lon, cur_lon), null);

        System.err.println("TIME getDestinations 5 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        c.moveToFirst();
        System.err.println("TIME getDestinations 6 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        Set<LocationItem> result = new HashSet<LocationItem>();
        System.err.println("TIME getDestinations 7 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        while (c.moveToNext()) {
            String name = c.getString(0);
            double lat = c.getDouble(1) / 10000000;
            double lon = c.getDouble(2) / 10000000;
            Location pv = new Location("");
            pv.setLatitude(lat);
            pv.setLongitude(lon);
            result.add(new LocationItem(pv, name, currentLocation));
        }
        System.err.println("TIME getDestinations 8 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        c.close();
        System.err.println("TIME getDestinations 9 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));

        Cursor c2 = DirectionsApplication.getInstance().getDb().rawQuery(String.format("select tag.v, node.lat, node.lon, way.id from node \n" +
                "join way on way.id = node.way\n" +
                "join tag_way on way.id = tag_way.way\n" +
                "join tag on tag.id=tag_way.tag\n" +
                "where tag.k='name'\n" +
                "and node.lat > %d - 400000 and node.lat < %d + 400000\n" +
                "and node.lon > %d - 700000 and node.lon < %d + 700000", cur_lat, cur_lat, cur_lon, cur_lon), null);

        System.err.println("TIME getDestinations 10 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        c2.moveToFirst();
        System.err.println("TIME getDestinations 11 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        Map<Integer, LocationItem> wayNodes = new HashMap<Integer, LocationItem>();
        System.err.println("TIME getDestinations 12 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));

        while (c2.moveToNext()) {
            String name = c2.getString(0);
            double lat = c2.getDouble(1) / 10000000;
            double lon = c2.getDouble(2) / 10000000;
            Integer way = c2.getInt(3);
            Location pv = new Location("");
            pv.setLatitude(lat);
            pv.setLongitude(lon);
            LocationItem newItem = new LocationItem(pv, name, currentLocation);
            LocationItem existingItem = wayNodes.get(way);
            if (existingItem != null) {
                if (dc.compare(existingItem, newItem) < 0) continue;
            } else wayNodes.put(c2.getInt(3), newItem);
        }
        System.err.println("TIME getDestinations 13 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        c2.close();
        System.err.println("TIME getDestinations 14 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));

        result.addAll(wayNodes.values());
        System.err.println("TIME getDestinations 15 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        LocationItem[] res = new LocationItem[result.size()];
        System.err.println("TIME getDestinations 16 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        result.toArray(res);

        System.err.println("TIME getDestinations 17 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        Arrays.sort(res, dc);
        System.err.println("TIME getDestinations 18 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        return res;
    }

    static class DistanceComparator<L extends LocationItem> implements java.util.Comparator<L> {

        private final Location currentLoc;

        DistanceComparator(Location currentLoc) {
            this.currentLoc = currentLoc;
        }

        @Override
        public int compare(L l, L l2) {
            if (l == l2) return 0;
            if (l == null) return -1;
            if (l2 == null) return 1;

            if (l.getLocation() == null) return -1;
            if (l2.getLocation() == null) return 1;

            float d = currentLoc.distanceTo(l.getLocation());
            float d2 = currentLoc.distanceTo(l2.getLocation());
            return Float.compare(d, d2);
        }
    }

    ;

    @Override
    public void onItemSelected(LocationItem locationItem, LocationItem[] destinations) {

        model.setDecisionPoint(locationItem.getCurrentLocation());
        model.setDecisionPointLocationItems(destinations);
        model.setDestinationLocation(locationItem.getLocation());
        model.setDestinationName(locationItem.getName());


    }

    public Model getModel() {
        return model;
    }

    public static class PlaceholderFragment extends Fragment {

        private static final long REPEATER_COMPASS_TIMER = 10;
        public static final int REPEATER_FIRST_TIME_OPEN_DIALOG = 1000;
        private Model model;

        private CompassSurface surface;
        private LinearLayout surfaceContainer;
        private Button notificationButton;
        private TextView textOutput;
        private Timer compassTimer;

        private SensorListener compass;
        private boolean questionMarkRendered;
        private Handler handler;

        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            menu.clear();
            inflater.inflate(R.menu.menu, menu);
        }

        /**
         * Function to show settings alert dialog
         */
        public void showSettingsAlert() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            // Setting Dialog Title
            alertDialog.setTitle(R.string.gps_settings);

            // Setting Dialog Message
            alertDialog.setMessage(R.string.enable_gps);

            // Setting Icon to Dialog
            //alertDialog.setIcon(R.drawable.delete);

            // On pressing Settings button
            alertDialog.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });

            // on pressing cancel button
            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_my, container, false);
            this.model = ((CompassActivity) getActivity()).getModel();

            surfaceContainer = (LinearLayout) rootView.findViewById(R.id.compassSurfaceContainer);

            notificationButton = (Button) rootView.findViewById(R.id.notificationButton);
            textOutput = (TextView) rootView.findViewById(R.id.textOutput);
            notificationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    surface.pauseAnimation();
                    Log.d("PROCESS!!", "openListLocations");
                    openListLocations();
                    surface.resumeAnimation();
                }
            });

            compass = new SensorListener(getActivity(), model);
            Timer firstTimeOnenDialogTimer = new Timer();
            firstTimeOnenDialogTimer.schedule(new CheckFirstTimeToOpenDialogTask(this, model), 0, REPEATER_FIRST_TIME_OPEN_DIALOG);

            handler = new Handler();

            return rootView;
        }

        @Override
        public void onPause() {
            // unregister from the directions to prevent undue battery drain
            compass.unregisterSensors();
            // stop the animation
            surface.stopAnimation();
            compassTimer.cancel();
            // call the superclass
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();

            // register to receive events from the directions
            compass.registerSensors();

            if (!compass.isAccelerometerAvailable()) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.accelerometer_not_available)
                        .show();
            }

            if (!compass.isMagnetometerAvailable()) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.magnetometer_not_available)
                        .show();
            }

            if (!compass
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showSettingsAlert();
            }
            surface = new CompassSurface(this, model);
            // add the directions
            surfaceContainer.removeAllViews();
            surfaceContainer.addView(surface);

            compassTimer = new Timer();
            compassTimer.schedule(new CompassTimerTask((CompassActivity) getActivity(), this, model), 0, REPEATER_COMPASS_TIMER);
        }

        public void openListLocations() {
            if (model.getData().getLocation() != null) {
                ListDialog listDialog = new ListDialog();
                FragmentManager fm = getFragmentManager();
                listDialog.show(fm, "list_dialog");
            } else {
                Toast.makeText(getActivity(), this.getString(R.string.defining), Toast.LENGTH_SHORT).show();
            }
        }

        public Button getNotificationButton() {
            return notificationButton;
        }

        public TextView getTextOutput() {
            return textOutput;
        }

        public Handler getHandler() {
            return handler;
        }

        public boolean isQuestionMarkRendered() {
            return questionMarkRendered;
        }

        public void setQuestionMarkRendered(boolean questionMarkRendered) {
            this.questionMarkRendered = questionMarkRendered;
        }

    }

    /**
     * This class makes the ad request and loads the ad.
     */
    public static class AdFragment extends Fragment {

        private AdView mAdView;

        public AdFragment() {
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
            // values/strings.xml.
            mAdView = (AdView) getView().findViewById(R.id.adView);

            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
            AdRequest adRequest = new AdRequest.Builder()
//                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("74760DA0E4A7D8383E8EC5268A2486CF")
                    .build();

            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_ad, container, false);
        }

        /**
         * Called when leaving the activity
         */
        @Override
        public void onPause() {
            if (mAdView != null) {
                mAdView.pause();
            }
            super.onPause();
        }

        /**
         * Called when returning to the activity
         */
        @Override
        public void onResume() {
            super.onResume();
            if (mAdView != null) {
                mAdView.resume();
            }
        }

        /**
         * Called before the activity is destroyed
         */
        @Override
        public void onDestroy() {
            if (mAdView != null) {
                mAdView.destroy();
            }
            super.onDestroy();
        }

    }

}
