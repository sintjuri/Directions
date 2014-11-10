package com.onettm.directions;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


public class SensorListener implements SensorEventListener, LocationListener {

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters
    private static final int MIN_ACCURACY = 50;

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 500;
    private Context context;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    // Declaring a Location Manager
    protected LocationManager locationManager;


    private Model model;

    public SensorListener(Context context, Model model) {
        this.model = model;
        this.context = context;
        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean isAccelerometerAvailable() {
        return accelerometer != null;
    }

    public boolean isMagnetometerAvailable() {
        return magnetometer != null;
    }

    public void registerListener() {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        // First get location from Network Provider
        if (isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            if (locationManager != null) {
                Location location = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if ((location != null)&&(isLocationEnoughAccurate(location))) {
                    model.setCurrentLocation(location);
                    ((MainActivity) context).currentLocationUpdated();
                }
            }
        }
        // if GPS Enabled get lat/long using GPS Services
        if (isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            if (locationManager != null) {
                Location location = locationManager
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if ((location != null)&&(isLocationEnoughAccurate(location))) {
                    model.setCurrentLocation(location);
                    ((MainActivity) context).currentLocationUpdated();
                }
            }

        }
    }

    public void unregisterListener() {
        mSensorManager.unregisterListener(this);
        stopUsingGPS();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /*get gravity value arrays from Accelerometer*/
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] gravity = new float[3];
            System.arraycopy(event.values, 0, gravity, 0, event.values.length);
            model.getAccelerometrBuffer().add(gravity);
        }
        /*get gravity value arrays from Magnet*/
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float[] geoMagnetic = new float[3];
            System.arraycopy(event.values, 0, geoMagnetic, 0, event.values.length);
            model.getMagneticBuffer().add(geoMagnetic);
        }
        //TODO refactor notification of activity
        ((MainActivity) context).phonePositionUpdated();
    }


    public boolean isProviderEnabled(String provider) {
        return locationManager.isProviderEnabled(provider);
    }

    private void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if ((location != null)&&(isLocationEnoughAccurate(location))) {
            model.setCurrentLocation(location);
            //TODO refactor notification of activity
            ((MainActivity) context).currentLocationUpdated();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    public boolean isLocationEnoughAccurate(Location location){
        return location.getAccuracy()>0 && location.getAccuracy()<=MIN_ACCURACY;
    }

}
