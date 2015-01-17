package com.onettm.directions.event;


import android.widget.TextView;

import com.onettm.directions.CompassActivity;
import com.onettm.directions.Data;
import com.onettm.directions.R;

public class TextOutputDataEventListener implements DataEventListener{

    private final TextView textOutput;

    public TextOutputDataEventListener(TextView textOutput) {
        super();
        this.textOutput = textOutput;
    }

    @Override
    public void onEvent(final DataEvent dataEvent) {
        ((CompassActivity)dataEvent.getContext()).getHandler().post(new Runnable() {
            @Override
            public void run() {
                String textToOutput = "";
                Data data = dataEvent.getData();
                if(data!=null){
                    if (data.getLocation() != null) {
                        if (data.getDestinationDistance() > 0) {
                            textToOutput = dataEvent.getContext().getString(R.string.distance, data.getDestinationName(), data.getDestinationDistance());
                        } else {
                            textToOutput = dataEvent.getContext().getString(R.string.please_select);
                        }
                    } else {
                        textToOutput = dataEvent.getContext().getString(R.string.defining);
                    }
                }
                textOutput.setText(textToOutput);
            }});

    }


}
