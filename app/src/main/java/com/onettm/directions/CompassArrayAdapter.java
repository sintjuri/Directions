package com.onettm.directions;

import android.content.Context;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CompassArrayAdapter extends BaseAdapter {

    private final Data modelData;
    Context context;
    LocationItem[] data;
    private static LayoutInflater inflater = null;

    public CompassArrayAdapter(Context context, LocationItem[] data, Data modelData) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.data = data;
        this.modelData = modelData;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.row, null);
        ImageView arrow = (ImageView) vi.findViewById(R.id.arrow);


        final float angle = -1*modelData.getPositiveBearing() + modelData.getDestinationBearing(data[position].getLocation()) - modelData.getDeclination();

        Matrix matrix = new Matrix();
        arrow.setScaleType(ImageView.ScaleType.MATRIX);   //required
        matrix.postRotate(angle, arrow.getDrawable().getIntrinsicWidth()/2, arrow.getDrawable().getIntrinsicHeight()/2);
        arrow.setImageMatrix(matrix);

        TextView text = (TextView) vi.findViewById(R.id.text);
        text.setText(data[position].toString());
        return vi;
    }
}
