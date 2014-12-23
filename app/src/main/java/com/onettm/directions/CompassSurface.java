/*******************************************************************************
 * NiceCompass
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.onettm.directions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

public class CompassSurface extends SurfaceView implements SurfaceHolder.Callback {


    private final Model model;
    private final CompassActivity.PlaceholderFragment context;

    class CompassThread extends Thread {

        private static final int TARGET_FPS = 30;
        private static final int MINIMUM_SLEEP_TIME = 10;

        private static final int REQUIRED_BEARING_CHANGE = 5;
        private static final int REQUIRED_BEARING_REPEAT = 40;


        private static final float INNER_COMPASS_CARD_RATIO = 7f / 11f;
        private static final float COMPASS_CENTER_X = 50f;
        private static final float COMPASS_CENTER_Y = 50f;
        private static final float CARD_DIAMETER = 100f;
        private static final float POINTER_DIAMETER = CARD_DIAMETER + CARD_DIAMETER*(1-INNER_COMPASS_CARD_RATIO)/2;

        private static final float BEARING_X = 50f;
        private static final float BEARING_Y = 8f;

        private static final float COMPASS_ACCEL_RATE = 0.9f;
        private static final float COMPASS_SPEED_MODIFIER = 0.26f;

        private Model model;
        private Data data;

        /**
         * variables *
         */

        private final SurfaceHolder mSurfaceHolder;
        private volatile boolean mRun;

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


        public CompassThread(SurfaceHolder holder, Model model) {

            // get handles to some important objects
            mSurfaceHolder = holder;
            this.model = model;

            /*Resources res = context.getResources();
// cache handles to our key sprites & other drawables
            mLanderImage = context.getResources().getDrawable(
                    R.drawable.lander_plain);
            mFiringImage = context.getResources().getDrawable(
                    R.drawable.lander_firing);
            mCrashedImage = context.getResources().getDrawable(
                    R.drawable.lander_crashed);*/
        }

        private void updateCompass(Data data) {
            synchronized (mSurfaceHolder) {
                float newBearing = data.getPositiveBearing();
                //float newBearing = bearing;
                // adjust the new bearing to prevent problems involving 360 -- 0
                if (compassCurrentBearing < 90 && newBearing > 270) {
                    newBearing -= 360;
                }
                if (compassCurrentBearing > 270 && newBearing < 90) {
                    newBearing += 360;
                }
                //accuracyText = "target: "+newBearing+" position:"+compassCurrentBearing;

                float distance = newBearing - compassCurrentBearing;
                float targetSpeed = distance * COMPASS_SPEED_MODIFIER;
                // accelerate the directions accordingly
                if (targetSpeed > compassSpeed) {
                    compassSpeed += COMPASS_ACCEL_RATE;
                }
                if (targetSpeed < compassSpeed) {
                    compassSpeed -= COMPASS_ACCEL_RATE;
                }
                // stop the directions speed dropping too low
                compassCurrentBearing += compassSpeed;

                // adjust the bearing for a complete circle
                if (compassCurrentBearing >= 360) {
                    compassCurrentBearing -= 360;
                }
                if (compassCurrentBearing < 0) {
                    compassCurrentBearing += 360;
                }
            }
        }

        private void updateBearing(Data data) {
            synchronized (mSurfaceHolder) {
                // work out the bearing, dampening jitter
//*                float newBearing = directions.getPositiveBearing(true);
                float newBearing = data.getPositiveBearing();
                if (Math.abs(bearing - newBearing) > REQUIRED_BEARING_CHANGE) {
                    bearing = newBearing; // the change is to insignificant to be displayed
                    repeatedBearingCount = 0; // reset the repetition count
                } else {
                    repeatedBearingCount++;
                    if (repeatedBearingCount > REQUIRED_BEARING_REPEAT) {
                        bearing = newBearing;
                        repeatedBearingCount = 0;
                    }
                }
            }
        }

        void update(Data data) {
//*            synchronized (mSurfaceHolder) {
                updateBearing(data);
                updateCompass(data);
                updateAccuracy(data);
//*            }
        }

        public void doDraw(Canvas canvas,final Data data) {


            // update the scales
            float widthScale = getWidthScale();
            float heightScale = getHeightScale();

            canvas.drawColor(creamPaint.getColor()); // blank the screen
            //getBackgroundGradientDrawable().draw(canvas);


            context.getHandler().post(new Runnable() {
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
            });


            /*blackPaint.setTextSize(25f);
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
            canvas.drawText(textToOutput, (BEARING_X * widthScale) - getTextCenterOffset(textToOutput, blackPaint), BEARING_Y * heightScale, blackPaint);
*/
                // draw the inside of the directions card
                int cardDiameter = (int) Math.floor(CARD_DIAMETER * widthScale);

            Rect centerRect = new Rect((int) Math.floor(COMPASS_CENTER_X * widthScale - ((cardDiameter * INNER_COMPASS_CARD_RATIO) / 2)),
                    (int) Math.floor(COMPASS_CENTER_Y * heightScale - ((cardDiameter * INNER_COMPASS_CARD_RATIO) / 2)),
                    (int) Math.floor(COMPASS_CENTER_X * widthScale + ((cardDiameter * INNER_COMPASS_CARD_RATIO) / 2)),
                    (int) Math.floor(COMPASS_CENTER_Y * heightScale + ((cardDiameter * INNER_COMPASS_CARD_RATIO) / 2)));
            // draw the right status
            if (displayedStatus == Model.STATUS_INTERFERENCE) {
                canvas.drawBitmap(interferenceImage, null, centerRect, imagePaint);
            }


            // draw the directions card
            canvas.rotate(compassCurrentBearing * -1, COMPASS_CENTER_X * widthScale, COMPASS_CENTER_Y * heightScale);
            int cardX = (int) Math.floor(COMPASS_CENTER_X * widthScale - (cardDiameter / 2));
            int cardY = (int) Math.floor(COMPASS_CENTER_Y * heightScale - (cardDiameter / 2));
            Rect cardRect = new Rect(cardX, cardY, cardX + cardDiameter, cardY + cardDiameter);
            canvas.drawBitmap(cardImage, null, cardRect, imagePaint);
            //canvas.restore();

            int pointerDiameter = (int) Math.floor(POINTER_DIAMETER * widthScale);

            // draw the pointer
            canvas.rotate(data.getDestinationBearing(), COMPASS_CENTER_X * widthScale, COMPASS_CENTER_Y * heightScale);
            int pointerX = (int) Math.floor(COMPASS_CENTER_X * widthScale - (pointerDiameter / 2));
            int pointerY = (int) Math.floor(COMPASS_CENTER_Y * heightScale - (pointerDiameter / 2));
            Rect pointerRect = new Rect(pointerX, pointerY, pointerX + pointerDiameter, pointerY + pointerDiameter);
            canvas.drawBitmap(pointerImage, null, pointerRect, imagePaint);
            //canvas.restore();

            // draw the locked bearing
/*
            canvas.rotate(data.getDestinationBearing(), COMPASS_CENTER_X * widthScale, COMPASS_CENTER_Y * heightScale);
            bluePaint.setStyle(Paint.Style.STROKE);
            bluePaint.setStrokeWidth(3f);
            canvas.drawLine(COMPASS_CENTER_X * widthScale, cardY, COMPASS_CENTER_X * widthScale, cardY + ((1 - INNER_COMPASS_CARD_RATIO) * cardDiameter / 2), bluePaint);
*/
            //canvas.restore();

            // draw the bezel
            darkGreyPaint.setStyle(Paint.Style.STROKE);
            darkGreyPaint.setStrokeWidth(6f);
            canvas.drawCircle(COMPASS_CENTER_X * widthScale, COMPASS_CENTER_Y * heightScale, cardDiameter / 2 + 2f, darkGreyPaint);
            //canvas.drawLine(COMPASS_CENTER_X * widthScale, cardY, COMPASS_CENTER_X * widthScale, cardY + ((1 - INNER_COMPASS_CARD_RATIO) * cardDiameter / 2), darkGreyPaint);
            darkGreyPaint.setStyle(Paint.Style.FILL);

        }

        void initDrawing() {
//*            synchronized (mSurfaceHolder) {
                cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.card);
                pointerImage = BitmapFactory.decodeResource(getResources(), R.drawable.pointer);
                interferenceImage = BitmapFactory.decodeResource(getResources(), R.drawable.interference);

                imagePaint = new Paint();
                imagePaint.setDither(true);
                blackPaint = new Paint();
                blackPaint.setColor(Color.BLACK);
                Paint greyPaint = new Paint();
                greyPaint.setARGB(255, 179, 179, 179);
                darkGreyPaint = new Paint();
                darkGreyPaint.setARGB(255, 112, 112, 112);
                creamPaint = new Paint();
                creamPaint.setARGB(255, 222, 222, 222);
                Paint redPaint = new Paint();
                redPaint.setColor(Color.RED);
                bluePaint = new Paint();
                bluePaint.setARGB(255, 0, 94, 155);

