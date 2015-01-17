package com.onettm.directions.event;

import android.content.Context;

import com.onettm.directions.Data;

import java.util.EventObject;

public class DataEvent extends EventObject {

    private final Data data;
    private final Context context;

    public DataEvent(Context context,Data source) {
        super(source);
        this.data = source;
        this.context = context;
    }

    public Data getData() {
        return data;
    }

    public Context getContext() {
        return context;
    }
}
