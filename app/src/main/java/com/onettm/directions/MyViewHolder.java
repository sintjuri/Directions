package com.onettm.directions;

import android.widget.TextView;

public class MyViewHolder {
    private TextView text;
    private MyImageView arrow;

    public TextView getText() {
        return text;
    }

    public void setText(TextView text) {
        this.text = text;
    }

    public MyImageView getArrow() {
        return arrow;
    }

    public void setArrow(MyImageView arrow) {
        this.arrow = arrow;
    }
}

