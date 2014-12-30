package com.onettm.directions;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.onettm.directions.data.DirDataHelper;

/**
 * Created by agrigory on 12/29/14.
 */
public class DirectionsApplication extends Application {
    private static DirectionsApplication inst;

    private DirDataHelper dbDataHelper;

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
}

