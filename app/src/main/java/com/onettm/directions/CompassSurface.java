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

import java.util.concurrent.Callable;

public class CompassSurface extends SurfaceView implements SurfaceHolder.Callback {

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
        if (animationThread == null) {
            createThread();
        }
    }

    public void pause() {
        animationThread.standBy();
    }

    public void resume() {
        if (animationThread != null)
        animationThread.wake();
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

    public void createThread() {
        animationThread = new CompassThread();
        animationThread.threadRunning = true;
        animationThread.setTask(new surfaceUpdateTask(getHolder()));
        animationThread.start();
    }

    public void stopAnimation() {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode

        if (animationThread != null) {
            animationThread.threadRunning = false;

            boolean retry = true;
            while (retry) {
                try {
                    synchronized (animationThread) {
                        animationThread.notifyAll();
                    }
                    animationThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    Log.d("CompassSurface", "stopAnimation");
                }
            }
            animationThread = null;
        }
    }

    class CompassThread extends Thread {

        private static final int TARGET_FPS = 30;
        private static final int MINIMUM_SLEEP_TIME = 10;

        /**
         * variables *
         */

        private volatile boolean threadRunning = false;
        private volatile Callable task;
        private volatile boolean standBy = false;

        public CompassThread() {
            super("AnimationThread");
        }

        public synchronized void setTask(Callable task) {
            this.task = task;
            threadRunning = true;
            standBy = false;
            wake();
        }

        public void run() {
            long maxSleepTime = (long) Math.floor(1000 / TARGET_FPS);
            long plannedFinish;
            long finishTime;
            while (threadRunning) {
                long requiredSleepTime;
                long startTime = System.currentTimeMillis();

                if (task != null) try {
                    task.call();
                } catch (Exception e) {
                    Log.d(CompassSurface.class.getCanonicalName(), "exception calling surface update", e);
                }
                else
                    Log.d(this.getClass().getCanonicalName(), "No callable set");
                finishTime = System.currentTimeMillis();

                requiredSleepTime = maxSleepTime - (finishTime - startTime);
                if (requiredSleepTime < MINIMUM_SLEEP_TIME) {
                    requiredSleepTime = MINIMUM_SLEEP_TIME;
                }
                Log.d(this.getClass().getCanonicalName(), "duration: " + (finishTime - startTime));
                synchronized (this) {
                    try {
                        plannedFinish = System.currentTimeMillis() + requiredSleepTime;
                        long waitTime;
                        while(threadRunning && standBy) wait();
                        while (threadRunning && ((waitTime = plannedFinish - System.currentTimeMillis() ) > MINIMUM_SLEEP_TIME)) {
                            Log.d(this.getClass().getCanonicalName(), "will wait for " + waitTime + "ms");
                            wait(waitTime);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

            }
        }

        public synchronized void standBy() {
            threadRunning = true;
            standBy = true;
        }

        public synchronized void wake() {
            threadRunning = true;
            standBy = false;
            notifyAll();
        }

    }
    private class surfaceUpdateTask implements Callable {
        private static final int REQUIRED_BEARING_CHANGE = 5;
        private static final int REQUIRED_BEARING_REPEAT = 40;


        private static final float COMPASS_ACCEL_RATE = 0.9f;
        private static final float COMPASS_SPEED_MODIFIER = 0.26f;
        private static final int STATUS_NO_EVENT = -1;
        private final SurfaceHolder surfaceHolder;
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

        private surfaceUpdateTask(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
            initDrawing();
        }

        @Override
        public Object call() throws Exception {
            Model model = DirectionsApplication.getInstance().getModel();
            Data data = model.getData();
            update(data);
            triggerDraw(data);
            return null;
        }

        void triggerDraw(Data data) {
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    this.doDraw(canvas, data);
                }

            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        private void updateBearing(Data data) {

            float newBearing = data.getPositiveBearing();
            Log.d(this.getClass().getCanonicalName(), "bearing: " + newBearing);
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

        void update(Data data) {
            updateBearing(data);
            updateAccuracy(data);

        }

        private void updateAccuracy(Data data) {
            int status = data.getStatus();
            if (displayedStatus == STATUS_NO_EVENT) {
                if (status == Model.STATUS_INTERFERENCE) {
                    displayedStatus = status;
                }
            }
        }

        public void doDraw(Canvas canvas, final Data data) {
            float canvasSize = Math.min(getCanvasWidth(), getCanvasHeight());
            float canvasCenterX = getCanvasWidth() / 2;
            float canvasCenterY = getCanvasHeight() / 2;
            canvas.drawColor(creamPaint.getColor()); // blank the screen
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

        }
    }
}
