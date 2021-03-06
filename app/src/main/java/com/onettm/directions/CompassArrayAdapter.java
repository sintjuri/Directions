package com.onettm.directions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CompassArrayAdapter extends BaseAdapter {

    Context context;
    LocationItem[] data;

    public CompassArrayAdapter(Context context, LocationItem[] data) {
        this.context = context;
        this.data = data;
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
        MyViewHolder holder = null;

        View vi = convertView;

        if(convertView != null)
            holder = (MyViewHolder)convertView.getTag();

        if (holder == null) {
            holder = new MyViewHolder ();
            vi = LayoutInflater.from(context).inflate(R.layout.row, parent, false);
            holder.setText((TextView) vi.findViewById(R.id.text));
            holder.setArrow((MyImageView)vi.findViewById(R.id.arrow));
            vi.setTag(holder);
        }

        Data modelData = DirectionsApplication.getInstance().getModel().getCachedData();
        String result = "";
        if ((modelData.getLocation() != null) && (data[position]!=null) && (data[position].getLocation() != null)) {
            result = String.format("%4.0f m : %s", modelData.getLocation().distanceTo(data[position].getLocation()), data[position].getName());
        }
        holder.getText().setText(result);
        holder.getArrow().removeHandler();
        holder.getArrow().setTargetLocation(data[position].getLocation());
        holder.getArrow().addHandler();

        return vi;
    }


}
