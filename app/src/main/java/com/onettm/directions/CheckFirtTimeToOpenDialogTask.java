package com.onettm.directions;

import android.content.Context;

import java.util.TimerTask;

public class CheckFirtTimeToOpenDialogTask extends TimerTask {

    private final Context context;
    private final Model model;

    public CheckFirtTimeToOpenDialogTask(Context context, Model model) {
        super();
        this.context = context;
        this.model = model;
    }

    @Override
    public void run() {
        Data data = model.getData();
        if ((data.getLocation() != null) && (data.getDestinationDistance() == 0)) {
            ((CompassActivity) context).getHandler().post(new Runnable() {
                @Override
                public void run() {
                    ((CompassActivity) context).openListLocations();
                }
            });
            cancel();
        }
    }
}
