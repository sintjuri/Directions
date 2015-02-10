package com.onettm.directions;

import android.widget.TextView;

public class MyViewHolder {
    private MyTextView text;
    private MyImageView arrow;

    public MyTextView getText() {
        return text;
    }

    public void setText(MyTextView text) {
        this.text = text;
    }

    public MyImageView getArrow() {
        return arrow;
    }

    public void setArrow(MyImageView arrow) {
        this.arrow = arrow;
    }
}

