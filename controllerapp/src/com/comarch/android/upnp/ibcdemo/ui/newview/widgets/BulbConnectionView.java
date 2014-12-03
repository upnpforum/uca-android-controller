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
import android.R.color;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.busevent.ConnectionStateChangedEvent.ConnectionState;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.LocalConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionStateChangedEvent;


public class BulbConnectionView extends LinearLayout{

    private ImageView mBulbIcon;
    private ImageView mDeviceIcon;
    
    private ToggleButton mLocalButton;
    private ToggleButton mCloudButton;
    
    private boolean localConnection = false;
    private boolean cloudConnection = false;
    private LinearLayout buttonsLayout;
    
    private boolean deviceLocalConnection = false;
    private boolean deviceCloudConnection = false;
    
    private Paint linePaint = new Paint();
    private Paint lineGlowPaint = new Paint();
    private Paint backgroundLinePaint = new Paint();
    
    public BulbConnectionView(Context context) {
        super(context);
    }
    
    public BulbConnectionView(Context context,AttributeSet attr){
        super(context, attr);
        initialize(context);
    }
    
    public BulbConnectionView(Context context,AttributeSet attr,int defStyle){
        super(context, attr,defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        this.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.bulb_connection_view, this, true);
        
        mDeviceIcon = (ImageView) getChildAt(0);
        buttonsLayout = (LinearLayout)getChildAt(1);
        mBulbIcon = (ImageView) getChildAt(2);
        mLocalButton =(ToggleButton) buttonsLayout.getChildAt(0);
        mCloudButton =(ToggleButton) buttonsLayout.getChildAt(1); 
        updateLocalButton();
        updateCloudButton();
        setBackgroundColor(color.transparent);
        
        
        linePaint.setColor(0xFFFFFFFF);
        linePaint.setAntiAlias(true);
        linePaint.setDither(true);
        linePaint.setStrokeWidth(2.0f);
        linePaint.setStyle(Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        
        lineGlowPaint.set(linePaint);
        lineGlowPaint.setMaskFilter(new BlurMaskFilter(4,Blur.OUTER));
        
        backgroundLinePaint.set(linePaint);
        backgroundLinePaint.setColor(0xFF86cd82);
        backgroundLinePaint.setStrokeWidth(3.0f);
        backgroundLinePaint.setMaskFilter(new BlurMaskFilter(2,Blur.NORMAL));

    }

    private void updateLocalButton(){
        if(localConnection){
            mLocalButton.setBackgroundResource(R.drawable.button_local_enabled);
        }else{
            mLocalButton.setBackgroundResource(R.drawable.button_local_disabled);
        }
        mLocalButton.setText("");
    }
    
    private void updateCloudButton(){
        if(cloudConnection){
            mCloudButton.setBackgroundResource(R.drawable.button_cloud_enabled);
        }else{
            mCloudButton.setBackgroundResource(R.drawable.button_cloud_disabled);
        }
        mLocalButton.setText("");
    }
    public void onEventMainThread(LocalConnectionStateChangedEvent ev) {
        localConnection = (ev.getState()==ConnectionState.CONNECTED);
        updateLocalButton();
    }
    
    public void onEventMainThread(XmppConnectionStateChangedEvent ev) {
        cloudConnection = (ev.getState()==ConnectionState.CONNECTED);
        updateCloudButton();
    }

    public void setDeviceLocalConnectionState(boolean b) {
        deviceLocalConnection = b;
        invalidate();
    }

    public void setDeviceCloudConnectionState(boolean b) {
        deviceCloudConnection = b;
        invalidate();
    }
    
    private void drawConnection(float x1,float y1,float x2,float y2,Canvas canvas,Paint paint){
        canvas.drawLine(x1, y1,  x2, y2, paint);
    }
    

    private void paintElementsWithPaint(Canvas canvas,Paint paint,boolean force){
        drawCircleAroundView(canvas, mBulbIcon,paint);
        drawCircleAroundView(canvas, mDeviceIcon,paint);
        
        float x1,y1,x2,y2;
        if(localConnection || force){
            x1 = (float) (mDeviceIcon.getX()+mDeviceIcon.getWidth()/2+phetagoreanGetHypotenuse(mDeviceIcon.getWidth()/2,mDeviceIcon.getHeight()/2)*Math.sqrt(2)/2);
            y1 = (float) (mDeviceIcon.getY()+mDeviceIcon.getHeight()/2-phetagoreanGetHypotenuse(mDeviceIcon.getWidth()/2,mDeviceIcon.getHeight()/2)*Math.sqrt(2)/2);

            
            x2 = buttonsLayout.getX()+mLocalButton.getX()+mLocalButton.getWidth()/2;
            y2 = buttonsLayout.getY()+mLocalButton.getY()+mLocalButton.getHeight()/2;
            drawConnection(x1, y1, x2, y2, canvas,paint);
        }
        if(cloudConnection || force){
            x1 = (float) (mDeviceIcon.getX()+mDeviceIcon.getWidth()/2+phetagoreanGetHypotenuse(mDeviceIcon.getWidth()/2,mDeviceIcon.getHeight()/2)*Math.sqrt(2)/2);
            y1 = (float) (mDeviceIcon.getY()+mDeviceIcon.getHeight()/2+phetagoreanGetHypotenuse(mDeviceIcon.getWidth()/2,mDeviceIcon.getHeight()/2)*Math.sqrt(2)/2);

            x2 = buttonsLayout.getX()+mCloudButton.getX()+mCloudButton.getWidth()/2;
            y2 = buttonsLayout.getY()+mCloudButton.getY()+mCloudButton.getHeight()/2;
            drawConnection(x1, y1, x2, y2, canvas,paint);
        }
        if(deviceLocalConnection || force){
            x1 = (float) (mBulbIcon.getX()+mBulbIcon.getWidth()/2-phetagoreanGetHypotenuse(mBulbIcon.getWidth()/2,mBulbIcon.getHeight()/2)*Math.sqrt(2)/2);
            y1 = (float) (mBulbIcon.getY()+mBulbIcon.getHeight()/2-phetagoreanGetHypotenuse(mBulbIcon.getWidth()/2,mBulbIcon.getHeight()/2)*Math.sqrt(2)/2);

            
            x2 = buttonsLayout.getX()+mLocalButton.getX()+mLocalButton.getWidth()/2;
            y2 = buttonsLayout.getY()+mLocalButton.getY()+mLocalButton.getHeight()/2;
            drawConnection(x1, y1, x2, y2, canvas,paint);
        }
        if(deviceCloudConnection || force){
            x1 = (float) (mBulbIcon.getX()+mBulbIcon.getWidth()/2-phetagoreanGetHypotenuse(mBulbIcon.getWidth()/2,mBulbIcon.getHeight()/2)*Math.sqrt(2)/2);
            y1 = (float) (mBulbIcon.getY()+mBulbIcon.getHeight()/2+phetagoreanGetHypotenuse(mBulbIcon.getWidth()/2,mBulbIcon.getHeight()/2)*Math.sqrt(2)/2);

            
            x2 = buttonsLayout.getX()+mCloudButton.getX()+mCloudButton.getWidth()/2;
            y2 = buttonsLayout.getY()+mCloudButton.getY()+mCloudButton.getHeight()/2;
            drawConnection(x1, y1, x2, y2, canvas,paint);
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        paintElementsWithPaint(canvas,backgroundLinePaint,true);
        paintElementsWithPaint(canvas,lineGlowPaint,false);
        paintElementsWithPaint(canvas,linePaint,false);
        super.onDraw(canvas);
    }
    
    
    private void drawCircleAroundView(Canvas canvas,View view,Paint paint){
        float centerX = view.getX()+view.getWidth()/2;
        float centerY = view.getY()+view.getHeight()/2;
        float radius = (float) phetagoreanGetHypotenuse(view.getWidth()/2, view.getHeight()/2);
        canvas.drawCircle(centerX, centerY,radius, paint);

        
    }
    double phetagoreanGetHypotenuse(double a,double b){
        return Math.sqrt(Math.pow(a, 2)+Math.pow(b,2));
    }
}
