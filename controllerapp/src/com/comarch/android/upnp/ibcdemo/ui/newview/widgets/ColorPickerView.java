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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerView extends View implements View.OnTouchListener{

	public interface ColorPickerViewListener{
		public void colorChanged(int color);
	}
	@SuppressWarnings("unused")
	private final String TAG = getClass().getSimpleName();
	
	private final static float MARGIN = 0.4f;
	private int[] mColors;
	//private float mCurrentColorPos;
	private Paint mMarkerPaint;
	private Handler mHandler;
	private float mHue;
	private ColorPickerViewListener mListener;
	
    public ColorPickerView(Context context) {
        super(context);
        initialize();
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

	private void initialize() {
		mColors = new int[] {
	            0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
	            0xFFFFFF00, 0xFFFF0000
	        };
		mHue = 180.0f;
		mMarkerPaint= new Paint(Paint.ANTI_ALIAS_FLAG);
		mMarkerPaint.setColor(0xFFFFFFFF);
		mMarkerPaint.setStyle(Paint.Style.STROKE);
		mMarkerPaint.setStrokeWidth(1.0f);
		this.setOnTouchListener(this);
		
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				if(mListener!=null){
					int color = Color.HSVToColor(new float[]{mHue,1.0f,1.0f});
					mListener.colorChanged(color);
				}
			}
		};
	}
	
	public void setColorPickerListener(ColorPickerViewListener listener) {
		mListener = listener;
		
	}
	private Paint getPaint(int w,int h){
		float[] pos = new float[mColors.length];
		for(int i=0;i<pos.length;i++){
			pos[i] = i/(float)(pos.length-1);
		}
		Shader s = new LinearGradient(0, 0,w,h, mColors,pos,TileMode.CLAMP);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    paint.setShader(s);
	    paint.setStyle(Paint.Style.FILL);
	    return paint;
    
	}
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int marginSize = (int) (height*(MARGIN/2));
        c.drawRect(1, marginSize, width-1, height-marginSize, getPaint(width-2,height-2*marginSize));
        
        float percent = (360.f - mHue)/360f;
        int curPos = (int) ((width-2)*percent)+1;
        c.drawRect(curPos-1,marginSize-1,curPos+2,height-marginSize+1, mMarkerPaint);
    }
    

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_MOVE
				|| event.getAction() == MotionEvent.ACTION_DOWN
				|| event.getAction() == MotionEvent.ACTION_UP) {

			float x = event.getX();
			if (x < 0.f)
				x = 0.f;
			if (x > getMeasuredWidth()) {
				x = getMeasuredWidth() - 0.001f; // to avoid jumping
															// the cursor from
															// bottom to top.
			}
			
			float hue = 360.f - 360.f / getMeasuredWidth() * x;
			if (hue == 360.f)
				hue = 0.f;
			
			mHue = hue;
			
			//mCurrentColorPos = x / getMeasuredWidth();
			mHandler.removeMessages(0);
			mHandler.sendEmptyMessageDelayed(0, 50);
			invalidate();
			return true;
		}
		return false;
	}

	public void setColor(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		mHue = hsv[0];
		invalidate();
	}



}
