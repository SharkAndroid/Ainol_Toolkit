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
import android.text.TextUtils;
import android.widget.*;

import com.ainol.toolkit.R;
import com.ainol.toolkit.ATMain;
import com.ainol.toolkit.SensorHost;

public class SensorActivity extends Activity implements SensorEventListener {
    final String TAG = "SensorActivity";
    private Handler mHandler;
    private TextView mTextCoord;
    private TextView mTextGSensor;
    public SensorManager sm = null;
    public SensorHost mSensorHost;
    
    public void findViews() {
        mTextCoord = (TextView) findViewById(R.id.text_coord);
        mTextGSensor = (TextView) findViewById(R.id.text_gsensor);
        mSensorHost = (SensorHost) findViewById(R.id.sh);
    }

    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.sensor_calib);

        findViews();
        findGSensor();

        mHandler = new Handler();
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        mHandler = null;
        sm = null;
    }

    @Override
    protected void onPause() {
        super.onPause();

        sm.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sm.registerListener(this,
                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor paramSensor, int paramInt) {
        // TODO Auto-generated method stub
    }

    public void findGSensor() {
        /* Check if CyanogenMod,AOKP,AOSPA installed on device. */
        String cm = ATMain.getProp("ro.cm.version");
        String aokp = ATMain.getProp("ro.aokp.version");
        String aospa = ATMain.getProp("ro.modversion");

        /* Device name */
        String model = null;
        if (!TextUtils.isEmpty(cm) || !TextUtils.isEmpty(aokp) || !TextUtils.isEmpty(aospa)) {
            model = ATMain.getProp("ro.real_device");
        } else {
            model = ATMain.getProp("ro.product.model");
        }
        
        /* GSensor name */
        String gstxt = null;
        if (model.trim().equals("Novo 10 Hero II") || model.trim().equals("Novo 7 Venus")) {
            gstxt = "mc3210";
            mTextGSensor.setText(gstxt);
        } else if (model.trim().equals("Novo 10 Captain")) {
            gstxt = "mma8452";
            mTextGSensor.setText(gstxt);
        } else {
            gstxt = "Unknow";
            mTextGSensor.setText(gstxt);
        }
    }
    
    public void onSensorChanged(SensorEvent se) {
        if ((se != null) && (se.values.length == 3)) {
            if (mTextCoord != null) {
                String txt = String.format("X: %.3f, Y: %.3f, Z: %.3f",
                        se.values[0], se.values[1], se.values[2]);
                mTextCoord.setText(txt);
            }
            if (mSensorHost != null) mSensorHost.onSensorChanged(se);
        }
    }
}