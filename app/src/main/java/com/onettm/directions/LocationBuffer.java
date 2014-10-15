package com.onettm.directions;

import android.location.Location;

/**
 * Created by sintyaev on 14.10.14.
 */
public class LocationBuffer extends Buffer<Location>{

    public LocationBuffer(int size) {
        super(size);
    }

    @Override
    public Location getAverageValue() {
        if(data.size()>0) {

            double latSum = 0;
            double lonSum = 0;

            for (Location location : data) {
                latSum += location.getLatitude();
                lonSum += location.getLongitude();
            }

            Location result = new Location("");
            result.setLatitude((latSum/data.size()));
            result.setLongitude((lonSum/data.size()));

            return result;
        }else{
            return null;
        }

    }
}
