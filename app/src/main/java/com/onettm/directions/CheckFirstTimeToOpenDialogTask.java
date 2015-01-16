package com.onettm.directions;

import java.util.TimerTask;

public class CheckFirstTimeToOpenDialogTask extends TimerTask {

    private final CompassActivity.PlaceholderFragment context;

    public CheckFirstTimeToOpenDialogTask(CompassActivity.PlaceholderFragment context) {
        super();
        this.context = context;
    }

    @Override
    public void run() {
        Data data = DirectionsApplication.getInstance().getModel().getData();
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
