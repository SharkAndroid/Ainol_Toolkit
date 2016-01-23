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
    final String AT_DIR = "AT_Dir";
    final String REC_IMG = "rec.img";
    final String cpu_freq_file = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    final String gpu_freq_file = "/sys/devices/system/cpu/cpufreq/gpufreq/gpu3dfreq";
    // We use only /dev/block/actj(sdcard) read_ahead_kb
    final String memory_speed_file = "/sys/devices/virtual/bdi/93:72/read_ahead_kb";
    final String ram_file = "/proc/meminfo";
    private CurCPUThread cpu_thread = new CurCPUThread();
    private CurGPUThread gpu_thread = new CurGPUThread();
    private CurMSThread ms_thread = new CurMSThread();
    ToggleButton cpuboost;
    ToggleButton gpuboost;
    ToggleButton freezes;
    ToggleButton colorfix;
    TextView cpu_freq_value;
    TextView gpu_freq_value;
    TextView ms_value;
    boolean cpuboost_state;
    boolean gpuboost_state;
    boolean freezes_state;
    boolean colorfix_state;

    private Handler cpu_hand = new Handler() {
        public void handleMessage(Message msg) {
            cpu_freq_value.setText(toMHzCPU((String) msg.obj));
        }
    };

    private Handler gpu_hand = new Handler() {
        public void handleMessage(Message msg) {
            gpu_freq_value.setText(toMHzGPU((String) msg.obj));
        }
    };

    private Handler ms_hand = new Handler() {
        public void handleMessage(Message msg) {
            ms_value.setText(toKbs((String) msg.obj));
        }
    };

    private class CurCPUThread extends Thread {
        private boolean interrupt = false;

        public void interrupt() {
            interrupt = true;
        }

        @Override
        public void run() {
            try {
                while (!interrupt) {
                    sleep(500);
                    final String Freq = fileReadOneLine(cpu_freq_file);
                    if (Freq != null) cpu_hand.sendMessage(cpu_hand.obtainMessage(0, Freq));
                }
            } catch (InterruptedException e) {
            }
        }
    };
    
    private class CurGPUThread extends Thread {
        private boolean interrupt = false;

        public void interrupt() {
            interrupt = true;
        }

        @Override
        public void run() {
            try {
                while (!interrupt) {
                    sleep(500);
                    final String Freq = fileReadOneLine(gpu_freq_file);
                    if (Freq != null) gpu_hand.sendMessage(gpu_hand.obtainMessage(0, Freq));
                }
            } catch (InterruptedException e) {
            }
        }
    };
    
    private class CurMSThread extends Thread {
        private boolean interrupt = false;

        public void interrupt() {
            interrupt = true;
        }

        @Override
        public void run() {
            try {
                while (!interrupt) {
                    sleep(500);
                    final String Speed = fileReadOneLine(memory_speed_file);
                    if (Speed != null) ms_hand.sendMessage(ms_hand.obtainMessage(0, Speed));
                }
            } catch (InterruptedException e) {
            }
        }
    };
    
    private class AdvicesFragment extends DialogFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
            View v = inflater.inflate(R.layout.advices, null);
            TextView tv = (TextView) v.findViewById(R.id.advices);
            tv.setText(R.string.advices_text);
            return v;
        }
    }
    
    private class ChangelogFragment extends DialogFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
            View v = inflater.inflate(R.layout.changelog, null);
            TextView tv = (TextView) v.findViewById(R.id.changelog);
            tv.setText(R.string.changelog_text);
            return v;
        }
    }

    private class AboutFragment extends DialogFragment {
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

        String bplatform = getProp("ro.board.platform");
        if (bplatform == null || !bplatform.trim().equals("ATM702X")) {
            showWarningDialog(getString(R.string.unsupport_device, bplatform), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }

        if (!RootTools.isAccessGiven()) { 
            showWarningDialog(getString(R.string.no_root), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }

        File sdcard = Environment.getExternalStorageDirectory();
        sdcard = new File(sdcard.getAbsolutePath() + "/" + AT_DIR);
        sdcard.mkdirs();

        cpuboost = (ToggleButton) findViewById(R.id.cpuboost_btn);
        gpuboost = (ToggleButton) findViewById(R.id.gpuboost_btn);
        freezes = (ToggleButton) findViewById(R.id.freezes_btn);
        colorfix = (ToggleButton) findViewById(R.id.colorfix_btn);
        cpu_freq_value = (TextView) findViewById(R.id.cpu_freq_value);
        gpu_freq_value = (TextView) findViewById(R.id.gpu_freq_value);
        ms_value = (TextView) findViewById(R.id.ms_value);
        String[] c_freq = new String[0];
        String cpu_freq_line;
        String[] cpu_frequencies;
        String[] g_freq = new String[0];
        String gpu_freq_line;
        String[] gpu_frequencies;
        String[] m_s = new String[0];
        String ms_line;
        String[] memory_speed;
        
        // Change current cpu freq text if we dont have a list file
        if (!fileExists(cpu_freq_file) || (cpu_freq_line = fileReadOneLine(cpu_freq_file)) == null) {
            cpu_freq_value.setText(getString(R.string.cpu_freq_value));
        } else {
            cpu_freq_value.setText(toMHzCPU(cpu_freq_line));
            cpu_thread.start();
            c_freq = cpu_freq_line.split(" ");
            cpu_frequencies = new String[c_freq.length];
            for (int i = 0; i < cpu_frequencies.length; i++) {
                cpu_frequencies[i] = toMHzCPU(c_freq[i]);
            }
        }

        // Change current gpu freq text if we dont have a list file
        if (!fileExists(gpu_freq_file) || (gpu_freq_line = fileReadOneLine(gpu_freq_file)) == null) {
            gpu_freq_value.setText(getString(R.string.gpu_freq_value));
        } else {
            gpu_freq_value.setText(toMHzGPU(gpu_freq_line));
            gpu_thread.start();
            g_freq = gpu_freq_line.split(" ");
            gpu_frequencies = new String[g_freq.length];
            for (int i = 0; i < gpu_frequencies.length; i++) {
                gpu_frequencies[i] = toMHzGPU(g_freq[i]);
            }
        }
        
        // Change current memory speed text if we dont have a list file
        if (!fileExists(memory_speed_file) || (ms_line = fileReadOneLine(memory_speed_file)) == null) {
            ms_value.setText(getString(R.string.ms_value));
        } else {
            ms_value.setText(toKbs(ms_line));
            ms_thread.start();
            m_s = ms_line.split(" ");
            memory_speed = new String[m_s.length];
            for (int i = 0; i < memory_speed.length; i++) {
                memory_speed[i] = toKbs(m_s[i]);
            }
        }

        SharedPreferences sharedPrefs = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE);
        cpuboost.setChecked(sharedPrefs.getBoolean("cpuboost_state", false));
        gpuboost.setChecked(sharedPrefs.getBoolean("gpuboost_state", false));
        freezes.setChecked(sharedPrefs.getBoolean("freezes_state", false));
        colorfix.setChecked(sharedPrefs.getBoolean("colorfix_state", false));

        final Button rec_button = (Button) findViewById(R.id.rec_install);
        rec_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File rec = Environment.getExternalStorageDirectory();
                rec = new File(rec.getAbsolutePath() + "/" + AT_DIR + "/" + REC_IMG);
                if (rec.exists()) {
                    try {
                        Process localProcess;
                        localProcess = Runtime.getRuntime().exec("su");
                        DataOutputStream localDataOutputStream = new DataOutputStream(localProcess.getOutputStream());
                        localDataOutputStream.writeBytes("dd if=/sdcard/" + AT_DIR + "/" + REC_IMG + " " + "of=/dev/block/acta\n");
                        localDataOutputStream.writeBytes("exit\n");
                        localDataOutputStream.flush();
                        localDataOutputStream.close();
                        localProcess.waitFor();
                        localProcess.destroy();
                        Toast.makeText(ATMain.this, getString(R.string.rec_install), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(ATMain.this, getString(R.string.rec_uninstall), Toast.LENGTH_LONG).show();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Toast.makeText(ATMain.this, getString(R.string.rec_uninstall), Toast.LENGTH_LONG).show();
                    }
                } else if (!rec.exists()) {
                    Toast.makeText(ATMain.this, getString(R.string.rec_copy), Toast.LENGTH_LONG).show();
                }
            }
        });

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
                    ExecuteRoot("chmod 644 /sys/devices/system/cpu/cpufreq/user/boost");
                    ExecuteRoot("echo '1' > /sys/devices/system/cpu/cpufreq/user/boost");
                    ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/user/boost");
                    Toast.makeText(ATMain.this, getString(R.string.cpuboost_unlocked), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Warning: Maximum CPU freq unlocked!");
                    cpuboost_state = true;
                } else {
                    ExecuteRoot("chmod 644 /sys/devices/system/cpu/cpufreq/user/boost");
                    ExecuteRoot("echo '0' > /sys/devices/system/cpu/cpufreq/user/boost");
                    ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/user/boost");
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
                    ExecuteRoot("chmod 644 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
                    ExecuteRoot("echo '2' > /sys/devices/system/cpu/cpufreq/gpufreq/policy");
                    ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
                    Toast.makeText(ATMain.this, getString(R.string.gpuboost_unlocked), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Warning: Maximum GPU freq unlocked!");
                    gpuboost_state = true;
                } else {
                    ExecuteRoot("chmod 644 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
                    ExecuteRoot("echo '0' > /sys/devices/system/cpu/cpufreq/gpufreq/policy");
                    ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
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
                    boolean fixinst = false;
                    String pdevice = getProp("ro.product.device");
                    if (pdevice.trim().equals("hero2v2")) {
                        fixfile = "freezes_fix_hero2v2.zip";
                        fixinst = true;
                    } else if (pdevice.trim().equals("hero2v1")) {
                        fixfile = "freezes_fix_hero2v1.zip";
                        fixinst = true;
                    } else if (pdevice.trim().equals("venus")) {
                        fixfile = "freezes_fix_venus.zip";
                        fixinst = true;
                    } else if (pdevice.trim().equals("captain")) {
                        fixfile = "freezes_fix_captain.zip";
                        fixinst = true;
                    } else {
                        Toast.makeText(ATMain.this, getString(R.string.need_fw), Toast.LENGTH_SHORT).show();
                        fixinst = false;
                    }
                    if (fixinst == true) {
                        try {
                            ins = am.open(fixfile);
                            outs = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/" + fixfile);
                            copyFile(ins, outs);
                            ins.close();
                            ins = null;
                            outs.flush();
                            outs.close();
                            outs = null;
                        } catch (IOException e) {
                            Log.d(TAG, "Failed to copy  " + fixfile + "to " + outs, e);
                        }
                        FreezeFuncDialog();
                        Toast.makeText(ATMain.this, getString(R.string.function_enabled), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Warning: Freeze function enabled!");
                        freezes_state = true;
                    }
                } else {
                    String unfixfile = null;
                    boolean unfixinst = false;
                    String pdevice = getProp("ro.product.device");
                    if (pdevice.trim().equals("hero2v2")) {
                        unfixfile = "freezes_unfix_hero2v2.zip";
                        unfixinst = true;
                    } else if (pdevice.trim().equals("hero2v1")) {
                        unfixfile = "freezes_unfix_hero2v1.zip";
                        unfixinst = true;
                    } else if (pdevice.trim().equals("venus")) {
                        unfixfile = "freezes_unfix_venus.zip";
                        unfixinst = true;
                    } else if (pdevice.trim().equals("captain")) {
                        unfixfile = "freezes_unfix_captain.zip";
                        unfixinst = true;
                    } else {
                        Toast.makeText(ATMain.this, getString(R.string.need_fw), Toast.LENGTH_SHORT).show();
                        unfixinst = false;
                    }
                    if (unfixinst == true) {
                        try {
                            ins = am.open(unfixfile);
                            outs = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/" + unfixfile);
                            copyFile(ins, outs);
                            ins.close();
                            ins = null;
                            outs.flush();
                            outs.close();
                            outs = null;
                        } catch (IOException e) {
                            Log.d(TAG, "Failed to copy  " + unfixfile + "to " + outs, e);
                        }
                        FreezeFuncDialog();
                        Toast.makeText(ATMain.this, getString(R.string.function_disabled), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Warning: Freeze function disabled!");
                        freezes_state = false;
                    }
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
                    ExecuteRoot("chmod 644 /sys/devices/system/cpu/cpufreq/interactive/boost");
                    ExecuteRoot("echo '1' > /sys/devices/system/cpu/cpufreq/interactive/boost");
                    ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/interactive/boost");
                    Toast.makeText(ATMain.this, getString(R.string.function_enabled), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Warning: Colorfix function enabled!");
                    colorfix_state = true;
                } else {
                    ExecuteRoot("chmod 644 /sys/devices/system/cpu/cpufreq/interactive/boost");
                    ExecuteRoot("echo '0' > /sys/devices/system/cpu/cpufreq/interactive/boost");
                    ExecuteRoot("chmod 755 /sys/devices/system/cpu/cpufreq/interactive/boost");
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

        cpu_thread.interrupt();
        try {
            cpu_thread.join();
        } catch (InterruptedException e) {
        }
        gpu_thread.interrupt();
        try {
            gpu_thread.join();
        } catch (InterruptedException e) {
        }
        ms_thread.interrupt();
        try {
            ms_thread.join();
        } catch (InterruptedException e) {
        }
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
            case R.id.advices:
                DialogFragment df1 = new AdvicesFragment();
                df1.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                df1.show(getFragmentManager(), "advices");
                return true;
            case R.id.changelog:
                DialogFragment df2 = new ChangelogFragment();
                df2.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                df2.show(getFragmentManager(), "changelog");
                return true;
            case R.id.about:
                DialogFragment df3 = new AboutFragment();
                df3.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                df3.show(getFragmentManager(), "about");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void AboutSystem() {
        /* Device info */
        String platform = "gs702a";
        String cpu = "Actions ATM7029";
        String gpu = "Vivante GC1000+MP";
        String ram = getRAM();

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
                + getString(R.string.gpu) + "   " + gpu + "\n\n"
                + getString(R.string.ram) + "   " + ram + " DDR3" + "\n";

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
        .setTitle(R.string.as_title)
        .setMessage(message)
        .setNeutralButton(R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void FreezeFuncDialog() {
        /* Message text */
        String message = getString(R.string.advices_text);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
        .setTitle(R.string.freezes)
        .setMessage(message)
        .setNeutralButton(R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    // Get string from build.prop
    public static String getProp(String key) {
        try {
            Process process = Runtime.getRuntime().exec(String.format("getprop %s", key));
            String value = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
            process.destroy();
            return value;
        } catch (IOException e) {
            Log.d("getProp exception", e.toString(), e);
            return null;
        }
    }   

    // Dialog interface
    public AlertDialog showWarningDialog(String text,DialogInterface.OnClickListener onClickListener) {
        return new AlertDialog.Builder(this)
                .setMessage(text)
                .setNeutralButton(R.string.exit, onClickListener)
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
    
    // Copy file from asset to sdcard
    private void copyFile(InputStream ins, OutputStream outs) throws IOException {
          byte[] buffer = new byte[1024];
          int read;
          while((read = ins.read(buffer)) != -1) {
                outs.write(buffer, 0, read);
          }
    }
    
    // Checking if file exists
    private static boolean fileExists(String filename) {
        return new File(filename).exists();
    }
    
    // Read line in the file
    private String fileReadOneLine(String fname) {
        BufferedReader br;
        String line = null;

        try {
            br = new BufferedReader(new FileReader(fname), 512);
            try {
                line = br.readLine();
            } finally {
                br.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception when reading /sys/* file", e);
        }
        return line;
    }
    
    // String for CPU freq.
    private String toMHzCPU(String mhzString) {
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000).append(" MHz")
                .toString();
    }
    
    // String for GPU freq.
    private String toMHzGPU(String mhzString) {
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000000).append(" MHz")
                .toString();
    }
    
    // String for Memory Speed.
    private String toKbs(String kbsString) {
        return new StringBuilder().append(Integer.valueOf(kbsString) / 1).append(" Kb/s")
                .toString();
    }

    // Reading a ram file(/proc/meminfo)
    private String getRAM() {
        String result = null;
        try {
            String firstLine = fileReadOneLine(ram_file);
            if (firstLine != null) {
                String parts[] = firstLine.split("\\s+");
                if (parts.length == 3) {
                    result = Long.parseLong(parts[1]) / 1024 + " MB";
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception when reading " + ram_file + ", e");
        }

        return result;
    }
}