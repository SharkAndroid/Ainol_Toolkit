/*
 * Copyright (C) 2015 SharkAndroid
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
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.actions.tools.R;
import com.actions.tools.SensorHost;

public class SensorActivity extends Activity implements SensorEventListener {
	
	public static final String TAG = "SensorActivity";
	private Handler mHandler;
	private SensorManager sm = null;
	private boolean mCalibMode = false;

	private TextView mTextView;
	private SensorHost mSensorHost;
	
	private void findViews() {
		mTextView = (TextView) findViewById(R.id.view_text);
		mSensorHost = (SensorHost) findViewById(R.id.sh);
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.sensor_calib);
		findViews();
		
		mHandler = new Handler();
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	}

	protected void onDestroy() {
		super.onDestroy();
		mHandler = null;
		sm = null;
	}

	protected void onPause() {
		super.onPause();
		sm.unregisterListener(this);
	}

	protected void onResume() {
		super.onResume();
		sm.registerListener(this,
				sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void onAccuracyChanged(Sensor paramSensor, int paramInt) {
	}

	public void onSensorChanged(SensorEvent e) {
		if ((e != null) && (e.values.length == 3)) {
			if(mCalibMode)
				return;

			if (mTextView != null) {
				String txt = String.format("X: %.3f, Y: %.3f, Z: %.3f",
						e.values[0], e.values[1], e.values[2]);
				mTextView.setText(txt);
				mTextView.setTextColor(getResources().getColor(R.color.purple));
			}
			if (mSensorHost != null)
				mSensorHost.onSensorChanged(e);
		}
	}
}