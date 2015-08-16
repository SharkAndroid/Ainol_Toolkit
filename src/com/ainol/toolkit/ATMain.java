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
import android.content.res.AssetManager;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.text.*;
import android.util.*;

import java.io.*;
import java.lang.Process;
import java.lang.String;

import com.ainol.toolkit.R;
import com.ainol.toolkit.SensorActivity;
import com.stericson.RootTools.*;
import com.stericson.RootTools.execution.*;

public class ATMain extends Activity {
	final String TAG = "ATMain";
    final String SETTINGS_KEY = "settings";
    private ToggleButton cpuboost;
    private ToggleButton gpuboost;
    private ToggleButton freezes;
    private ToggleButton colorfix;
    boolean cpuboost_state;
    boolean gpuboost_state;
    boolean freezes_state;
    boolean colorfix_state;

    public class ChangelogFragment extends DialogFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
            View v = inflater.inflate(R.layout.changelog, null);
            TextView tv = (TextView) v.findViewById(R.id.changelog);
            tv.setText(R.string.changelog_text);
            return v;
        }
    }

    public class AboutFragment extends DialogFragment {
    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
            View v = inflater.inflate(R.layout.about, null);
            TextView tv = (TextView) v.findViewById(R.id.about);
            tv.setText(R.string.about_text);
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
        colorfix = (ToggleButton) findViewById(R.id.colorfix_btn);

        SharedPreferences sharedPrefs = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE);
        cpuboost.setChecked(sharedPrefs.getBoolean("cpuboost_state", false));
        gpuboost.setChecked(sharedPrefs.getBoolean("gpuboost_state", false));
        freezes.setChecked(sharedPrefs.getBoolean("freezes_state", false));
        colorfix.setChecked(sharedPrefs.getBoolean("colorfix_state", false));

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

        final Button sensor_button = (Button) findViewById(R.id.sensor_button);
        sensor_button.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
            	Intent sb = new Intent(ATMain.this, SensorActivity.class);
                startActivity(sb);
            }
        });

        final Button as_button = (Button) findViewById(R.id.as_button);
        as_button.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		AboutSystem();
        	}
        });

        cpuboost.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
                if (cpuboost.isChecked()) {
                	ExecuteRoot("echo '1' > /sys/devices/system/cpu/cpufreq/user/boost");
                    ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/user/boost");
                    Toast.makeText(ATMain.this, getString(R.string.cpuboost_unlocked), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Warning: Maximum CPU freq unlocked!");
                    cpuboost_state = true;
                } else {
                	ExecuteRoot("chmod 666 /sys/devices/system/cpu/cpufreq/user/boost");
                    ExecuteRoot("echo '0' > /sys/devices/system/cpu/cpufreq/user/boost");
                    ExecuteRoot("chmod 777 /sys/devices/system/cpu/cpufreq/user/boost");
                    Toast.makeText(ATMain.this, getString(R.string.cpuboost_locked), Toast.LENGTH_SHORT).show();
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
	                Toast.makeText(ATMain.this, getString(R.string.gpuboost_unlocked), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Warning: Maximum GPU freq unlocked!");
                    gpuboost_state = true;
                } else {
                	ExecuteRoot("chmod 666 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
	                ExecuteRoot("echo '0' > /sys/devices/system/cpu/cpufreq/gpufreq/policy");
	                ExecuteRoot("chmod 777 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
	                Toast.makeText(ATMain.this, getString(R.string.gpuboost_locked), Toast.LENGTH_SHORT).show();
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
				AssetManager am = getAssets();
				InputStream ins = null;
			    OutputStream outs = null;
				if (freezes.isChecked()) {
					String fixfile = null;
					String pdevice = getProp("ro.product.device");
					if (pdevice.trim().equals("hero2v2")) {
						fixfile = "freezes_fix_hero2v2.zip";
					}
					else if (pdevice.trim().equals("hero2v1")) {
						fixfile = "freezes_fix_hero2v1.zip";
					}
					else if (pdevice.trim().equals("venus")) {
						fixfile = "freezes_fix_venus.zip";
					}
					else if (pdevice.trim().equals("captain")) {
						fixfile = "freezes_fix_captain.zip";
					}
					try {
						ins = am.open(fixfile);
			            outs = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/" + fixfile);
			            copyFile(ins, outs);
			            ins.close();
			            ins = null;
			            outs.flush();
			            outs.close();
			            outs = null;
					}
					catch(IOException e) {
						Log.d(TAG, "Failed to copy  " + fixfile + "to " + outs, e);
				    }
		            Toast.makeText(ATMain.this, getString(R.string.function_enabled), Toast.LENGTH_SHORT).show();
		            Log.d(TAG, "Warning: Freeze function enabled!");
		            freezes_state = true;  
		        } else {
		        	String unfixfile = null;
		        	String pdevice = getProp("ro.product.device");
					if (pdevice.trim().equals("hero2v2")) {
						unfixfile = "freezes_unfix_hero2v2.zip";
					}
					else if (pdevice.trim().equals("hero2v1")) {
						unfixfile = "freezes_unfix_hero2v1.zip";
					}
					else if (pdevice.trim().equals("venus")) {
						unfixfile = "freezes_unfix_venus.zip";
					}
					else if (pdevice.trim().equals("captain")) {
						unfixfile = "freezes_unfix_captain.zip";
					}
					try {
						ins = am.open(unfixfile);
			            outs = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/" + unfixfile);
			            copyFile(ins, outs);
			            ins.close();
			            ins = null;
			            outs.flush();
			            outs.close();
			            outs = null;
					}
					catch(IOException e) {
						Log.d(TAG, "Failed to copy  " + unfixfile + "to " + outs, e);
				    }
		            Toast.makeText(ATMain.this, getString(R.string.function_disabled), Toast.LENGTH_SHORT).show();
		            Log.d(TAG, "Warning: Freeze function disabled!");
		            freezes_state = false;
		        }
		        SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE).edit();
		        editor.putBoolean("freezes_state", freezes_state);
		        editor.commit();
			}
        });
        
        colorfix.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (colorfix.isChecked()) {
		        	ExecuteRoot("echo '1' > /sys/devices/system/cpu/cpufreq/interactive/boost");
		            ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/interactive/boost");
		            Toast.makeText(ATMain.this, getString(R.string.function_enabled), Toast.LENGTH_SHORT).show();
		            Log.d(TAG, "Warning: Colorfix function enabled!");
		            colorfix_state = true;
		        } else {
		        	ExecuteRoot("chmod 666 /sys/devices/system/cpu/cpufreq/interactive/boost");
		            ExecuteRoot("echo '0' > /sys/devices/system/cpu/cpufreq/interactive/boost");
		            ExecuteRoot("chmod 777 /sys/devices/system/cpu/cpufreq/interactive/boost");
		            Toast.makeText(ATMain.this, getString(R.string.function_disabled), Toast.LENGTH_SHORT).show();
		            Log.d(TAG, "Warning: Colorfix function disabled!");
		            colorfix_state = false;
		        }
		        SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE).edit();
		        editor.putBoolean("colorfix_state", colorfix_state);
		        editor.commit();
			}
        });
	}

	@Override
    public void onDestroy() {
		super.onDestroy();

    	moveTaskToBack(true);
    	System.runFinalizersOnExit(true);
    	finishAffinity();
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
                intent.setData(Uri.parse("http://github.com/SharkAndroid/update_manager/blob/master/Ainol_Toolkit/Ainol_Toolkit.apk?raw=true"));
                startActivity(intent);
                return true;
            case R.id.source_code:
                Intent intent2 = new Intent();
                intent2.setAction(Intent.ACTION_VIEW);
                intent2.addCategory(Intent.CATEGORY_BROWSABLE);
                intent2.setData(Uri.parse("http://github.com/SharkAndroid/Ainol_Toolkit"));
                startActivity(intent2);
                return true;
            case R.id.changelog:
                DialogFragment df1 = new ChangelogFragment();
                df1.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                df1.show(getFragmentManager(), "changelog");
                return true;
            case R.id.about:
            	DialogFragment df2 = new AboutFragment();
                df2.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                df2.show(getFragmentManager(), "about");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void AboutSystem() {
    	/* Device info */
    	String cpu = "Actions ATM7029";
    	String gpu = "Vivante GC1000+MP";
    	String platform = "gs702a";

    	/* Android info */
    	String android = getProp("ro.build.version.release");
    	String date = getProp("ro.build.date");

    	/* Check if CyanogenMod,AOKP,AOSPA installed on device. */
    	String cm = getProp("ro.cm.version");
    	String aokp = getProp("ro.aokp.version");
    	String aospa = getProp("ro.modversion");

    	/* Device name */
    	String model = null;
        if (!TextUtils.isEmpty(cm) || !TextUtils.isEmpty(aokp) || !TextUtils.isEmpty(aospa)) {
        	model = getProp("ro.real_device");
        } else {
        	model = getProp("ro.product.model");
        }

        /* Message text */
        String message = getString(R.string.product_model) + "   " + model + "\n\n"
        		+ getString(R.string.android_version) + "   " + android + "\n\n"
        		+ getString(R.string.build_date) + "   " + date + "\n\n"
        		+ getString(R.string.platform) + "   " + platform + "\n\n"
        		+ getString(R.string.cpu) + "   " + cpu + "\n\n"
        		+ getString(R.string.gpu) + "   " + gpu + "\n";

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
        .setTitle(R.string.as_title)
        .setMessage(message)
        .setNeutralButton(R.string.ok, null);

		AlertDialog dialog = builder.create();
		dialog.show();
    }
    
    // Get string from build.prop
    public static String getProp(String key) {
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

    // Dialog interface
	public AlertDialog showWarningDialog(String text,DialogInterface.OnClickListener onClickListener) {
        return new AlertDialog.Builder(this)
                .setMessage(text)
                .setNeutralButton(R.string.exit,onClickListener)
                .setCancelable(false)
                .show();
    }
    
	// Root checker
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
	
	// Copy file from asset to misc
	public void copyFile(InputStream ins, OutputStream outs) throws IOException {
	      byte[] buffer = new byte[1024];
	      int read;
	      while((read = ins.read(buffer)) != -1) {
	            outs.write(buffer, 0, read);
	      }
	}
}