package com.onettm.directions;


import android.content.Context;
import android.graphics.Matrix;
import android.location.Location;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

public class MyTextView extends TextView implements Handlerable{

    private LocationItem targetLocationItem;
    private Handler handler = new Handler();
    private Runnable runnable;

    public MyTextView(Context context) {
        super(context);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addHandler(){
        runnable = new Runnable() {

            @Override
            public void run() {
                Data modelData = DirectionsApplication.getInstance().getModel().getData();
                String result = "";
                if ((modelData.getLocation() != null) && (targetLocationItem.getLocation() != null)) {
                    result = String.format("%4.0f m : %s", modelData.getLocation().distanceTo(targetLocationItem.getLocation()), targetLocationItem.getName());
                }
                setText(result);
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 100);
    }

    public void removeHandler(){
        if (runnable!=null) {
            handler.removeCallbacks(runnable);
        }
    }

    public void setTargetLocationItem(LocationItem targetLocationItem) {
        this.targetLocationItem = targetLocationItem;
    }
}
