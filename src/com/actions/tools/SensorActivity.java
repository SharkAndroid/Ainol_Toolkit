/*
 * Copyright (C) 2014 SharkAndroid
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

package com.actions.tools;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actions.tools.SensorControl;
import com.actions.tools.SensorHost;

import com.actions.tools.R;

public class SensorActivity extends Activity implements SensorEventListener {
	
    private final String TAG = "SensorActivity";
    private Button mRunCalib;
    private Button mResetCalib;
    private boolean mCalibMode = false;
    private Handler mHandler;
    private SensorHost mSensorHost;
    private TextView mViewText;
    private SensorControl sc = null;
    private SensorManager sm = null;
    
    private View.OnClickListener mRunCalibListener = new View.OnClickListener() {
        public void onClick(View paramAnonymousView) {
            SensorActivity.this.mRunCalib.setClickable(false);
            SensorActivity.CalibAccess(SensorActivity.this, true);
            SensorActivity.this.sc.resetCalib();
            SensorActivity.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    SensorActivity.this.mRunCalib.setClickable(true);
                    SensorActivity.CalibAccess(SensorActivity.this, false);
                    SensorActivity.this.sc.runCalib();
                    String str1 = SensorActivity.this.sc.getCalibValue();
                    Log.i(TAG, "Calib: " + str1);
                    String str2 = str1 + "\n";
                    SensorActivity.this.sc.writeCalibFile(str2);
                    Toast.makeText(SensorActivity.this, R.string.info_calib, 1).show();
                }
            }
            , 1000L);
        }
    };

    private View.OnClickListener mResetCalibListener = new View.OnClickListener() {
        public void onClick(View paramAnonymousView) {
            SensorActivity.this.mResetCalib.setClickable(false);
            SensorActivity.CalibAccess(SensorActivity.this, true);
            SensorActivity.this.sc.resetCalib();
            SensorActivity.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    SensorActivity.this.mResetCalib.setClickable(true);
                    SensorActivity.CalibAccess(SensorActivity.this, false);
                    String str1 = SensorActivity.this.sc.getCalibValue();
                    Log.i(TAG, "Calib: " + str1);
                    String str2 = str1 + "\n";
                    SensorActivity.this.sc.writeCalibFile(str2);
                    Toast.makeText(SensorActivity.this, R.string.info_reset, 1).show();
                }
            }
            , 1000L);
        }
    };

    private void findViews() {
        mViewText = ((TextView) findViewById(R.id.view_text));
        mRunCalib = ((Button) findViewById(R.id.run_button));
        mResetCalib = ((Button) findViewById(R.id.reset_button));
        mSensorHost = ((SensorHost) findViewById(R.id.sensor_host));
    }

	protected static void CalibAccess(SensorActivity sensorActivity, boolean b) {
		// Auto generate method
	}

	private void setListensers() {
        mRunCalib.setOnClickListener(mRunCalibListener);
        mResetCalib.setOnClickListener(mResetCalibListener);
    }

    public void onAccuracyChanged(Sensor paramSensor, int paramInt) {
    	// Auto generate method
    }

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.sensor_calib);
        
        findViews();
        mHandler = new Handler();
        sm = ((SensorManager)getSystemService("sensor"));
        sc = new SensorControl(this);
        setListensers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        mHandler = null;
        sm = null;
        sc = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        sm.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        sm.registerListener(this, sm.getDefaultSensor(1), 3);
    }

    public void onSensorChanged(SensorEvent paramSensorEvent) {
        if ((paramSensorEvent == null) || (paramSensorEvent.values.length != 3) || (mCalibMode));
        do {
            if (mViewText != null) {
                Object[] arrayOfObject = new Object[3];
                arrayOfObject[0] = Float.valueOf(paramSensorEvent.values[0]);
                arrayOfObject[1] = Float.valueOf(paramSensorEvent.values[1]);
                arrayOfObject[2] = Float.valueOf(paramSensorEvent.values[2]);
                String str = String.format("X: %.3f, Y: %.3f, Z: %.3f", arrayOfObject);
                mViewText.setText(str);
                mViewText.setTextColor(getResources().getColor(R.color.purple));
            }
        }

        while (mSensorHost == null);
        mSensorHost.onSensorChanged(paramSensorEvent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.submain, menu);
        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back_actmain:
                Intent act_main = new Intent(SensorActivity.this, ACTMain.class);
                startActivity(act_main);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}