/*
 * Copyright (C) 2015, SharkAndroid
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

public class SensorActivity extends Activity implements SensorEventListener {
	final String TAG = "SensorActivity";
	private Handler mHandler;
	private TextView mTextView;
	public SensorManager sm = null;
	public SensorHost mSensorHost;
	
	public void findViews() {
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

	public void onSensorChanged(SensorEvent se) {
		if((se != null) && (se.values.length == 3)) {
			if(mTextView != null) {
				String txt = String.format("X: %.3f, Y: %.3f, Z: %.3f",
						se.values[0], se.values[1], se.values[2]);
				mTextView.setText(txt);
				mTextView.setTextColor(getResources().getColor(R.color.purple));
			}
			if(mSensorHost != null)
				mSensorHost.onSensorChanged(se);
		}
	}
}