package com.onettm.directions;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener, LocationListener {
    private static final double ROTATION_SPEED_KOEF = 0.72;
    // define the display assembly compass picture
    private ImageView image;
    private ImageView pointer;
    private TextView tvHeading;

    private float[] gravity = new float[3];
    private float[] geoMagnetic = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private double magneticCurrentDegree;
    private double pointerCurrentDegree;

    private AzimutBuffer azimutBuffer = new AzimutBuffer(5);

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    private LocationBuffer locationBuffer = new LocationBuffer(5);

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 500;

    // Declaring a Location Manager
    protected LocationManager locationManager;

    private Location cicusLocation = new Location("");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.imageViewCompass);
        pointer = (ImageView) findViewById(R.id.imageViewPointer);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.accelerometer_not_available)
                    .show();
        }
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetometer == null) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.magnetometer_not_available)
                    .show();
        }
        // getting GPS status
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if (!isGPSEnabled) {
            showSettingsAlert();
        }

        double circusLatitude = 51.656608;
        cicusLocation.setLatitude(circusLatitude);
        double circusLongitude = 39.185975;
        cicusLocation.setLongitude(circusLongitude);
    }

    /**
     * Function to show settings alert dialog
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.gps_settings);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.enable_gps);

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners

        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        // First get location from Network Provider
        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            if (locationManager != null) {
                Location location = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(location!=null) {
                    locationBuffer.add(location);
                }
            }
        }
        // if GPS Enabled get lat/long using GPS Services
        if (isGPSEnabled) {

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            if (locationManager != null) {
                Location location = locationManager
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location!=null) {
                    locationBuffer.add(location);
                }
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
        stopUsingGPS();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Double azimut = calculateAzimut(event);
        if(azimut!=null) {
            azimutBuffer.add(azimut);
            rotateImagesArroundCenter();
        }

    }

    private RotateAnimation createRotateAnimation(float fromDegree, float toDegree){

        fromDegree = (fromDegree % 360) + 360;
        toDegree = (toDegree % 360) + 360;

        if (Math.abs(toDegree - fromDegree)>180){
            toDegree = 360 - toDegree;
        }

        RotateAnimation raImage = new RotateAnimation(
                fromDegree,
                toDegree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        raImage.setDuration(Math.round(Math.abs(toDegree - fromDegree)*ROTATION_SPEED_KOEF));
        raImage.setFillAfter(true);
        return raImage;
    }

    private void rotateImagesArroundCenter() {
        /*Matrix matrix = new Matrix();
        image.setScaleType(ImageView.ScaleType.MATRIX);   //required

        matrix.setRotate((float) (-1 * azimut), image.getDrawable().getBounds().width() / 2, image.getDrawable().getBounds().height() / 2);
        image.setImageMatrix(matrix);*/

        if ((!azimutBuffer.isRendered())) {
            RotateAnimation raImage = createRotateAnimation(
                    (float) magneticCurrentDegree,
                    (float) -azimutBuffer.getAverageValue());

            this.image.startAnimation(raImage);
            magneticCurrentDegree = -azimutBuffer.getAverageValue();

            this.image.startAnimation(raImage);
            magneticCurrentDegree = -azimutBuffer.getAverageValue();

        }
        if ((!locationBuffer.isRendered()) && (!azimutBuffer.isRendered())) {
            float bearing = locationBuffer.getAverageValue().bearingTo(cicusLocation);
            RotateAnimation raPointer = createRotateAnimation(
                    (float) pointerCurrentDegree,
                    (float) (bearing - azimutBuffer.getAverageValue()));

            this.pointer.startAnimation(raPointer);
            pointerCurrentDegree = bearing -  azimutBuffer.getAverageValue();

            this.pointer.startAnimation(raPointer);
            pointerCurrentDegree = bearing -  azimutBuffer.getAverageValue();

        }
        if ((!locationBuffer.isRendered()) && (!azimutBuffer.isRendered())) {
            tvHeading.setText(getString(R.string.distance, locationBuffer.getAverageValue().distanceTo(cicusLocation)));
            //tvHeading.setText("cur location: " + location.getLatitude() + " " + location.getLongitude());
        }
    }

    private Double calculateAzimut(SensorEvent event) {
    /*get gravity value arrays from Accelerometer*/
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, event.values.length);
            mLastAccelerometerSet = true;
        }
        /*get gravity value arrays from Magnet*/
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geoMagnetic, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
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
                return (Math.toDegrees(orientation[0]) + 360) % 360;
            }
        }
        return null;
    }


    @Override
    public void onLocationChanged(Location location) {
        if(location!=null) {
            locationBuffer.add(location);
            rotateImagesArroundCenter();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}