package com.onettm.directions;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    private TextView tvHeading;

    private float azimut;
    private float[] gravity;
    private float[] geoMagnetic;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        image = (ImageView) findViewById(R.id.imageViewCompass);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            new AlertDialog.Builder(this)
                    .setMessage("Accelerometer is not available")
                    .show();
        }
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetometer == null) {
            new AlertDialog.Builder(this)
                    .setMessage("Magnetometer is not available")
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);


    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /*get gravity value arrays from Accelerometer*/
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values;
        /*get gravity value arrays from Magnet*/
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geoMagnetic = event.values;
        if (gravity != null && geoMagnetic != null) {
        /*Rotation matrix and Inclination matrix*/
            float R[] = new float[9];
            float I[] = new float[9];
        /* Compute the inclination matrix I as well as the rotation matrix R transforming a vector from the device
        coordinate system to the world's coordinate system
        R and I [Length 9]
        gravity vector expressed in the device's coordinate [Length 3]
        geoMagnetic vector expressed in the device's coordinate[Length 3]
        */
            boolean success = SensorManager.getRotationMatrix(R, I,
                    gravity, geoMagnetic);

            if (success) {
         /* Orientation has azimuth, pitch and roll */
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0];
            }
        }

        // Do something with these orientation angles.


        // get the angle around the z-axis rotated
        //float degree = Math.round(azimut);

        tvHeading.setText("Heading: " + Float.toString(-azimut*360/(2*3.14159f)) + " degrees");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -azimut*360/(2*3.14159f),
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -azimut*360/(2*3.14159f);

    }


}
/*

package com.onettm.directions;

import java.util.List;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView tvText;
    SensorManager sensorManager;
    List<Sensor> sensors;
    Sensor sensorLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvText = (TextView) findViewById(R.id.tvText);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    public void onClickSensList(View v) {

        sensorManager.unregisterListener(listenerLight, sensorLight);
        StringBuilder sb = new StringBuilder();

        for (Sensor sensor : sensors) {
            sb.append("name = ").append(sensor.getName())
                    .append(", type = ").append(sensor.getType())
                    .append("\nvendor = ").append(sensor.getVendor())
                    .append(" ,version = ").append(sensor.getVersion())
                    .append("\nmax = ").append(sensor.getMaximumRange())
                    .append(", resolution = ").append(sensor.getResolution())
                    .append("\n--------------------------------------\n");
        }
        tvText.setText(sb);
    }

    public void onClickSensLight(View v) {
        sensorManager.registerListener(listenerLight, sensorLight,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listenerLight, sensorLight);
    }

    SensorEventListener listenerLight = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            tvText.setText(String.valueOf(event.values[0]));
        }
    };

}
*/