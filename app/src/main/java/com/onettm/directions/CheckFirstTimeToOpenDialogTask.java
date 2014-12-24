package com.onettm.directions;

import java.util.TimerTask;

public class CheckFirstTimeToOpenDialogTask extends TimerTask {

    private final CompassActivity.PlaceholderFragment context;
    private final Model model;

    public CheckFirstTimeToOpenDialogTask(CompassActivity.PlaceholderFragment context, Model model) {
        super();
        this.context = context;
        this.model = model;
    }

    @Override
    public void run() {
        Data data = model.getData();
        if ((data.getLocation() != null) && (data.getDestinationDistance() == 0)) {
            context.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    context.openListLocations();
                }
            });
            cancel();
        }
    }
}
