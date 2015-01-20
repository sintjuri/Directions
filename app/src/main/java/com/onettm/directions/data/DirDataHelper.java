package com.onettm.directions.data;

import android.database.sqlite.SQLiteDatabase;

import com.onettm.directions.DirectionsApplication;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by agrigory on 12/29/14.
 * Singleton for database
 */
public class DirDataHelper {

    private static final int DATABASE_VERSION = 2;

    public static SQLiteDatabase getDb() {
        return DbHolder.db;
    }

    private static class DbHolder{
        private static final SQLiteDatabase db;

        static{
            SQLiteAssetHelper sah = new SQLiteAssetHelper(DirectionsApplication.getInstance(), "osm.db", null, DATABASE_VERSION);
            sah.setForcedUpgrade();
            db = sah.getReadableDatabase();
        }
    }
}
