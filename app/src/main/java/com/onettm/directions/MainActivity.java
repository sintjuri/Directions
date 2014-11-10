package com.onettm.directions;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends FragmentActivity implements ListDialog.Callbacks {
    private static final double ROTATION_SPEED_KOEF = 0.36;
    private static final float NEW_DECISION_DISTANCE = 1000;
    // define the display assembly compass picture
    private ImageView compass;
    private ImageView pointer;
    private TextView tvHeading;
    private Button notificationButton;

    private SensorListener sensorListener;
    private Model model;
    private Controller controller;

    private float magneticCurrentDegree;
    private float pointerCurrentDegree;
    private boolean notificationIsActive = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compass = (ImageView) findViewById(R.id.imageViewCompass);
        pointer = (ImageView) findViewById(R.id.imageViewPointer);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);
        tvHeading.setText(getString(R.string.defining));
        ImageButton listButton = (ImageButton) findViewById(R.id.listButton);

        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openListLocations();
            }
        });

        notificationButton = (Button) findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openListLocations();
            }
        });

        model = new Model();
        controller = new Controller();
        sensorListener = new SensorListener(this, model);
        if (!sensorListener.isAccelerometerAvailable()) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.accelerometer_not_available)
                    .show();
        }

        if (!sensorListener.isMagnetometerAvailable()) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.magnetometer_not_available)
                    .show();
        }

        if (!sensorListener
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showSettingsAlert();
        }
    }

    private void openListLocations() {
        if(model.getCurrentLocation()!=null) {
            android.app.FragmentManager fm = getFragmentManager();
            ListDialog listDialog = new ListDialog();
            listDialog.show(fm, "list_dialog");
        }else{
            Toast.makeText(this, getString(R.string.defining), Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        sensorListener.registerListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        sensorListener.unregisterListener();
    }

    private RotateAnimation createRotateAnimation(float fromDegree, float toDegree) {

        fromDegree = (fromDegree % 360) + 360;
        toDegree = (toDegree % 360) + 360;

        if (Math.abs(toDegree - fromDegree) > 180) {
            toDegree = 360 - toDegree;
        }

        RotateAnimation raImage = new RotateAnimation(
                fromDegree,
                toDegree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        raImage.setDuration(Math.round(Math.abs(toDegree - fromDegree) * ROTATION_SPEED_KOEF));
        raImage.setFillAfter(true);
        return raImage;
    }

    private void doRender() {
        float azimut = controller.calculateAzimut(model);
        RotateAnimation raImage = createRotateAnimation(
                magneticCurrentDegree,
                -azimut);

        this.compass.startAnimation(raImage);
        magneticCurrentDegree = -azimut;

        if((model.getCurrentLocation() != null) && (model.getDestinationName() != null)){
            float bearing = controller.calculateBearing(model);
            RotateAnimation raPointer = createRotateAnimation(
                    pointerCurrentDegree,
                    (bearing - azimut));

            this.pointer.startAnimation(raPointer);
            pointerCurrentDegree = bearing - azimut;

            if(pointer.getVisibility() != View.VISIBLE){
                pointer.setVisibility(View.VISIBLE);
            }

            tvHeading.setText(getString(R.string.distance, model.getDestinationName(), controller.calculateDistance(model.getCurrentLocation(), model.getDestinationLocation())));
        }



        if((controller.calculateDistance(model.getCurrentLocation(), model.getDecisionPoint())>NEW_DECISION_DISTANCE) && (!notificationIsActive)){
            if(isNewTargetLocationsAvailable()){
                notificationIsActive = true;
                notificationButton.setVisibility(View.VISIBLE);
            }
        }

    }


    public void currentLocationUpdated() {
        doRender();
    }

    public void phonePositionUpdated() {
        doRender();
    }

    @Override
    public List<LocationItem> getData() {
        return controller.getData(model);
    }

    @Override
    public void onItemSelected(LocationItem locationItem) {

        model.setDecisionPoint(locationItem.getCurrentLocation());
        model.setDecisionPointLocationItems(getData());
        model.setDestinationLocation(locationItem.getLocation());
        model.setDestinationName(locationItem.getName());

    }

    private boolean isNewTargetLocationsAvailable() {
        return controller.isNewTargetLocationsAvailable(model);
    }
}