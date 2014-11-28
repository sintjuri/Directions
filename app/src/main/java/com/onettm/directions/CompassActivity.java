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
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;


public class CompassActivity extends FragmentActivity implements ListDialog.Callbacks{

    public static final int REPRATER_FIRST_TIME_OPEN_DIALOG = 1000;
    private SensorListener compass;
    private Model model;
    private CompassSurface surface;
    private LinearLayout surfaceContainer;

    //private CompassSurface.CompassThread compassThread;


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
        // class the superclass
        super.onResume();

        surface = new CompassSurface(this, model);
        // add the directions
        surfaceContainer.removeAllViews();
        surfaceContainer.addView(surface);

        // register to receive events from the directions
        compass.registerSensors();

        if (!compass.isAccelerometerAvailable()) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.accelerometer_not_available)
                    .show();
        }

        if (!compass.isMagnetometerAvailable()) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.magnetometer_not_available)
                    .show();
        }

        if (!compass
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showSettingsAlert();
        }
    }

    /**
     * Function to show settings alert dialog
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create the gui
        setContentView(R.layout.main);

        model = new Model();
        // initialize variables
        compass = new SensorListener(this, model);

        //compassThread = surface.getThread();
        surfaceContainer = (LinearLayout) findViewById(R.id.compassSurfaceContainer);

        Button notificationButton = (Button) findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openListLocations();
            }
        });

        Timer firstTimeOnenDialogTimer = new Timer();
        firstTimeOnenDialogTimer.schedule(new CheckFirtTimeToOpenDialogTask(this, model), REPRATER_FIRST_TIME_OPEN_DIALOG);

    }

    @Override
    public LocationItem[] getData() {
        //TODO change to real implementation
        Location currentLocation = model.getData().getLocation();

        List<LocationItem> result = new LinkedList<LocationItem>();
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
        model.setDecisionPointLocationItems(getData());
        model.setDecisionTime(new Date());
        model.setDestinationLocation(locationItem.getLocation());
        model.setDestinationName(locationItem.getName());


    }

    public void openListLocations() {
        if (model.getData().getLocation() != null) {
            android.app.FragmentManager fm = this.getFragmentManager();
            ListDialog listDialog = new ListDialog();
            listDialog.show(fm, "list_dialog");
        } else {
            Toast.makeText(this, this.getString(R.string.defining), Toast.LENGTH_SHORT).show();
        }
    }

}
