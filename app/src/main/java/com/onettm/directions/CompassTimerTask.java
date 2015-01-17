package com.onettm.directions;

import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;
import java.util.TimerTask;

public class CompassTimerTask extends TimerTask {

    private static final int MIN_DISTANCE = 30;
    private final CompassActivity context;
    private final CompassActivity.PlaceholderFragment fragment;

    public CompassTimerTask(CompassActivity context, CompassActivity.PlaceholderFragment fragment) {
        super();
        this.context = context;
        this.fragment = fragment;
    }

    @Override
    public void run() {
        Model model = DirectionsApplication.getInstance().getModel();
        Data data = model.getData();
        checkNewItems(data);
        checkArrive(data);
    }

    private void checkNewItems(Data data) {
        //TODO
        /*if (!fragment.isQuestionMarkRendered() && data.getLocation() != null && data.getDecisionPoint() != null && data.getLocation().distanceTo(data.getDecisionPoint()) > 1000) {
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
        }*/
    }

    private void checkArrive(Data data) {
        //TODO
        /*Model model = DirectionsApplication.getInstance().getModel();

        if ((data.getLocation() != null) && (data.getDestinationDistance() > 0) && (data.getDestinationDistance() < MIN_DISTANCE)) {
            fragment.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, context.getString(R.string.arriving), Toast.LENGTH_SHORT).show();
                }
            });
            model.setDestinationName(null);
            model.setDestinationLocation(null);
        }*/
    }


}
