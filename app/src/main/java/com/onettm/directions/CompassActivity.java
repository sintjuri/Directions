/*******************************************************************************
 * NiceCompass
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.onettm.directions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;


public class CompassActivity extends FragmentActivity implements ListDialog.Callbacks {

    private Model model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new Model();
        setContentView(R.layout.main);
    }

    @Override
    public LocationItem[] getDestinations() {
        //TODO change to real implementation
        Location currentLocation = model.getData().getLocation();

        long cur_lat = (long)(currentLocation.getLatitude()*10000000);
        long cur_lon = (long) (currentLocation.getLongitude()*10000000);
        Cursor c = DirectionsApplication.getInstance().getDb().rawQuery(String.format("select tag.v, node.lat, node.lon from node \n" +
                "join tag_node on  node.id=tag_node.node\n" +
                "join tag on tag.id=tag_node.tag\n" +
                "where tag.k='name'\n" +
                "and node.lat > %d - 400000 and node.lat < %d + 400000\n" +
                "and node.lon > %d - 700000 and node.lon < %d + 700000", cur_lat, cur_lat, cur_lon, cur_lon), null);

        c.moveToFirst();
        List<LocationItem> result = new LinkedList<LocationItem>();
        while(c.moveToNext()){
            String name = c.getString(0);
            double lat = c.getDouble(1)/  10000000;
            double lon = c.getDouble(2)/10000000;
            Location pv = new Location("");
            pv.setLatitude(lat);
            pv.setLongitude(lon);
            result.add(new LocationItem(pv, name, currentLocation));
        }
        LocationItem[] res = new LocationItem[result.size()];
        result.toArray(res);

        DistanceComparator<LocationItem> dc = new DistanceComparator<LocationItem>(currentLocation);
        Arrays.sort(res, dc);
        return res;
    }

    static class DistanceComparator<L extends LocationItem>  implements java.util.Comparator<L>{

        private final Location currentLoc;

        DistanceComparator(Location currentLoc) {
            this.currentLoc = currentLoc;
        }

        @Override
        public int compare(L l, L l2) {
            if(l==l2) return 0;
            if (l == null) return -1;
            if (l2 == null) return 1;

            if(l.getLocation()== null) return -1;
            if(l2.getLocation() == null) return 1;

            float d = currentLoc.distanceTo(l.getLocation());
            float d2 = currentLoc.distanceTo(l2.getLocation());
            return Float.compare(d, d2);
        }
    };

    @Override
    public void onItemSelected(LocationItem locationItem) {

        model.setDecisionPoint(locationItem.getCurrentLocation());
        model.setDecisionPointLocationItems(getDestinations());
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
                    openListLocations();
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
                AsyncTask at = new AsyncTask() {

                    ListDialog listDialog;

                    @Override
                    protected Object doInBackground(Object[] objects) {
                        return listDialog = new ListDialog();
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        FragmentManager fm = getFragmentManager();

                        listDialog.show(fm, "list_dialog");
                    }
                };
                at.execute();
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
