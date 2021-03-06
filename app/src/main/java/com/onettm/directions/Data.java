package com.onettm.directions;

import android.hardware.SensorManager;
import android.location.Location;



public class Data {

    private int status;
    private Location location;
    private Location destinationLocation;
    private String destinationName;
    private float[] magValues;
    private float[] accelValues;
    private float declination;
    //private GeomagneticField geoField;

    private float[] orientationDataCache;


    public int getStatus() {
        return status;
    }

    public Location getLocation() {
        return location;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public float getPositiveBearing() {
        // take the given bearing and convert it into 0 <= x < 360
        float bearing = getBearing(false);
        if(bearing < 0){
            bearing += 360;
        }
        return bearing;
    }

    private float getBearing(boolean trueNorth) {
        // update the values
        float[] orientationData = getOrientationData();

        // bail if the orientation data was null
        if(orientationData == null) {
            return 0f;
        }

        // convert the orientation data into a bearing
        float azimuth = orientationData[0];
        float bearing = toDegrees(azimuth); // convert from radians into degrees

        // check if we need to convert this into true
        if(trueNorth) {
            bearing = convertToTrueNorth(bearing);
        }

        return bearing;
    }

    private float[] getOrientationData() {
        if(magValues == null || accelValues == null){
            return orientationDataCache;
        }

        // compute the orientation data
        float[] R = new float[16];
        float[] I = new float[16];
        SensorManager.getRotationMatrix(R, I, accelValues, magValues);
        orientationDataCache = new float[3];
        SensorManager.getOrientation(R, orientationDataCache);

        // return the new data
        return orientationDataCache;
    }

    private float toDegrees(float arg) {
        return arg * (360 / (2 * (float) Math.PI));
    }

    private float convertToTrueNorth(float bearing){
        return bearing + declination;
    }

    public float getDestinationBearing() {

        float bearing = 0f;
        if ((location != null) && (destinationLocation != null)) {
            bearing = location.bearingTo(destinationLocation);
        }
        if(bearing < 0){
            bearing += 360;
        }
        return bearing;
    }

    public float getDestinationBearing(Location targetLocation) {

        float bearing = 0f;
        if ((location != null) && (targetLocation != null)) {
            bearing = location.bearingTo(targetLocation);
        }
        if(bearing < 0){
            bearing += 360;
        }
        return bearing;
    }

    public float getDestinationDistance() {
        float distance = 0f;
        if ((location != null) && (destinationLocation != null)) {
            distance = location.distanceTo(destinationLocation);
        }
        return Math.round(distance);
    }

    public void setDestinationLocation(Location destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public Location getDestinationLocation(){
        return destinationLocation;
    }

    public void setAccelValues(float[] accelValues) {
        this.accelValues = accelValues;
    }

    public void setMagValues(float[] magValues) {
        this.magValues = magValues;
    }

    public void setDeclination(float declination){
        this.declination = declination;
    }

    public float getDeclination() {
        return declination;
    }
}
