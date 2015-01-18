package com.onettm.directions;

import android.hardware.GeomagneticField;
import android.location.Location;

import java.util.Observable;


public class Model extends Observable{

    public static final int STATUS_GOOD = 0;
    public static final int STATUS_INTERFERENCE = 1;
    public static final int STATUS_INACTIVE = 2;
    private static final float MAGNETIC_INTERFERENCE_THRESHOLD_MODIFIER = 1.05f;

    private float[] magValues;
    private float[] accelValues;

    //private boolean sensorHasNewData;
    private int status;
    private GeomagneticField geoField;
    private Location currentLocation;

    private String destinationName;
    private LocationItem[] decisionPointLocationItems;
    private Location destinationLocation;
    private Location decisionPoint;

    public synchronized Data getData(){

        Data result = new Data();
        result.setDestinationName(destinationName);
        result.setStatus(status);
        if (currentLocation!=null) {
            result.setLocation(new Location(currentLocation));
        }
        if(destinationLocation!=null) {
            result.setDestinationLocation(new Location(destinationLocation));
        }
        if(decisionPoint!=null) {
            result.setDecisionPoint(new Location(decisionPoint));
        }
        if(decisionPointLocationItems!=null) {
            result.setDecisionPointLocationItems(decisionPointLocationItems.clone());
        }
        if(magValues!=null) {
            result.setMagValues(magValues.clone());
        }
        if(accelValues!=null) {
            result.setAccelValues(accelValues.clone());
        }
        if(geoField!=null) {
            result.setDeclination(geoField.getDeclination());
        }
        return result;
    }

    public synchronized void setStatus(int status){
        this.status = status;
    }


    public synchronized void setMagValues(float[] values) {
        magValues = values;
        // check for interference
        interferenceTest(magValues);
    }

    public synchronized void setAccelValues(float[] accelValues) {
        this.accelValues = accelValues;
    }

    public synchronized void updateLocation(Location locationCache) {
        this.currentLocation = locationCache;
        updateGeoField(locationCache);
        setChanged();
        if (locationCache != null) notifyObservers(locationCache);
    }

    public synchronized void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public synchronized void setDecisionPointLocationItems(LocationItem[] decisionPointLocationItems) {
        this.decisionPointLocationItems = decisionPointLocationItems;
    }

    public synchronized void setDestinationLocation(Location destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public void setDecisionPoint(Location decisionPoint) {
        this.decisionPoint = decisionPoint;
    }

    private void updateGeoField(Location location) {
        // we can do nothing without location
        if(location != null) {
            // update the geomagnetic field
            geoField = new GeomagneticField(
                    Double.valueOf(location.getLatitude()).floatValue(),
                    Double.valueOf(location.getLongitude()).floatValue(),
                    Double.valueOf(location.getAltitude()).floatValue(),
                    System.currentTimeMillis());
        }
    }



    private void interferenceTest(float[] values) {
        // get the expected values
        float threshold = getExpectedFieldStrength() * MAGNETIC_INTERFERENCE_THRESHOLD_MODIFIER;
        float totalStrength = 1f;
        // loop through the values and test that they are not more than X% above the expected values
        for (float value : values) {
            totalStrength *= value;
        }
        if(totalStrength > threshold){
            // report possible interference
            status = STATUS_INTERFERENCE;
        } else {
            status =  STATUS_GOOD;
        }
    }

    private float getExpectedFieldStrength(){
        // a geo field is required for accurate data
        if(geoField != null){
            return geoField.getFieldStrength();
        } else {
            // provide a field strength over average
            return 60*60*60f;
        }
    }


    /*private boolean destinationIsOutdated() {
        if (decisionTime!=null){
            Calendar cal = Calendar.getInstance();
            cal.setTime(decisionTime);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date date = cal.getTime();
            if (date.before(new Date())){
                return true;
            }
        }
        return false;
    }*/

}
