package com.o3dr.android.dp.wear.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.o3dr.android.dp.wear.R;

import timber.log.Timber;

/**
 * Created by Toby on 8/7/2015.
 */
public class JoystickView extends View {
    private static float ENGAGE_THRESHOLD = 0.5f;
    private boolean engaged;
    private JoystickListener listener;
    private float x, y;
    private static final float STROKE_WIDTH = 10f;
    public JoystickView(Context context) {
        super(context);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public interface JoystickListener{
        void onJoystickEngaged(float x, float y);
        void onJoystickMoved(float x, float y);
        void onJoystickReleased(float x, float y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
            x = (event.getX() - getWidth() / 2) / (getWidth() / 2);
            y = (event.getY() - getHeight() / 2) / (getHeight() / 2);
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                float dist = (float)Math.sqrt(x * x + y* y);
                if(dist < ENGAGE_THRESHOLD){
                    engaged = true;
                    dispatchEngaged(x, y);
                    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    invalidate();
                    return true;
                }
                x = 0;
                y = 0;
                break;
            case MotionEvent.ACTION_MOVE:

                if(engaged) {
                    dispatchMotion(x, y);
                    invalidate();
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(engaged) {
                    ValueAnimator elasticToCenter = ValueAnimator.ofFloat(1f, 0f);
                    elasticToCenter.setInterpolator(new DecelerateInterpolator());
                    elasticToCenter.setDuration(100);
                    final float xStart = x, yStart = y;
                    dispatchReleased(x, y);
                    elasticToCenter.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            x = xStart * (float) animation.getAnimatedValue();
                            y = yStart * (float) animation.getAnimatedValue();
                            invalidate();
                        }
                    });
                    elasticToCenter.start();
                    engaged = false;
                    return true;
                }
        }
        return false;
    }

    public void setJoystickListener(JoystickListener listener){
        this.listener = listener;
    }

    private void dispatchReleased(float x, float y) {
        if(listener != null){
            listener.onJoystickReleased(x, y);
        }
    }

    private void dispatchEngaged(float x, float y) {
        if(listener != null){
            listener.onJoystickEngaged(x, y);
        }
    }

    private void dispatchMotion(float x, float y){
        if(listener != null){
            listener.onJoystickMoved(x, y);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float dist = (float)Math.sqrt(x * x + y* y);
        float x = getWidth()/2 + this.x*getWidth()/2;
        float y = getHeight()/2 + this.y*getHeight()/2;
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.red));
        paint.setStrokeWidth(STROKE_WIDTH * (float)(Math.sqrt(2) - dist));
        canvas.drawLine(getWidth() / 2, getHeight() / 2, x, y, paint);
        canvas.drawCircle(x, y, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, STROKE_WIDTH, getResources().getDisplayMetrics()), paint);
        super.onDraw(canvas);
    }
}
