package com.onettm.directions;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class Model {
    public static final int BUFFER_SIZE = 50;

    private SensorBuffer magneticBuffer;
    private SensorBuffer accelerometrBuffer;

    private List<LocationItem> decisionPointLocationItems;
    private Location decisionPoint;

    private Location currentLocation;

    private Location destinationLocation;
    private String destinationName;

    public Model() {
        magneticBuffer = new SensorBuffer(BUFFER_SIZE);
        accelerometrBuffer = new SensorBuffer(BUFFER_SIZE);
        decisionPointLocationItems = new ArrayList<LocationItem>();
    }

    public SensorBuffer getMagneticBuffer() {
        return magneticBuffer;
    }

    public SensorBuffer getAccelerometrBuffer() {
        return accelerometrBuffer;
    }

    public float[] getAveragedGravity() {
        return accelerometrBuffer.getAveragedValue();
    }

    public float[] getAveragedGeoMagnetic() {
        return magneticBuffer.getAveragedValue();
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Location getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(Location destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public List<LocationItem> getDecisionPointLocationItems() {
        return decisionPointLocationItems;
    }

    public void setDecisionPointLocationItems(List<LocationItem> decisionPointLocationItems) {
        this.decisionPointLocationItems = decisionPointLocationItems;
    }

    public Location getDecisionPoint() {
        return decisionPoint;
    }

    public void setDecisionPoint(Location decisionPoint) {
        this.decisionPoint = decisionPoint;
    }
}

