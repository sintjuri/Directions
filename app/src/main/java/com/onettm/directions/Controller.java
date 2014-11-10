package com.onettm.directions;

import android.hardware.SensorManager;
import android.location.Location;

import java.util.LinkedList;
import java.util.List;

public class Controller {

    public float calculateAzimut(Model model) {
        float result = 0f;
        /*Rotation matrix and Inclination matrix*/
        float R[] = new float[9];
        float I[] = new float[9];
        /* Compute the inclination matrix I as well as the rotation matrix R transforming a vector from the device
        coordinate system to the world's coordinate system
        R and I [Length 9]
        gravity vector expressed in the device's coordinate [Length 3]
        geoMagnetic vector expressed in the device's coordinate[Length 3]
        */
        boolean success = SensorManager.getRotationMatrix(R, I,
                model.getAveragedGravity(), model.getAveragedGeoMagnetic());

        if (success) {
         /* Orientation has azimuth, pitch and roll */
            float orientation[] = new float[3];
            SensorManager.getOrientation(R, orientation);
            result = (float) ((Math.toDegrees(orientation[0]) + 360) % 360);
        }
        return result;
    }

    public float calculateBearing(Model model) {
        float bearing = 0f;
        if ((model.getCurrentLocation() != null) && (model.getDestinationLocation() != null)) {
            bearing = model.getCurrentLocation().bearingTo(model.getDestinationLocation());
        }
        return bearing;
    }

    public float calculateDistance(Location location1, Location location2) {
        float distance = 0f;
        if ((location1 != null) && (location2 != null)) {
            distance = location1.distanceTo(location2);
        }
        return Math.round(distance);
    }

    public boolean isNewTargetLocationsAvailable(Model model) {
        boolean result = false;
        for(LocationItem item: getData(model)){
            if(!model.getDecisionPointLocationItems().contains(item)){
                result = true;
                break;
            }
        }

        return result;
    }

    public List<LocationItem> getData(Model model) {
        //TODO change to real implementation
        List<LocationItem> result = new LinkedList<LocationItem>();
        for (int i = 0; i < 10; i++) {
            Location cicusLocation = new Location("");
            double circusLatitude = 51.656608;
            cicusLocation.setLatitude(circusLatitude);
            double circusLongitude = 39.185975;
            cicusLocation.setLongitude(circusLongitude);
            LocationItem item = new LocationItem(cicusLocation, "Circus", model.getCurrentLocation());
            result.add(item);
        }

        Location mdm = new Location("");
        mdm.setLatitude(51.661678);
        mdm.setLongitude(39.203636);
        LocationItem item = new LocationItem(mdm, "MDM", model.getCurrentLocation());
        result.add(item);
        return result;
    }
}
