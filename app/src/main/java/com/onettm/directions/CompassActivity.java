package com.onettm.directions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Observable;
import java.util.Observer;


public class CompassActivity extends Activity implements ListDialog.Callbacks {

    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        loadPref();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        handler = new Handler();
    }

    public Handler getHandler() {
        return handler;
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
    public void onItemSelected(LocationItem locationItem, LocationItem[] destinations) {

        Model model = DirectionsApplication.getInstance().getModel();
        model.setDecisionPoint(locationItem.getCurrentLocation());
        model.setDecisionPointLocationItems(destinations);
        model.setDestinationLocation(locationItem.getLocation());
        model.setDestinationName(locationItem.getName());


    }

    public static class ButtonsFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.buttons, container, false);
            final Button listButton = (Button) rootView.findViewById(R.id.openListButton);

            listButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //surface.pauseAnimation();
                    Log.d("PROCESS!!", "listButton.setOnClickListener");
                    openListLocations();
                    //surface.resumeAnimation();
                }
            });

            final Button updateButton = (Button) rootView.findViewById(R.id.updateListButton);
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("PROCESS!!", "updateButton.setOnClickListener");
                    DirectionsApplication.getInstance().getLocationsManager().invalidate();
                }
            });

            Observer buttonsUpdater = new Observer() {
                @Override
                public void update(Observable observable, Object data) {
                    String listButtonText = getString(R.string.show_list, DirectionsApplication.getInstance().getLocationsManager().getLocationItems().size());
                    listButton.setText(listButtonText);
                    listButton.invalidate();
                    final AnimationDrawable img = (AnimationDrawable)getResources().getDrawable( R.drawable.loader);
                    img.setBounds( 0, 0, listButton.getHeight()-8, listButton.getHeight()-8 );
                    if(DirectionsApplication.getInstance().getLocationsManager().isRunning()){
                        img.start();
                        updateButton.setText(getString(R.string.updating));
                        updateButton.setCompoundDrawables( img, null, null, null );

                    }else{
                        img.stop();
                        updateButton.setText(getString(R.string.update));
                        updateButton.setCompoundDrawables(null, null, null, null);
                    }

                    updateButton.invalidate();
                }
            };
            buttonsUpdater.update(null, null);
            DirectionsApplication.getInstance().getLocationsManager().addObserver(buttonsUpdater);

            return rootView;
        }

        public void openListLocations() {
            if (DirectionsApplication.getInstance().getModel().getData().getLocation() != null) {
                ListDialog listDialog = new ListDialog();
                FragmentManager fm = getFragmentManager();
                listDialog.show(fm, "list_dialog");
            } else {
                Toast.makeText(getActivity(), this.getString(R.string.defining), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class PlaceholderFragment extends Fragment {

        private static final long REPEATER_COMPASS_TIMER = 10;
        public static final int REPEATER_FIRST_TIME_OPEN_DIALOG = 1000;

        private CompassSurface surface;

        private SensorListener compass;
        private boolean questionMarkRendered;
        private TextView textOutput;

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

            surface = (CompassSurface) rootView.findViewById(R.id.surface);
            textOutput = (TextView) rootView.findViewById(R.id.textOutput);

            Observer textOutputUpdater = new Observer() {
                @Override
                public void update(Observable observable, Object data) {
                    Model model = DirectionsApplication.getInstance().getModel();
                    Data dataModel = model.getData();
                    if (dataModel.getLocation() != null) {
                        if (dataModel.getDestinationDistance() > 0) {
                            textOutput.setText(getString(R.string.distance, dataModel.getDestinationName(), dataModel.getDestinationDistance()));
                        } else {
                            textOutput.setText(getString(R.string.please_select));
                        }
                    } else {
                        textOutput.setText(getString(R.string.defining));
                    }
                }
            };

            DirectionsApplication.getInstance().getModel().addObserver(textOutputUpdater);
            compass = new SensorListener(getActivity());

            return rootView;
        }

        @Override
        public void onPause() {
            // unregister from the directions to prevent undue battery drain
            compass.unregisterSensors();
            // stop the animation
            surface.stopAnimation();
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
