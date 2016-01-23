/*
 * Copyright (C) 2016, SharkAndroid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.ainol.toolkit;

import android.content.*;
import android.graphics.*;
import android.hardware.*;
import android.util.*;
import android.view.*;

public class SensorHost extends View implements View.OnTouchListener {
    final String TAG = "SensorHost";
    float vX = 0.0F;
    float vY = 0.0F;
    float vZ = 0.0F;
    Display mDisplay = null;
    Paint mPaint = null;
    RectF mOval = null;

    public SensorHost(Context ctx, AttributeSet attrs) {
        this(ctx, attrs, 0);
    }

    public SensorHost(Context ctx, AttributeSet attrs, int param) {
        super(ctx, attrs, param);

        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);

        mDisplay = wm.getDefaultDisplay();
        mPaint = new Paint();
        mOval = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int r = Math.min(w, h) * 4 / 5;
        setMeasuredDimension(r, r);
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        int w = getWidth();
        int h = getHeight();

        c.translate(w / 2, h / 2);

        mPaint.setAntiAlias(true);
        mPaint.setColor(-1);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAlpha(255);

        int r = Math.min(w, h);
        mOval.left = -r / 2;
        mOval.top = -r / 2;
        mOval.right = r / 2;
        mOval.bottom = r / 2;

        c.drawArc(mOval, 0, 360, false, mPaint);
        c.drawLine(-r / 2, 0.0F, r / 2, 0.0F, mPaint);
        c.drawLine(0.0F, -r / 2, 0.0F, r / 2, mPaint);

        int k = mDisplay.getRotation() * 90;
        float f2;
        if (k == 90) {
            f2 = vX;
            vX = (-vY);
            vY = f2;
        }
        if (k == 180) {
            vX = (-vX);
            vY = (-vY);
        }
        if (k == 270) {
            f2 = vX;
            vX = vY;
            vY = (-f2);
        }

        mPaint.setStyle(Paint.Style.FILL);
        c.drawCircle(-w / 2 * vX / 10.0F, h / 2 * vY / 10.0F, 20.0F - vZ,
                mPaint);
    }

    public void onSensorChanged(SensorEvent se) {
        if (se.values.length == 3) {
            vX = se.values[0];
            vY = se.values[1];
            vZ = se.values[2];
            invalidate();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        return false;
    }
}