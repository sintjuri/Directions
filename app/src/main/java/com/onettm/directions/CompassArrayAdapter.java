package com.onettm.directions;

import android.content.Context;
import android.graphics.Matrix;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CompassArrayAdapter extends BaseAdapter {

    public static final int DELAY = 1000;

    private final Model model;
    Context context;
    LocationItem[] data;

    public CompassArrayAdapter(Context context, LocationItem[] data, Model model) {
        this.context = context;
        this.data = data;
        this.model = model;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
       // if (convertView == null) {
            vi = LayoutInflater.from(context).inflate(R.layout.row, parent, false);
            final ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 3;
            params.gravity = Gravity.CENTER_VERTICAL;
            imageView.setLayoutParams(params);
            imageView.setImageResource(R.drawable.arrow);
            imageView.setTag("huy"+position);
            ((LinearLayout)vi).addView (imageView);
        //}

        /*LinearLayout linearLayout = new LinearLayout(context);
        AbsListView.LayoutParams LLParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(LLParams);


        final ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 3;
        params.gravity = Gravity.CENTER_VERTICAL;
        imageView.setLayoutParams(params);
        imageView.setImageResource(R.drawable.arrow);
        linearLayout.addView(imageView);*/


        //final ImageView arrow = (ImageView) vi.findViewById(R.id.arrow);


        final ImageView arrow = imageView;//(ImageView)vi.findViewWithTag("huy"+position);
        if (arrow == null) return vi;


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable(){

            @Override
            public void run() {
                Data modelData = model.getData();
                final float angle = -1*modelData.getPositiveBearing() + modelData.getDestinationBearing(data[position].getLocation()) - modelData.getDeclination();

                Matrix matrix = new Matrix();
                arrow.setScaleType(ImageView.ScaleType.MATRIX);   //required
                matrix.postRotate(angle, arrow.getDrawable().getIntrinsicWidth()/2, arrow.getDrawable().getIntrinsicHeight()/2);
                arrow.setImageMatrix(matrix);
                arrow.invalidate();
                handler.postDelayed(this, DELAY);
            }
        }, DELAY);

        /*TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(data[position].toString());
        linearLayout.addView(textView);*/

        TextView text = (TextView) vi.findViewById(R.id.text);
        text.setText(data[position].toString());
        /*vi = linearLayout;*/
        return vi;
    }
}
