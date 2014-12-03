/**
 *
 * Copyright 2013-2014 UPnP Forum All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE OR WARRANTIES OF 
 * NON-INFRINGEMENT, ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are 
 * those of the authors and should not be interpreted as representing official 
 * policies, either expressed or implied, by the UPnP Forum.
 *
 **/
package com.comarch.android.upnp.ibcdemo.ui.newview.widgets;

import com.comarch.android.upnp.ibcdemo.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class RotaryKnobView extends View {

    enum SwitchState {
        ON,OFF,CHANGING
    }
    private final String TAG = this.getClass().getSimpleName();

    private final boolean ABSOLUTE_KNOB_CONTROLL = true;
    private final float BOTTOM_ANGLE_LOCK = 25.0f;

    private final float MINIMAL_STEP = 0.01f;

    private float angle = 0f;
    private float theta_old = 0f;
    private float knobValue_old;
    private float knobValue;

    private SwitchState switchState = null;
    private boolean boolSwitchState = false;

    private Bitmap button;
    private Bitmap scalledBitmap;
    
    private Bitmap draggerBitmap;

    private Paint paint = new Paint();
    private Paint shadowPaint = new Paint(0);
    private Paint gradientBackgroundPaint = new Paint();
    
    private RectF rectF = new RectF();
    private RectF shadowRect = new RectF();

    private int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
    private int x = (getMeasuredWidth() - size) / 2;
    private int y = (getMeasuredHeight() - size) / 2;
    private int margin = 44;
    private float radiusOfKnob = 24.0f;
    private int radiusOfButton = 0;
    private int progresWidth = 14;
    
    private Point centerOfButton = new Point();
    private Point centerOfKnob = new Point();

    private RotaryKnobListener knobListener;

    public interface RotaryKnobListener {
        public void onKnobChanged(double arg);

        public void onSwitchChanged(boolean state);
    }

    public void setKnobListener(RotaryKnobListener l) {
        knobListener = l;
    }

    public RotaryKnobView(Context context) {
        super(context);
        initialize();
    }

    public RotaryKnobView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public RotaryKnobView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public void setSwitch(boolean value) {
        SwitchState newState = value ? SwitchState.ON : SwitchState.OFF;
        if (switchState != newState) {
            updateSwitchState(newState);
        }
    }

    public void setValue(double value) {
        float fullAngle = 360.0f - 2 * BOTTOM_ANGLE_LOCK;
        angle = 180.0f + BOTTOM_ANGLE_LOCK + (float) value * fullAngle;

        if (!ABSOLUTE_KNOB_CONTROLL) {
            if (angle > 360.0f) {
                angle -= 360;
            } else if (angle < 0.0f) {
                angle += 360;
            }

            float leftSize = (360.0f - (180.0f + BOTTOM_ANGLE_LOCK)) / (360.0f - 2 * BOTTOM_ANGLE_LOCK);

            knobValue_old = angle >= 180.0f + BOTTOM_ANGLE_LOCK ? leftSize * ((angle - (180.0f + BOTTOM_ANGLE_LOCK)) / (360.0f - (180.0f + BOTTOM_ANGLE_LOCK)))
                    : leftSize + (1.0f - leftSize) * (angle / (180.0f - BOTTOM_ANGLE_LOCK));
        } else {
            boolean onRight = false;

            if (angle > 449.0f) {
                angle -= 449.0f;
                onRight = true;
            }

            float leftSize = (449.0f - (180.0f + BOTTOM_ANGLE_LOCK)) / (360.0f - 2 * BOTTOM_ANGLE_LOCK);

            knobValue_old = angle >= 180.0f + BOTTOM_ANGLE_LOCK ? leftSize * ((angle - (180.0f + BOTTOM_ANGLE_LOCK)) / (449.0f - (180.0f + BOTTOM_ANGLE_LOCK)))
                    : leftSize + (1.0f - leftSize) * angle / ((180.0f - BOTTOM_ANGLE_LOCK) - 90);

            if (onRight) {
                angle += 90.0f;
            }

            if (knobValue_old > 1.0f) {
                knobValue_old = 1.0f;
                angle = 180.0f - BOTTOM_ANGLE_LOCK;
            }
        }
        invalidate();
    }

    private float getTheta(float x, float y) {
        float sx = x - (getMeasuredWidth() / 2.0f);
        float sy = y - (getMeasuredHeight() / 2.0f);

        float length = (float) Math.sqrt(sx * sx + sy * sy);
        float nx = sx / length;
        float ny = sy / length;
        float theta = (float) Math.atan2(ny, nx);

        final float rad2deg = (float) (180.0 / Math.PI);
        float theta2 = theta * rad2deg;

        return (theta2 < 0) ? theta2 + 360.0f : theta2;
    }

    private void updateSwitchState(SwitchState state) {
        if(state == switchState) return;
        switchState = state;
        int id;
        switch (state) {
        case OFF:
            id = R.drawable.bulb_switch_off;
            boolSwitchState = false;
            break;
        case ON:
            id = R.drawable.bulb_switch_on;
            boolSwitchState = true;
            break;
        case CHANGING:
            id = R.drawable.bulb_switch_changing;
            break;
        default:
            throw new IllegalArgumentException();
        }
        
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(id);
        button = bitmapDrawable.getBitmap();
        scalledBitmap = null;
        this.invalidate();
    }

    private void createPaints() {
        paint.setColor(0xFF00AEFF);
        paint.setAntiAlias(true);
        paint.setStyle(Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(progresWidth);
        
        gradientBackgroundPaint.setColor(Color.argb(192, 255, 255, 255));
        gradientBackgroundPaint.setAntiAlias(true);
        gradientBackgroundPaint.setStyle(Style.FILL);
        

        shadowPaint.setColor(Color.argb(128,20,20,20));
        shadowPaint.setAntiAlias(true);
        shadowPaint.setStyle(Style.FILL);
        
    }

    public void initialize() {

        // this.setImageResource(R.drawable.bulb_switch_on);
        createPaints();

        if(!isInEditMode()){
            updateSwitchState(SwitchState.OFF);
            BitmapDrawable tmp =(BitmapDrawable) getResources().getDrawable(R.drawable.dragger);
            draggerBitmap = Bitmap.createScaledBitmap(tmp.getBitmap(),(int)(radiusOfKnob*2),(int)(radiusOfKnob*2),false);
        }
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean retValue;
                if(!dragKnob && isButtonColision((int)event.getX(0),(int)event.getY(0))){
                    Log.d(TAG,"Button colision");
                    retValue= onTouchButton(v, event);
                }else{
                    Log.d(TAG,"Else colision");
                    updateSwitchState(boolSwitchState?SwitchState.ON:SwitchState.OFF);
                    retValue= onTouchKnob(v, event);
                }
                return retValue;
                
            }
        });
    }

    private boolean onTouchButton(View v,MotionEvent event){
        int actionCode = event.getActionMasked();
        if (actionCode == MotionEvent.ACTION_DOWN) {
            updateSwitchState(SwitchState.CHANGING);
        }else if(actionCode == MotionEvent.ACTION_UP){
            updateSwitchState(!boolSwitchState?SwitchState.ON:SwitchState.OFF);
            notifySwitchChangedListener(boolSwitchState);
        }else{
            updateSwitchState(boolSwitchState?SwitchState.ON:SwitchState.OFF);
        }
        return true;
    }
    private boolean dragKnob = false;
    private boolean onTouchKnob(View v, MotionEvent event) {
        int actionCode = event.getActionMasked();

        if (actionCode == MotionEvent.ACTION_UP) {
            if (!(Math.abs(knobValue - knobValue_old) < MINIMAL_STEP)) {
                notifyKnobChangedListener(knobValue);
                knobValue_old = knobValue;
                dragKnob = false;
            }
            return true;
        }
        dragKnob = true;
        float x = event.getX(0);
        float y = event.getY(0);

        if (!ABSOLUTE_KNOB_CONTROLL) {
            if (actionCode == MotionEvent.ACTION_DOWN) {
                theta_old = getTheta(x, y);
            } else if (actionCode == MotionEvent.ACTION_MOVE) {
                invalidate();

                float theta = getTheta(x, y);
                float delta_theta = theta - theta_old;

                theta_old = theta;

                int direction = (delta_theta > 0) ? 1 : -1;
                angle += 3 * direction;

                if (angle > 360.0f) {
                    angle -= 360;
                } else if (angle < 0.0f) {
                    angle += 360;
                }

                if (angle >= 180.0f && angle < 180.0f + BOTTOM_ANGLE_LOCK) {
                    angle = 180.0f + BOTTOM_ANGLE_LOCK;
                    direction = 0;
                } else if (angle <= 180.0f && angle > 180.0f - BOTTOM_ANGLE_LOCK) {
                    angle = 180.0f - BOTTOM_ANGLE_LOCK;
                    direction = 0;
                }
            }

            float leftSize = (360.0f - (180.0f + BOTTOM_ANGLE_LOCK)) / (360.0f - 2 * BOTTOM_ANGLE_LOCK);

            knobValue = angle >= 180.0f + BOTTOM_ANGLE_LOCK ? leftSize * ((angle - (180.0f + BOTTOM_ANGLE_LOCK)) / (360.0f - (180.0f + BOTTOM_ANGLE_LOCK)))
                    : leftSize + (1.0f - leftSize) * (angle / (180.0f - BOTTOM_ANGLE_LOCK));
        } else {
            if (actionCode == MotionEvent.ACTION_MOVE || actionCode == MotionEvent.ACTION_DOWN) {
                invalidate();

                angle = getTheta(x, y) + 90.0f;

                if (angle >= 180.0f && angle < 180.0f + BOTTOM_ANGLE_LOCK) {
                    angle = 180.0f + BOTTOM_ANGLE_LOCK;
                } else if (angle <= 180.0f && angle > 180.0f - BOTTOM_ANGLE_LOCK) {
                    angle = 180.0f - BOTTOM_ANGLE_LOCK;
                }

                float leftSize = (449.0f - (180.0f + BOTTOM_ANGLE_LOCK)) / (360.0f - 2 * BOTTOM_ANGLE_LOCK);

                knobValue = angle >= 180.0f + BOTTOM_ANGLE_LOCK ? leftSize * ((angle - (180.0f + BOTTOM_ANGLE_LOCK)) / (449.0f - (180.0f + BOTTOM_ANGLE_LOCK)))
                        : leftSize + (1.0f - leftSize) * (angle - 90.0f) / ((180.0f - BOTTOM_ANGLE_LOCK) - 90);
            }
        }

        return true;

    }

    private void notifyKnobChangedListener(double arg) {
        if (null != knobListener)
            knobListener.onKnobChanged(arg);
    }
    private void notifySwitchChangedListener(boolean arg) {
        if (null != knobListener)
            knobListener.onSwitchChanged(arg);
    }
    
    private Bitmap getScalledBitmap() {
        if (button == null)
            return null;
        int sizeOfBitmap = size - 2 * margin - progresWidth;
        
        if (scalledBitmap == null || (scalledBitmap.getWidth() != sizeOfBitmap || scalledBitmap.getHeight() != sizeOfBitmap)) {
            Log.d(TAG, "Create bitmap with size:" + sizeOfBitmap);
            scalledBitmap = Bitmap.createScaledBitmap(button, sizeOfBitmap, sizeOfBitmap, true);
            radiusOfButton = (int) (sizeOfBitmap / 2);
            centerOfButton.set(x+margin+radiusOfButton+progresWidth/2, y+margin+radiusOfButton+progresWidth/2);
            
            int hOfShadow = radiusOfButton/5;
            shadowRect.set(centerOfButton.x-radiusOfButton, centerOfButton.y+radiusOfButton-1.5f*hOfShadow, centerOfButton.x+radiusOfButton, centerOfButton.y+radiusOfButton+0.5f*hOfShadow);

        }
        return scalledBitmap;
    }

    private boolean isButtonColision(int x,int y){
        if(Math.abs(x-centerOfButton.x)<radiusOfButton && Math.abs(y-centerOfButton.y)<radiusOfButton){
            if(Math.abs(x-centerOfKnob.x)<radiusOfKnob && Math.abs(y-centerOfKnob.y)<radiusOfKnob){
                return false;
            }
            return true;
        }
        return false;
    }
    
    
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        
        size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        x = (getMeasuredWidth() - size) / 2;
        y = (getMeasuredHeight() - size) / 2;
        //BlurMaskFilter bmf = ((BlurMaskFilter)gradientBackgroundPaint.getMaskFilter());
        
        Bitmap bitmap = getScalledBitmap();
        if (bitmap != null) {
            gradientBackgroundPaint.setMaskFilter(new BlurMaskFilter(size/2-radiusOfButton, Blur.OUTER ));
            c.drawCircle(centerOfButton.x, centerOfButton.y, radiusOfButton, gradientBackgroundPaint);
            shadowPaint.setMaskFilter(new BlurMaskFilter(shadowRect.height(),Blur.NORMAL));
            c.drawOval(shadowRect,shadowPaint);
            c.drawBitmap(bitmap, centerOfButton.x-radiusOfButton,centerOfButton.y-radiusOfButton, null);
            
        }
        rectF.set(x + margin, y + margin, x + size - margin, y + size - margin);
        c.drawArc(rectF, 90 + BOTTOM_ANGLE_LOCK, (angle - 180 - BOTTOM_ANGLE_LOCK + 360) % 360, false, paint);

        float r = (size - 2 * margin) / 2;

        centerOfKnob.set((int) (x + margin + r * (1 + Math.sin(Math.toRadians(angle)))), (int) (y + margin + r * (1 - Math.cos(Math.toRadians(angle)))));
        if(!isInEditMode()){
            c.drawBitmap(draggerBitmap, centerOfKnob.x-radiusOfKnob,centerOfKnob.y-radiusOfKnob,null);
        }
    }
}
