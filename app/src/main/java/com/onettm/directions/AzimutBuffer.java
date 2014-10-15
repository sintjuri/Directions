package com.onettm.directions;

import android.location.Location;
import android.util.Log;

/**
 * Created by sintyaev on 14.10.14.
 */
public class AzimutBuffer extends Buffer<Double> {
    public AzimutBuffer(int size) {
        super(size);
    }

    @Override
    public Double getAverageValue() {
        double result = 0;
        if (data.size() > 0) {
            int count = 0;
            for (Double d : data) {
                Log.d("DATA", d.toString());
                result += d;
                count++;
            }
            Log.d("COUNT", count+"");
            Log.d("AVERAGE", (result/count) + "");
            return result / count;
        } else {
            return Double.valueOf(0);
        }
    }
}
