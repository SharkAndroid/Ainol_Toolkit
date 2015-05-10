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

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.text.TextUtils;
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
    boolean cpuboost_state;
    boolean gpuboost_state;
    boolean freezes_state;
    
    public static class ChangelogFragment extends DialogFragment {
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
        if (bplatform == null || !bplatform.trim().equals("ATM702X")) {
            showWarningDialog(getString(R.string.unsupport_device, bplatform), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
    	
        final Button calib_button = (Button)findViewById(R.id.sensor_button);
        calib_button.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
            	Intent sb = new Intent(ACTMain.this, SensorActivity.class);
                startActivity(sb);
            }
        });
        
        cpuboost.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
                if (cpuboost.isChecked()) {
                	ExecuteRoot("echo '1' > /sys/devices/system/cpu/cpufreq/user/boost");
                    ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/user/boost");
                    Toast.makeText(ACTMain.this, getString(R.string.cpuboost_unlocked), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Warning: Maximum CPU freq unlocked!");
                    cpuboost_state = true;
                } else {
                	ExecuteRoot("chmod 666 /sys/devices/system/cpu/cpufreq/user/boost");
                    ExecuteRoot("echo '0' > /sys/devices/system/cpu/cpufreq/user/boost");
                    ExecuteRoot("chmod 777 /sys/devices/system/cpu/cpufreq/user/boost");
                    Toast.makeText(ACTMain.this, getString(R.string.cpuboost_locked), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Warning: Maximum CPU freq locked!");
                    cpuboost_state = false;
                }
                SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE).edit();
                editor.putBoolean("cpuboost_state", cpuboost_state);
                editor.commit();
            }
        });
		
        gpuboost.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
                if (gpuboost.isChecked()) {
                	ExecuteRoot("echo '2' > /sys/devices/system/cpu/cpufreq/user/boost");
	                ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/user/boost");
	                Toast.makeText(ACTMain.this, getString(R.string.gpuboost_unlocked), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Warning: Maximum GPU freq unlocked!");
                    gpuboost_state = true;
                } else {
                	ExecuteRoot("chmod 666 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
	                ExecuteRoot("echo '0' > /sys/devices/system/cpu/cpufreq/gpufreq/policy");
	                ExecuteRoot("chmod 777 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
	                Toast.makeText(ACTMain.this, getString(R.string.gpuboost_locked), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Warning: Maximum GPU freq locked!");
                    gpuboost_state = false;
                }
                SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE).edit();
                editor.putBoolean("gpuboost_state", gpuboost_state);
                editor.commit();
            }
        }); 
        
        freezes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (freezes.isChecked()) {
		        	ExecuteRoot("echo '1' > /sys/devices/system/cpu/cpufreq/interactive/boost");
		            ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/interactive/boost");
		            Toast.makeText(ACTMain.this, getString(R.string.function_enabled), Toast.LENGTH_SHORT).show();
		            Log.d(TAG, "Warning: Freeze function enabled!");
		            freezes_state = true;
		        } else {
		        	ExecuteRoot("chmod 666 /sys/devices/system/cpu/cpufreq/interactive/boost");
		            ExecuteRoot("echo '0' > /sys/devices/system/cpu/cpufreq/interactive/boost");
		            ExecuteRoot("chmod 777 /sys/devices/system/cpu/cpufreq/interactive/boost");
		            Toast.makeText(ACTMain.this, getString(R.string.function_disabled), Toast.LENGTH_SHORT).show();
		            Log.d(TAG, "Warning: Freeze function disabled!");
		            freezes_state = false;
		        }
		        SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE).edit();
		        editor.putBoolean("freezes_state", freezes_state);
		        editor.commit();
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
            Log.d("getProp exception",e.toString(),e);
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
                intent.setData(Uri.parse("http://github.com/SharkAndroid/update_manager/blob/master/Actions_Tools/Actions_Tools.apk?raw=true"));
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
                DialogFragment df = new ChangelogFragment();
                df.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                df.show(getFragmentManager(), "changelog");
                return true;
            case R.id.about:
            	About();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void About() {
    	String platform = "gs702a";
    	String cpu = "Actions ATM7029";
    	String gpu = "Vivante GC1000+MP";
    	String av = getProp("ro.build.version.release");
    	String bd = getProp("ro.build.date");
    	String cm = getProp("ro.cm.version");
    	String pm = null;
    	if(!TextUtils.isEmpty(cm)) {
    		pm = getProp("ro.real_device");
    	} else {
    		pm = getProp("ro.product.model");
    	}
        String message = getString(R.string.platform) + "   " + platform + "\n\n"
        		+ getString(R.string.cpu) + "   " + cpu + "\n\n"
        		+ getString(R.string.gpu) + "   " + gpu + "\n\n"
        		+ getString(R.string.android_version) + "   " + av + "\n\n"
                + getString(R.string.build_date) + "   " + bd + "\n\n"
                + getString(R.string.product_model) + "   " + pm + "\n";

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setMessage(message)
                .setNeutralButton(R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
        messageView.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Small);
    }
    
	public void ExecuteRoot(String commandString) {
        CommandCapture command = new CommandCapture(0, commandString);
        try { 
           RootTools.getShell(true).add(command); 
        }
        catch (Exception e) {
        	e.printStackTrace();
        	showWarningDialog(getString(R.string.no_root), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
    }
}