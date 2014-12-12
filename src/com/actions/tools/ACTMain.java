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

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Process;
import java.lang.String;

import com.actions.tools.R;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class ACTMain extends Activity {
	
	public static final String TAG = "ACTMain";
    final String SETTINGS_KEY = "settings";
    private ToggleButton cpuboost;
    private ToggleButton gpuboost;
    private ToggleButton freezes;
    private TextView freezes_text;
    
    public static class HelpFragment extends DialogFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
            View v = inflater.inflate(R.layout.changelog, null);
            TextView tv = (TextView) v.findViewById(R.id.changelog);
            tv.setText(R.string.changelog_text);
            return v;
        }
    }

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        
        cpuboost = (ToggleButton) findViewById(R.id.cpuboost_btn);
		gpuboost = (ToggleButton) findViewById(R.id.gpuboost_btn);
        freezes = (ToggleButton) findViewById(R.id.freezes_btn);
		freezes_text = (TextView) findViewById(R.id.freezes);
		
        SharedPreferences sharedPrefs = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE);
        cpuboost.setChecked(sharedPrefs.getBoolean("cpuboost_state", false));
        gpuboost.setChecked(sharedPrefs.getBoolean("gpuboost_state", false));
        freezes.setChecked(sharedPrefs.getBoolean("freezes_state", false));
        
        if (!RootTools.isAccessGiven()) { 
        	showWarningDialog(getString(R.string.no_root), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
        
        String bplatform = getProp("ro.board.platform");
        if(bplatform == null || !bplatform.trim().equals("ATM702X")) {
            showWarningDialog(getString(R.string.unsupport_device, bplatform), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
        
        String sharkandroid = getProp("ro.sa.version");
        if(sharkandroid == null) {
		    freezes_text.setVisibility(View.GONE);
        	freezes.setVisibility(View.GONE);
        }
        
        final Button calib_button = (Button)findViewById(R.id.calib_button);
        calib_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	Intent gsensor_calib = new Intent(ACTMain.this, SensorActivity.class);
                startActivity(gsensor_calib);
            }
            });
        
        cpuboost.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
                if (cpuboost.isChecked()) {
                	ExecuteRoot("echo '1'>/sys/devices/system/cpu/cpufreq/user/boost");
                    ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/user/boost");
                    Toast.makeText(ACTMain.this, getString(R.string.cpuboost_unlocked), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Warning: Maximum CPU freq unlocked!");
                    SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE).edit();
                    editor.putBoolean("cpuboost_state", true);
                    editor.commit();
                } else {
                	ExecuteRoot("chmod 666 /sys/devices/system/cpu/cpufreq/user/boost");
                    ExecuteRoot("echo '0'>/sys/devices/system/cpu/cpufreq/user/boost");
                    ExecuteRoot("chmod 777 /sys/devices/system/cpu/cpufreq/user/boost");
                    Toast.makeText(ACTMain.this, getString(R.string.cpuboost_locked), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Warning: Maximum CPU freq locked!");
                    SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE).edit();
                    editor.putBoolean("cpuboost_state", false);
                    editor.commit();
                }
            }
        });
		
        gpuboost.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
                if (gpuboost.isChecked()) {
                	ExecuteRoot("echo '2'>/sys/devices/system/cpu/cpufreq/user/boost");
	                ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/user/boost");
	                Toast.makeText(ACTMain.this, getString(R.string.gpuboost_unlocked), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Warning: Maximum GPU freq unlocked!");
                    SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE).edit();
                    editor.putBoolean("gpuboost_state", true);
                    editor.commit();
                } else {
                	ExecuteRoot("chmod 666 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
	                ExecuteRoot("echo '0'>/sys/devices/system/cpu/cpufreq/gpufreq/policy");
	                ExecuteRoot("chmod 777 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
	                Toast.makeText(ACTMain.this, getString(R.string.gpuboost_locked), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Warning: Maximum GPU freq locked!");
                    SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE).edit();
                    editor.putBoolean("gpuboost_state", false);
                    editor.commit();
                }
            }
        }); 
        
        freezes.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
                if (freezes.isChecked()) {
                	ExecuteRoot("echo '1'>/sys/devices/system/cpu/cpufreq/interactive/boost");
	                ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/interactive/boost");
	                Toast.makeText(ACTMain.this, getString(R.string.function_enabled), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Warning: Freeze function enabled!");
                    SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE).edit();
                    editor.putBoolean("freezes_state", true);
                    editor.commit();
                } else {
                	ExecuteRoot("chmod 666 /sys/devices/system/cpu/cpufreq/interactive/boost");
	                ExecuteRoot("echo '0'>/sys/devices/system/cpu/cpufreq/interactive/boost");
	                ExecuteRoot("chmod 777 /sys/devices/system/cpu/cpufreq/interactive/boost");
	                Toast.makeText(ACTMain.this, getString(R.string.function_disabled), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Warning: Freeze function disabled!");
                    SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE).edit();
                    editor.putBoolean("freezes_state", false);
                    editor.commit();
                }
            }
        }); 
	}

	private String getProp(String key){
        try {
            Process process = Runtime.getRuntime().exec(String.format("getprop %s",key));
            String value = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
            process.destroy();
            return value;
        } catch (IOException e) {
            Log.e("getProp exception",e.toString(),e);
            return null;
        }
    }	
	
	private AlertDialog showWarningDialog(String text,DialogInterface.OnClickListener onClickListener){
        return new AlertDialog.Builder(this)
                .setMessage(text)
                .setNeutralButton(R.string.exit,onClickListener)
                .setCancelable(false)
                .show();
    }

	@Override
    public void onDestroy() {
    	moveTaskToBack(true);
    	
    	System.runFinalizersOnExit(true);
    	finishAffinity();
    	
    	super.onDestroy();
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_app:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("http://github.com/SharkAndroid/update_manager/Actions_Tools/raw/master/Actions_Tools.apk"));
                startActivity(intent);
                return true;
            case R.id.source_code:
                Intent intent2 = new Intent();
                intent2.setAction(Intent.ACTION_VIEW);
                intent2.addCategory(Intent.CATEGORY_BROWSABLE);
                intent2.setData(Uri.parse("http://github.com/SharkAndroid/Actions_Tools"));
                startActivity(intent2);
                return true;
            case R.id.changelog:
                DialogFragment df = new HelpFragment();
                df.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                df.show(getFragmentManager(), "changelog");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	public void ExecuteRoot(String commandString) {
        CommandCapture command = new CommandCapture(0, commandString);
        try { 
           RootTools.getShell(true).add(command); 
        }
        catch (Exception e) {
        	e.printStackTrace();
            Toast.makeText(this ,getString(R.string.no_root), Toast.LENGTH_SHORT).show();
        }
    }
}