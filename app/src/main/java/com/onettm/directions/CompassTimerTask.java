package com.onettm.directions;

import android.content.Context;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;
import java.util.TimerTask;

public class CompassTimerTask extends TimerTask {

    private static final int MIN_DISTANCE = 30;
    private final Context context;
    private final Model model;

    public CompassTimerTask(Context context, Model model) {
        super();
        this.context = context;
        this.model = model;
    }

    @Override
    public void run() {
        Data data = model.getData();
        checkNewItems(data);
        checkArrive(data);
    }

    private void checkNewItems(Data data) {
        if (!((CompassActivity) context).isQuestionMarkRendered() && data.getLocation() != null && data.getDecisionPoint() != null && data.getLocation().distanceTo(data.getDecisionPoint()) > 1000) {
            for (LocationItem item : ((CompassActivity) context).getData()) {

                if (!Arrays.asList(data.getDecisionPointLocationItems()).contains(item)) {
                    ((CompassActivity) context).getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            Button button = (Button) ((CompassActivity) context).findViewById(R.id.notificationButton);
                            button.setText(button.getText() + " ?");
                            ((CompassActivity) context).setQuestionMarkRendered(true);
                        }
                    });
                    break;
                }
            }
        }
    }

    private void checkArrive(Data data) {
        if ((data.getLocation() != null) && (data.getDestinationDistance() > 0) && (data.getDestinationDistance() < MIN_DISTANCE)) {
            ((CompassActivity) context).getHandler().post(new Runnable() {
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
