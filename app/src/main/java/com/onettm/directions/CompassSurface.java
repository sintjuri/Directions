package com.onettm.directions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CompassSurface extends SurfaceView implements SurfaceHolder.Callback {


    //private final CompassActivity.PlaceholderFragment context;


    /**
     * constants *
     */

    private static final int STATUS_NO_EVENT = -1;
    // images
    private Bitmap cardImage;
    private Bitmap pointerImage;
    private Bitmap interferenceImage;
    // paint
    private Paint imagePaint;
    private Paint creamPaint;
    private int displayedStatus;
    private float compassBearingTo;
    private int repeatedBearingCount;
    private float compassCurrentBearing;
    private float compassSpeed;
    private CompassThread animationThread;

    public CompassSurface(Context context) {
        super(context);
        init();

    }

    public CompassSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CompassSurface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    float getCanvasWidth() {
        return getWidth();
    }

    float getCanvasHeight() {
        return getHeight();
    }

    private void init() {
        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        animationThread = new CompassThread(holder);
        animationThread.setRunning(true);
        animationThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopAnimation();
    }

    public void stopAnimation() {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        if (animationThread != null) {
            animationThread.setRunning(false);
            while (retry) {
                try {
                    animationThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    Log.d("CompassSurface", e.getMessage());
                }
            }
        }
    }

    /*public void pauseAnimation() {
        synchronized (DirectionsApplication.getInstance().getModel()) {
            Log.d("PROCESS!!", "wait1");
            animationThread.setPausing(true);
            Log.d("PROCESS!!", "wait2");
        }
    }

    public void resumeAnimation() {
        Model model = DirectionsApplication.getInstance().getModel();
        synchronized (model) {
            Log.d("PROCESS!!", "notify");
            animationThread.setPausing(false);
            model.notify();
        }
    }*/

    class CompassThread extends Thread {

        private static final int TARGET_FPS = 30;
        private static final int MINIMUM_SLEEP_TIME = 10;

        private static final int REQUIRED_BEARING_CHANGE = 3;
        private static final int REQUIRED_BEARING_REPEAT = 10;


        private static final float COMPASS_ACCEL_RATE = 0.9f;
        private static final float COMPASS_SPEED_MODIFIER = 0.26f;
        /**
         * variables *
         */

        private final SurfaceHolder mSurfaceHolder;
        private Data data;
        private volatile boolean mRun;

        private boolean flag = false;

        public CompassThread(SurfaceHolder holder) {
            mSurfaceHolder = holder;
        }

        /*public void setPausing(boolean b) {
            flag = b;
        }*/

        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         *
         * @param b true to run, false to shut down
         */
        public void setRunning(boolean b) {
            mRun = b;
        }

        private void updateBearing(Data data) {
            synchronized (mSurfaceHolder) {

                float newBearing = data.getPositiveBearing();
                if (Math.abs(compassBearingTo - newBearing) > REQUIRED_BEARING_CHANGE) {
                    compassBearingTo = newBearing;
                    repeatedBearingCount = 0;
                } else {
                    repeatedBearingCount++;
                    if (repeatedBearingCount > REQUIRED_BEARING_REPEAT) {
                        compassBearingTo = newBearing;
                        repeatedBearingCount = 0;
                    }
                }
                if (compassCurrentBearing < 90 && compassBearingTo > 270) {
                    compassBearingTo -= 360;
                }
                if (compassCurrentBearing > 270 && compassBearingTo < 90) {
                    compassBearingTo += 360;
                }
                float distance = compassBearingTo - compassCurrentBearing;
                float targetSpeed = distance * COMPASS_SPEED_MODIFIER;
                if (targetSpeed > compassSpeed) {
                    compassSpeed += COMPASS_ACCEL_RATE;
                }
                if (targetSpeed < compassSpeed) {
                    compassSpeed -= COMPASS_ACCEL_RATE;
                }
                // stop the directions speed dropping too low
                compassCurrentBearing += compassSpeed;

                // adjust the compassBearingTo for a complete circle
                if (compassCurrentBearing >= 360) {
                    compassCurrentBearing -= 360;
                }
                if (compassCurrentBearing < 0) {
                    compassCurrentBearing += 360;
                }
            }
        }

        void update(Data data) {
            updateBearing(data);
            updateAccuracy(data);

        }

        public void doDraw(Canvas canvas, final Data data) {
            float canvasSize = Math.min(getCanvasWidth(), getCanvasHeight());
            float canvasCenterX = getCanvasWidth() / 2;
            float canvasCenterY = getCanvasHeight() / 2;


            canvas.drawColor(creamPaint.getColor()); // blank the screen


            /*context.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    String textToOutput;

                    if (data.getLocation() != null) {
                        if (data.getDestinationDistance() > 0) {
                            textToOutput = getContext().getString(R.string.distance, data.getDestinationName(), data.getDestinationDistance());
                        } else {
                            textToOutput = getContext().getString(R.string.please_select);
                        }
                    } else {
                        textToOutput = getContext().getString(R.string.defining);
                    }

                    context.getTextOutput().setText(textToOutput);
                }
            });*/


            Rect cardRect = new Rect((int) Math.floor(canvasCenterX - canvasSize / 2), (int) Math.floor(canvasCenterY - canvasSize / 2), (int) Math.floor(canvasCenterX + canvasSize / 2), (int) Math.floor(canvasCenterY + canvasSize / 2));

            if (displayedStatus == Model.STATUS_INTERFERENCE) {
                canvas.drawBitmap(interferenceImage, null, cardRect, imagePaint);
            } else {
                canvas.rotate(compassCurrentBearing * -1, canvasCenterX, canvasCenterY);
                canvas.drawBitmap(cardImage, null, cardRect, imagePaint);
                canvas.rotate(data.getDestinationBearing() - data.getDeclination(), canvasCenterX, canvasCenterY);
                canvas.drawBitmap(pointerImage, null, cardRect, imagePaint);
            }

        }

        void initDrawing() {
//*            synchronized (mSurfaceHolder) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.card);
            pointerImage = BitmapFactory.decodeResource(getResources(), R.drawable.pointer);
            interferenceImage = BitmapFactory.decodeResource(getResources(), R.drawable.interference);

            imagePaint = new Paint();
            imagePaint.setDither(true);
            Paint blackPaint = new Paint();
            blackPaint.setColor(Color.BLACK);
            Paint greyPaint = new Paint();
            greyPaint.setARGB(255, 179, 179, 179);
            Paint darkGreyPaint = new Paint();
            darkGreyPaint.setARGB(255, 112, 112, 112);
            creamPaint = new Paint();
            creamPaint.setARGB(255, 222, 222, 222);
            Paint redPaint = new Paint();
            redPaint.setColor(Color.RED);
            Paint bluePaint = new Paint();
            bluePaint.setARGB(255, 0, 94, 155);

//*            }
        }

        private void updateAccuracy(Data data) {
            synchronized (mSurfaceHolder) {
                int status = data.getStatus();
                // check in case the status is already set to an event
                if (displayedStatus == STATUS_NO_EVENT) {
                    // only display statuses we can handle
                    if (status == Model.STATUS_INTERFERENCE) {
                        displayedStatus = status;
                    }
                }
            }
        }


        void triggerDraw(Data data) {
//*            synchronized (mSurfaceHolder) {
            //Log.d("callback", "triggerDraw");
            Canvas canvas = null;
            try {
            /*SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    Log.d("callback", "surfaceChanged");
                }

                public void surfaceCreated(SurfaceHolder holder) {
                    Log.d("callback", "surfaceCreated");
                }

                public void surfaceDestroyed(SurfaceHolder holder) {
                    Log.d("callback", "surfaceDestroyed");
                }
            };

            this.getHolder().addCallback(callback);*/
                //Log.d("callback", "beforeLock");
                canvas = mSurfaceHolder.lockCanvas();
                //Log.d("callback", "afterLock");
                if (canvas != null) {
                    //Log.d("callback", "beforeDraw");
                    this.doDraw(canvas, data);
                    //Log.d("callback", "afterDraw");
                }

            } finally {
                if (canvas != null) {
                    //Log.d("callback", "beforeUnlock");
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                    //Log.d("callback", "afterUnlock");
                }
            }
//*            }
        }

        public void run() {


            // set the directions position to prevent spinning
//*            compassCurrentBearing = directions.getPositiveBearing(true);

            // reset the status
//*            displayedStatus = STATUS_NO_EVENT;

            initDrawing();

            // initialize a timing variable
            long maxSleepTime = (long) Math.floor(1000 / TARGET_FPS);
            // loop whilst we are told to
            Model model = DirectionsApplication.getInstance().getModel();

            while (mRun) {
                long requiredSleepTime;
                synchronized (model) {
                    try {
                        while (flag) {
                            model.wait(1000);
                        }
                    } catch (InterruptedException e) {
                        Log.d("CompassSurface", e.getMessage());
                    }
                    data = model.getData();
//*                synchronized (mSurfaceHolder) {
                    // record the start time
                    long startTime = System.currentTimeMillis();
                    // update the animation
                    update(data);
                    triggerDraw(data);

                    // work out how long to sleep for
                    long finishTime = System.currentTimeMillis();
                    requiredSleepTime = maxSleepTime - (finishTime - startTime);
                    // check if the sleep time was too low
                    if (requiredSleepTime < MINIMUM_SLEEP_TIME) {
                        requiredSleepTime = MINIMUM_SLEEP_TIME;
                    }
                    // try to sleep for this time
                    try {
                        Thread.sleep(requiredSleepTime);
                    } catch (InterruptedException e) {
                        Log.d("CompassSurface", e.getMessage());
                    }
                }


            }
        }
    }
}
