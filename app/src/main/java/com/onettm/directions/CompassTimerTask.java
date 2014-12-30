package com.onettm.directions;

import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;
import java.util.TimerTask;

public class CompassTimerTask extends TimerTask {

    private static final int MIN_DISTANCE = 30;
    private final CompassActivity context;
    private final CompassActivity.PlaceholderFragment fragment;
    private final Model model;

    public CompassTimerTask(CompassActivity context, CompassActivity.PlaceholderFragment fragment , Model model) {
        super();
        this.context = context;
        this.fragment = fragment;
        this.model = model;
    }

    @Override
    public void run() {
        Data data = model.getData();
        checkNewItems(data);
        checkArrive(data);
    }

    private void checkNewItems(Data data) {
        if (!fragment.isQuestionMarkRendered() && data.getLocation() != null && data.getDecisionPoint() != null && data.getLocation().distanceTo(data.getDecisionPoint()) > 1000) {
            for (LocationItem item : context.getDestinations()) {

                if (!Arrays.asList(data.getDecisionPointLocationItems()).contains(item)) {
                    fragment.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            Button button = fragment.getNotificationButton();
                            button.setText(button.getText() + " ?");
                            fragment.setQuestionMarkRendered(true);
                        }
                    });
                    break;
                }
            }
        }
    }

    private void checkArrive(Data data) {
        if ((data.getLocation() != null) && (data.getDestinationDistance() > 0) && (data.getDestinationDistance() < MIN_DISTANCE)) {
            fragment.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, context.getString(R.string.arriving), Toast.LENGTH_SHORT).show();
                }
            });
            model.setDestinationName(null);
            model.setDestinationLocation(null);
        }
    }


}