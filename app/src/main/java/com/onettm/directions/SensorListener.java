/*******************************************************************************
 * NiceCompass
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
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
	/** constants **/
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 500;
    private static final int MIN_ACCURACY = 50;

	
	/** variables **/
	private final LocationManager locationManager;

	private final SensorManager sensorManager;
	private final Sensor magSensor;
	private final Sensor accelSensor;
	private boolean sensorsRegistered; // stores the event listener state



	public void unregisterSensors() {
		if(sensorsRegistered){
			// unregister our sensor listeners
			locationManager.removeUpdates(this);
			sensorManager.unregisterListener(this, magSensor);
			sensorManager.unregisterListener(this, accelSensor);
            Model model = DirectionsApplication.getInstance().getModel();
            model.setStatus(Model.STATUS_INACTIVE);
			sensorsRegistered = false; // flag the sensors as unregistered
		}
	}

    public boolean isAccelerometerAvailable() {
        return accelSensor != null;
    }

    public boolean isMagnetometerAvailable() {
        return magSensor != null;
    }

    public boolean isProviderEnabled(String provider) {
        return locationManager.isProviderEnabled(provider);
    }

    public boolean isLocationEnoughAccurate(Location location){
        return location.getAccuracy()>0 && location.getAccuracy()<=MIN_ACCURACY;
    }

	public void registerSensors() {
		if(!sensorsRegistered) {
            // First get location from Network Provider
            if (isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Location location = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if ((location != null)&&(isLocationEnoughAccurate(location))) {
                    Model model = DirectionsApplication.getInstance().getModel();
                    model.updateLocation(location);
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Location location = locationManager
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if ((location != null)&&(isLocationEnoughAccurate(location))) {
                    Model model = DirectionsApplication.getInstance().getModel();
                    model.updateLocation(location);
                }

            }
			sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_UI);
			sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);
			sensorsRegistered = true; // flag the sensors as registered


		}

        /*if(destinationIsOutdated()){
            destinationName = null;
            decisionPointLocationItems = null;
            destinationLocation= null;
            decisionPoint= null;
            decisionTime= null;
        }*/
    }



    public void onSensorChanged(SensorEvent event) {
		// save the data from the sensor
        Model model = DirectionsApplication.getInstance().getModel();

        switch(event.sensor.getType()){
		case Sensor.TYPE_MAGNETIC_FIELD:
            float[] mgValues = event.values.clone();
			model.setMagValues(mgValues);

			break;
		case Sensor.TYPE_ACCELEROMETER:
			model.setAccelValues(event.values.clone());
			break;
		}
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

    @Override
    public void onLocationChanged(Location location) {
        // store the new location
        Model model = DirectionsApplication.getInstance().getModel();
        model.updateLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public SensorListener(Context context) {
		// initialize variables
        Model model = DirectionsApplication.getInstance().getModel();
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorsRegistered = false;
		model.setStatus(Model.STATUS_INACTIVE);
	}
}