//*            }
        }

        float getTextCenterOffset(String text, Paint paint) {
            synchronized (mSurfaceHolder) {
                float[] widths = new float[text.length()];
                paint.getTextWidths(text, widths);
                float totalWidth = 0;
                for (int i = 0; i < text.length(); i++) {
                    totalWidth += widths[i];
                }
                return totalWidth / 2;
            }
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
            while (mRun) {
                data = model.getData();
//*                synchronized (mSurfaceHolder) {
                    // record the start time
                    long startTime = System.currentTimeMillis();
                    // update the animation
                    update(data);
                    triggerDraw(data);

                    // work out how long to sleep for
                    long finishTime = System.currentTimeMillis();
                    long requiredSleepTime = maxSleepTime - (finishTime - startTime);
                    // check if the sleep time was too low
                    if (requiredSleepTime < MINIMUM_SLEEP_TIME) {
                        requiredSleepTime = MINIMUM_SLEEP_TIME;
                    }


                    // try to sleep for this time
                    try {
                        Thread.sleep(requiredSleepTime);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
//*                }
            }
        }
    }

    /**
     * constants *
     */

    private static final int STATUS_NO_EVENT = -1;


    //private SensorListener directions;
    //private volatile boolean isRunning;

    // images
    private Bitmap cardImage;
    private Bitmap pointerImage;
    private Bitmap interferenceImage;


    // paint
    private Paint imagePaint;
    private Paint darkGreyPaint;
    private Paint creamPaint;
    private Paint blackPaint;
    private Paint bluePaint;


    private float cachedWidthScale;
    private float cachedHeightScale;

    private int displayedStatus;

    private float bearing;
    private int repeatedBearingCount;

    private float compassCurrentBearing;
    private float compassSpeed;



    private CompassThread animationThread;

    float getWidthScale() {
        // check if the scale needs to be initialized
        if (cachedWidthScale == 0f) {
            cachedWidthScale = this.getWidth() / 100f;
        }
        return cachedWidthScale;
    }

    float getHeightScale() {
        // check if the scale needs to be initialized
        if (cachedHeightScale == 0f) {
            cachedHeightScale = this.getHeight() / 100f;
        }
        return cachedHeightScale;
    }


    public CompassSurface(CompassActivity.PlaceholderFragment context, Model model) {
        super(context.getActivity());
        this.context = context;
        this.model = model;

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }



    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {

        animationThread = new CompassThread(holder, model);
        animationThread.setRunning(true);
        animationThread.start();
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopAnimation();
    }

    public void stopAnimation(){
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
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
