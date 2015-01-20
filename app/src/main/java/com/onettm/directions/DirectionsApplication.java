package com.onettm.directions;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.onettm.directions.data.DirDataHelper;
import com.onettm.directions.data.LocationsManager;

/**
 * Created by agrigory on 12/29/14.
 */
public class DirectionsApplication extends Application {
    private static DirectionsApplication inst;

    private DirDataHelper dbDataHelper;

    private final Settings settings = new Settings();

    private final Model model = new Model();
    private LocationsManager locationsManager;

    public static DirectionsApplication getInstance() {
        return inst;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        inst = this;
        locationsManager = new LocationsManager(model);
        dbDataHelper = new DirDataHelper();
    }

    public SQLiteDatabase getDb() {
        return dbDataHelper.getDb();
    }


    public LocationsManager getLocationsManager() {
        return locationsManager;
    }

    public Model getModel() {
        return model;
    }

    public Settings getSettings() {
        return settings;
    }

    public static class Settings {
        private final int decisionExpirationDistance = 1000; //meters
        private final int searchRadius = 5000; //meters
        private final int minDistanceToTarget = 30; //meters

        public int getDecisionExpirationDistance() {
            return decisionExpirationDistance;
        }

        public int getSearchRadius() {
            return searchRadius;
        }

        public int getMinDistanceToTarget() {
            return minDistanceToTarget;
        }
    }
}

