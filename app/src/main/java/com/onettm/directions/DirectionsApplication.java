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

    private final Model model = new Model();
    private final LocationsManager locationsManager = new LocationsManager(model);

    public static DirectionsApplication getInstance() {
        return inst;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        inst = this;
        dbDataHelper = new DirDataHelper();
    }

    public SQLiteDatabase getDb(){
        return dbDataHelper.getDb();
    }


    public LocationsManager getLocationsManager() {
        return locationsManager;
    }

    public Model getModel() {
        return model;
    }
}

