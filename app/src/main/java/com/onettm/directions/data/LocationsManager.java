package com.onettm.directions.data;

import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Process;
import android.util.SparseArray;

import com.onettm.directions.DirectionsApplication;
import com.onettm.directions.LocationItem;
import com.onettm.directions.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.CancellationException;

/**
 * Created by agrigory on 1/16/15.
 * Search objects
 */
public class LocationsManager extends Observable implements Observer {

    private final com.onettm.directions.DirectionsApplication.Settings settings = DirectionsApplication.getInstance().getSettings();

    private final static String NONAME = "Без названия";
    private Collection<LocationItem> locations = Collections.unmodifiableCollection(Collections.<LocationItem>emptyList());
    private volatile boolean valid = true;
    private Location lastLocation;
    private Location decisionLocation;
    private volatile AsyncTask searchTask = null;

    public LocationsManager(Model model) {
        model.addObserver(this);
    }


    public boolean isRunning() {
        AsyncTask t = searchTask;
        if (t == null) return false;
        if (!t.isCancelled())
            if (t.getStatus() != AsyncTask.Status.FINISHED) return true;
        return false;
    }

    public void cancel() {
        AsyncTask t = searchTask;
        if (t == null) return;
        if (t.isCancelled()) return;
        if (t.getStatus() != AsyncTask.Status.FINISHED) searchTask.cancel(true);
    }

    private void setValid() {
        this.valid = true;
    }

    private void setInvalid() {
        this.valid = false;
    }

    public synchronized void invalidate() {
        setInvalid();
        update(lastLocation);
    }

