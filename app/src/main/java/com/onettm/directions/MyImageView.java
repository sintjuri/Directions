package com.onettm.directions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.location.Location;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MyImageView extends ImageView implements Handlerable{

    private Location targetLocation;
    private Handler handler = new Handler();
    private Runnable runnable;

    public MyImageView(Context context) {
        super(context);

    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addHandler(){
        runnable = new Runnable() {

            @Override
            public void run() {
                Data modelData = DirectionsApplication.getInstance().getModel().getCachedData();
                final float angle = -1 * modelData.getPositiveBearing() + modelData.getDestinationBearing(targetLocation) /*+ modelData.getDeclination()*/;

                Matrix matrix = new Matrix();
                setScaleType(ScaleType.MATRIX);   //required
                matrix.postRotate(angle, getDrawable().getIntrinsicWidth() / 2, getDrawable().getIntrinsicHeight() / 2);
                setImageMatrix(matrix);
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


    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
    }
}