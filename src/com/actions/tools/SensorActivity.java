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

import com.actions.tools.SensorHost;

import com.actions.tools.R;

public class SensorActivity extends Activity implements SensorEventListener {
	
	public static final String TAG = "SensorActivity";
    private Handler handler;
    private SensorHost sensorhost;
    private TextView viewtext;
    private SensorManager sensormanager = null;

    private void findViews() {
        viewtext = (TextView) findViewById(R.id.view_text);
        sensorhost = (SensorHost) findViewById(R.id.sensor_host);
    }

    public void onAccuracyChanged(Sensor paramSensor, int paramInt) {
    	// Auto generate method
    }

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.sensor_calib);
        findViews();
        handler = new Handler();
        sensormanager = (SensorManager) getSystemService("sensor");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler = null;
        sensormanager = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensormanager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensormanager.registerListener(this, sensormanager.getDefaultSensor(1), 3);
    }

    public void onSensorChanged(SensorEvent paramSensorEvent) {
        if ((paramSensorEvent == null) || (paramSensorEvent.values.length != 3));
        do {
            if (viewtext != null) {
                Object[] arrayOfObject = new Object[3];
                arrayOfObject[0] = Float.valueOf(paramSensorEvent.values[0]);
                arrayOfObject[1] = Float.valueOf(paramSensorEvent.values[1]);
                arrayOfObject[2] = Float.valueOf(paramSensorEvent.values[2]);
                String str = String.format("X: %.3f, Y: %.3f, Z: %.3f", arrayOfObject);
                viewtext.setText(str);
                viewtext.setTextColor(getResources().getColor(R.color.purple));
            }
        }

        while (sensorhost == null);
        sensorhost.onSensorChanged(paramSensorEvent);
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