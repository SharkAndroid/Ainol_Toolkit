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

import android.app.*;
import android.content.*;
import android.hardware.*;
import android.os.*;
import android.widget.*;

import com.ainol.toolkit.R;
import com.ainol.toolkit.SensorHost;

import java.io.File;

public class ModulesActivity extends Activity implements SensorEventListener {
    private String TAG = "ModulesActivity";
    private Handler mHandler;
    private TextView mCModule;
    private TextView mTModule;
    private TextView mGSModule;
    private TextView mTextGSCoord;
    private SensorManager mSensorManager = null;
    public SensorHost mSensorHost;

    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.modules);

        /* Initialize layout */
        mCModule = (TextView) findViewById(R.id.cmodule);
        mTModule = (TextView) findViewById(R.id.tmodule);
        mGSModule = (TextView) findViewById(R.id.gsmodule);
        mTextGSCoord = (TextView) findViewById(R.id.text_gscoord);
        mSensorHost = (SensorHost) findViewById(R.id.sh);

        /* Get modules */
        getCModule();
        getTModule();
        getGSModule();

        mHandler = new Handler();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandler = null;
        mSensorManager = null;
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor paramSensor, int paramInt) {
        // TODO Auto-generated method stub
    }

    public void getCModule() {
        /* Camera module */
        String camera = null;
        File gc0308 = new File("/misc/modules/camera_gc0308.ko");
        File hi253 = new File("/misc/modules/camera_hi253.ko");

        if (gc0308.exists() && hi253.exists()) {
            camera = "gc0308 | hi253";
        } else if (gc0308.exists() && !hi253.exists()) {
            camera = "gc0308";
        } else if (!gc0308.exists() && hi253.exists()) {
            camera = "hi253";
        } else {
            camera = getResources().getString(R.string.unknown_module);
        }
        mCModule.setText(camera);
    }

    public void getTModule() {
        /* Touch module */
        String touch = null;
        File ft5x06 = new File("/misc/modules/ctp_ft5x06.ko");
        File gslX680 = new File("/misc/modules/ctp_gslX680.ko");

        if (ft5x06.exists()) {
            touch = "ft5x06";
        } else if (gslX680.exists()) {
            touch = "gslX680";
        } else {
            touch = getResources().getString(R.string.unknown_module);
        }
        mTModule.setText(touch);
    }

    public void getGSModule() {
        /* GSensor module */
        String gstxt = null;
        File gs3210 = new File("/misc/modules/gsensor_mc3210.ko");
        File gs8452 = new File("/misc/modules/gsensor_mma8452.ko");

        if (gs3210.exists()) {
            gstxt = "mc3210";
        } else if (gs8452.exists()) {
            gstxt = "mma8452";
        } else {
            gstxt = getResources().getString(R.string.unknown_module);
        }
        mGSModule.setText(gstxt);
    }

    public void onSensorChanged(SensorEvent se) {
        if ((se != null) && (se.values.length == 3)) {
            if (mTextGSCoord != null) {
                String txt = String.format("X: %.3f; Y: %.3f; Z: %.3f.",
                        se.values[0], se.values[1], se.values[2]);
                mTextGSCoord.setText(txt);
            }

            if (mSensorHost != null) mSensorHost.onSensorChanged(se);
        }
    }
}