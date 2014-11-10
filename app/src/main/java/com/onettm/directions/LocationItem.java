package com.onettm.directions;

import android.location.Location;

public class LocationItem {
    private Location currentLocation;//location of current position of user to define bearing
    private Location location;
    private String name;

    public LocationItem(Location location, String name, Location currentLocation) {
        this.location = location;
        this.name = name;
        this.currentLocation = currentLocation;
    }

    @Override
    public String toString() {
        String result = name;
        if ((currentLocation != null) && (location != null)) {
            result += " " + currentLocation.distanceTo(location);
        }
        return result;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LocationItem other = (LocationItem) obj;
        if (currentLocation == null) {
            if (other.getCurrentLocation() != null)
                return false;
        } else if (currentLocation.distanceTo(other.getCurrentLocation())!=0)
            return false;
        if (location == null) {
            if (other.getLocation() != null)
                return false;
        } else if (location.distanceTo(other.getLocation())!=0)//!location.equals(other.getLocation())
            return false;
        if (name == null) {
            if (other.getName() != null)
                return false;
        } else if (!name.equals(other.getName()))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((currentLocation == null) ? 0 : currentLocation.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
}
