package com.onettm.directions.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.onettm.directions.DirectionsApplication;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by agrigory on 12/29/14.
 */
public class DirDataHelper {

    private static final int DATABASE_VERSION = 2;
    private SQLiteDatabase db = null;

    public DirDataHelper() {
        SQLiteOpenHelper o = new DirOpenHelper(DirectionsApplication.getInstance(), "osm.db");
        this.db = o.getReadableDatabase();

    }

    private class DirOpenHelper extends SQLiteAssetHelper {

        DirOpenHelper(Context context,String DatabaseName) {
            super(context, DatabaseName, null, DATABASE_VERSION);
            this.setForcedUpgrade();
        }
    }

    public SQLiteDatabase getDb() {
        return db;
    }
}
