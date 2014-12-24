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
import android.location.Location;
import android.location.LocationManager;
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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;


public class CompassActivity extends FragmentActivity implements ListDialog.Callbacks{

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

        List<LocationItem> result = new LinkedList<LocationItem>();

        Location pv = new Location("");
        pv.setLatitude(51.672318);
        pv.setLongitude(39.154473);
        result.add(new LocationItem(pv, "Pivzavod", currentLocation));

        Location gum = new Location("");
        gum.setLatitude(51.669349);
        gum.setLongitude(39.151828);
        result.add(new LocationItem(gum, "6 gymnasium", currentLocation));

        for (int i = 0; i < 100; i++) {
            Location cicusLocation = new Location("");
            double circusLatitude = 51.656608;
            cicusLocation.setLatitude(circusLatitude);
            double circusLongitude = 39.185975;
            cicusLocation.setLongitude(circusLongitude);
            LocationItem item = new LocationItem(cicusLocation, "Circus", currentLocation);
            result.add(item);
        }

        Location mdm = new Location("");
        mdm.setLatitude(51.661678);
        mdm.setLongitude(39.203636);
        LocationItem item = new LocationItem(mdm, "MDM", currentLocation);
        result.add(item);
        LocationItem[] res = new LocationItem[result.size()];
        return result.toArray(res);
    }

    @Override
    public void onItemSelected(LocationItem locationItem) {

        model.setDecisionPoint(locationItem.getCurrentLocation());
        model.setDecisionPointLocationItems(getDestinations());
        model.setDecisionTime(new Date());
        model.setDestinationLocation(locationItem.getLocation());
        model.setDestinationName(locationItem.getName());


    }

    public Model getModel() {
        return model;
    }

    public static class PlaceholderFragment extends Fragment  {

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
            compassTimer.schedule(new CompassTimerTask((CompassActivity)getActivity(), this, model), 0, REPEATER_COMPASS_TIMER);
         }

        public void openListLocations() {
            if (model.getData().getLocation() != null) {
                FragmentManager fm = this.getFragmentManager();
                ListDialog listDialog = new ListDialog();
                listDialog.show(fm, "list_dialog");
            } else {
                Toast.makeText(getActivity(), this.getString(R.string.defining), Toast.LENGTH_SHORT).show();
            }
        }

        public Button getNotificationButton() {
            return notificationButton;
        }

        public TextView getTextOutput(){
            return textOutput;
        }

        public Handler getHandler(){
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

        /** Called when leaving the activity */
        @Override
        public void onPause() {
            if (mAdView != null) {
                mAdView.pause();
            }
            super.onPause();
        }

        /** Called when returning to the activity */
        @Override
        public void onResume() {
            super.onResume();
            if (mAdView != null) {
                mAdView.resume();
            }
        }

        /** Called before the activity is destroyed */
        @Override
        public void onDestroy() {
            if (mAdView != null) {
                mAdView.destroy();
            }
            super.onDestroy();
        }

    }

}
