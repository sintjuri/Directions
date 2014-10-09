package com.onettm.directions;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Matrix;
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
    private TextView tvHeading;

    private float[] gravity = new float[3];
    private float[] geoMagnetic = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private double mCurrentDegree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);


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
        double azimut = calculateAzimut(event);

        if (azimut != 0) {
            tvHeading.setText("Heading: " + azimut + " degrees");
            rotateImageArroundCenter(azimut);
        }
    }

    private void rotateImageArroundCenter(double azimut) {
        /*Matrix matrix = new Matrix();
        image.setScaleType(ImageView.ScaleType.MATRIX);   //required

        matrix.setRotate((float) (-1 * azimut), image.getDrawable().getBounds().width() / 2, image.getDrawable().getBounds().height() / 2);
        image.setImageMatrix(matrix);*/

        RotateAnimation ra = new RotateAnimation(
                (float)mCurrentDegree,
                (float)-azimut,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        ra.setDuration(50);

        ra.setFillAfter(true);

        image.startAnimation(ra);
        mCurrentDegree = -azimut;
    }

    private double calculateAzimut(SensorEvent event) {
    /*get gravity value arrays from Accelerometer*/
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            System.arraycopy(event.values, 0, gravity, 0, event.values.length);
            mLastAccelerometerSet = true;
        }
        /*get gravity value arrays from Magnet*/
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geoMagnetic, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet){
            mLastAccelerometerSet = false;
            mLastMagnetometerSet = false;
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
                return (float)(Math.toDegrees(orientation[0])+360)%360;
            }
        }
        return 0;
    }


}