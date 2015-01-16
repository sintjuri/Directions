package com.onettm.directions.data;

import android.database.Cursor;
import android.location.Location;

import com.onettm.directions.DirectionsApplication;
import com.onettm.directions.LocationItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by agrigory on 1/16/15.
 */
public class LocationsManager {
    public void invalidate() {
    }

    public Collection<LocationItem> getLocationItems(){
        return new ArrayList<LocationItem>();
    }

    private void request(){
        //TODO change to real implementation
        System.err.println("TIME getDestinations 1 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        Location currentLocation = DirectionsApplication.getInstance().getModel().getData().getLocation();
        System.err.println("TIME getDestinations 2 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        DistanceComparator<LocationItem> dc = new DistanceComparator<LocationItem>(currentLocation);

        System.err.println("TIME getDestinations 3 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        long cur_lat = (long) (currentLocation.getLatitude() * 10000000);
        long cur_lon = (long) (currentLocation.getLongitude() * 10000000);
        System.err.println("TIME getDestinations 4 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        Cursor c = DirectionsApplication.getInstance().getDb().rawQuery(String.format("select tag.v, node.lat, node.lon from node \n" +
                "join tag_node on node.id=tag_node.node\n" +
                "join tag on tag.id=tag_node.tag\n" +
                "where tag.k='name'\n" +
                "and node.lat > %d - 400000 and node.lat < %d + 400000\n" +
                "and node.lon > %d - 700000 and node.lon < %d + 700000", cur_lat, cur_lat, cur_lon, cur_lon), null);

        System.err.println("TIME getDestinations 5 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        c.moveToFirst();
        System.err.println("TIME getDestinations 6 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        Set<LocationItem> result = new HashSet<LocationItem>();
        System.err.println("TIME getDestinations 7 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        while (c.moveToNext()) {
            String name = c.getString(0);
            double lat = c.getDouble(1) / 10000000;
            double lon = c.getDouble(2) / 10000000;
            Location pv = new Location("");
            pv.setLatitude(lat);
            pv.setLongitude(lon);
            result.add(new LocationItem(pv, name, currentLocation));
        }
        System.err.println("TIME getDestinations 8 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        c.close();
        System.err.println("TIME getDestinations 9 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));

        Cursor c2 = DirectionsApplication.getInstance().getDb().rawQuery(String.format("select tag.v, node.lat, node.lon, way.id from node \n" +
                "join way on way.id = node.way\n" +
                "join tag_way on way.id = tag_way.way\n" +
                "join tag on tag.id=tag_way.tag\n" +
                "where tag.k='name'\n" +
                "and node.lat > %d - 400000 and node.lat < %d + 400000\n" +
                "and node.lon > %d - 700000 and node.lon < %d + 700000", cur_lat, cur_lat, cur_lon, cur_lon), null);

        System.err.println("TIME getDestinations 10 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        c2.moveToFirst();
        System.err.println("TIME getDestinations 11 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        Map<Integer, LocationItem> wayNodes = new HashMap<Integer, LocationItem>();
        System.err.println("TIME getDestinations 12 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));

        while (c2.moveToNext()) {
            String name = c2.getString(0);
            double lat = c2.getDouble(1) / 10000000;
            double lon = c2.getDouble(2) / 10000000;
            Integer way = c2.getInt(3);
            Location pv = new Location("");
            pv.setLatitude(lat);
            pv.setLongitude(lon);
            LocationItem newItem = new LocationItem(pv, name, currentLocation);
            LocationItem existingItem = wayNodes.get(way);
            if (existingItem != null) {
                if (dc.compare(existingItem, newItem) < 0) continue;
            } else wayNodes.put(c2.getInt(3), newItem);
        }
        System.err.println("TIME getDestinations 13 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
        c2.close();
        System.err.println("TIME getDestinations 14 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));

        result.addAll(wayNodes.values());

        ArrayList<LocationItem> res= new ArrayList<LocationItem>(result);
        Collections.sort(res, dc);

        System.err.println("TIME getDestinations 16 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
    }


    static class DistanceComparator<L extends LocationItem> implements java.util.Comparator<L> {

        private final Location currentLoc;

        DistanceComparator(Location currentLoc) {
            this.currentLoc = currentLoc;
        }

        @Override
        public int compare(L l, L l2) {
            if (l == l2) return 0;
            if (l == null) return -1;
            if (l2 == null) return 1;

            if (l.getLocation() == null) return -1;
            if (l2.getLocation() == null) return 1;

            float d = currentLoc.distanceTo(l.getLocation());
            float d2 = currentLoc.distanceTo(l2.getLocation());
            return Float.compare(d, d2);
        }
    }
}