    public synchronized void update(Location curLoc) {
        if (curLoc == null) return;
        if (!valid)
            if (isRunning()) return;
        setInvalid();

        AsyncTask<Location, Void, Void> at = new AsyncTask<Location, Void, Void>() {

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                setChanged();
                notifyObservers();
            }

            @Override
            protected Void doInBackground(Location[] params) {
                int priority = android.os.Process.getThreadPriority(Process.myTid());
                try {
                    Process.setThreadPriority(priority > 1 ? priority - 1 : 1);
                    publishProgress();
                    Set<LocationItem> result = request(params[0]);
                    if (!locations.containsAll(result)) {
                        List<LocationItem> res = new ArrayList<>(result);
                        Comparator<LocationItem> dc = new DistanceComparator<>(params[0]);
                        Collections.sort(res, dc);

                        locations = Collections.unmodifiableCollection(res);
                        decisionLocation = params[0];
                        setChanged();
                    }
                } finally {
                    Process.setThreadPriority(priority);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void o) {
                super.onPostExecute(o);
                setValid();
                setChanged();
                notifyObservers();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                setChanged();
                notifyObservers();
            }

            private Set<LocationItem> request(Location location) {

                Comparator<LocationItem> dc = new DistanceComparator<>(location);

                long longitudeSide = Math.round(getLongitudeDegreesByDistance(location, DirectionsApplication.getInstance().getSettings().getSearchRadius()) * 10000000);
                long latitudeSide = Math.round(getLatitudeDegreesByDistance(location, DirectionsApplication.getInstance().getSettings().getSearchRadius()) * 10000000);

                long cur_lat = (long) (location.getLatitude() * 10000000);
                long cur_lon = (long) (location.getLongitude() * 10000000);
                Cursor c = DirectionsApplication.getInstance().getDb().rawQuery(String.format(
                        "select node_tags.v, nodes.lat, nodes.lon from nodes \n" +
                        "left join node_tags on nodes.id=node_tags.node_id and node_tags.k='name' \n" +
                        "where nodes.lat > %d and nodes.lat < %d \n" +
                        "and nodes.lon > %d and nodes.lon < %d ", cur_lat - latitudeSide, cur_lat + latitudeSide, cur_lon - longitudeSide, cur_lon + longitudeSide), null);

                c.moveToFirst();
                Set<LocationItem> result = new HashSet<>();
                while (c.moveToNext()) {
                    if (isCancelled()) throw new CancellationException();
                    String name = c.getString(0);
                    if (name==null){
                        name = NONAME;
                    }
                    double lat = c.getDouble(1) / 10000000;
                    double lon = c.getDouble(2) / 10000000;
                    Location pv = new Location("");
                    pv.setLatitude(lat);
                    pv.setLongitude(lon);
                    result.add(new LocationItem(pv, name));
                }
                c.close();

                Cursor c2 = DirectionsApplication.getInstance().getDb().rawQuery(String.format(
                        "select way_tags.v, nodes.lat, nodes.lon, ways.id " +
                        "from ways left join way_tags on ways.id = way_tags.way_id and way_tags.k='name' " +
                        "inner join way_nodes on ways.id = way_nodes.way_id " +
                        "inner join nodes on way_nodes.node_id = nodes.id where "+
                        "nodes.lat > %d and nodes.lat < %d \n" +
                        "and nodes.lon > %d and nodes.lon < %d ", cur_lat - latitudeSide, cur_lat + latitudeSide, cur_lon - longitudeSide, cur_lon + longitudeSide), null);

                c2.moveToFirst();
                SparseArray<LocationItem> wayNodes = new SparseArray<>();

                while (c2.moveToNext()) {
                    if (isCancelled()) throw new CancellationException();
                    String name = c2.getString(0);
                    if (name==null){
                        name = NONAME;
                    }
                    double lat = c2.getDouble(1) / 10000000;
                    double lon = c2.getDouble(2) / 10000000;
                    Integer way = c2.getInt(3);
                    Location pv = new Location("");
                    pv.setLatitude(lat);
                    pv.setLongitude(lon);
                    LocationItem newItem = new LocationItem(pv, name);
                    LocationItem existingItem = wayNodes.get(way);
                    if (existingItem != null) {
                        if (dc.compare(existingItem, newItem) < 0) continue;
                    }
                    wayNodes.put(c2.getInt(3), newItem);
                }
                c2.close();

                int key;
                for (int i = 0; i < wayNodes.size(); i++) {
                    if (isCancelled()) throw new CancellationException();
                    key = wayNodes.keyAt(i);
                    LocationItem obj = wayNodes.get(key);
                    result.add(obj);
                }

                return result;
            }
        };
        searchTask = at.execute(curLoc);
    }

    public Collection<LocationItem> getLocationItems() {
        return locations;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isInitialized() {
        return lastLocation != null;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (!(data instanceof Location)) throw new AssertionError("Location object expected");
        lastLocation = (Location) data;
        if (isRunning()) return;
        if (isValid())
            if (decisionLocation != null)
                if (lastLocation.distanceTo(decisionLocation) < settings.getDecisionExpirationDistance())
                    return;
        update(lastLocation);
    }

    private float getLatitudeDegreesByDistance(Location location, int meters) {
        double lo = Math.floor(location.getLatitude());
        double hi = Math.ceil(location.getLatitude());
        Location loLoc = new Location("dummy");
        loLoc.setLatitude(lo);
        Location hiLoc = new Location("dummy");
        hiLoc.setLatitude(hi);
        float d = loLoc.distanceTo(hiLoc);
        return meters * (1 / d);
    }

    private float getLongitudeDegreesByDistance(Location location, int meters) {
        double lo = Math.floor(location.getLongitude());
        double hi = Math.ceil(location.getLongitude());
        Location loLoc = new Location("dummy");
        loLoc.setLongitude(lo);
        Location hiLoc = new Location("dummy");
        hiLoc.setLongitude(hi);
        float d = loLoc.distanceTo(hiLoc);
        return meters * (1 / d);
    }

    public void setLocations(List<LocationItem> locations) {
        this.locations = locations;
    }

    public void setDecisionLocation(Location decisionLocation) {
        this.decisionLocation = decisionLocation;
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
