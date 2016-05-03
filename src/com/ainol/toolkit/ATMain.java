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
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ainol.toolkit.R;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.*;

public class ATMain extends Activity {
    /* Utils */
    public static int ttime = 6000; // 6 seconds
    public static Date mDate = new Date();
    public static SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private AlertDialog.Builder mAlerDialogBuilder;

    /* Main variables */
    private String TAG = "ATMain";
    private String SETTINGS_KEY = "settings";
    public static String AT_DIR = "AT_Dir";
    public static String BACKUP_DIR = "backups";
    private String ATRIMG_Name = "ATRIMG_" + mSimpleDateFormat.format(mDate);
    private String REC_IMG = "rec.img";
    private String cpu_freq_file = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    private String gpu_freq_file = "/sys/devices/system/cpu/cpufreq/gpufreq/gpu3dfreq";
    // We use only /dev/block/actj(sdcard) read_ahead_kb
    private String memory_speed_file = "/sys/devices/virtual/bdi/93:72/read_ahead_kb";
    private String ram_file = "/proc/meminfo";
    private CurCPUThread mCurCPUThread = new CurCPUThread();
    private CurGPUThread mCurGPUThread = new CurGPUThread();
    private CurMSThread mCurMSThread = new CurMSThread();
    private ToggleButton mCPUBoost;
    private ToggleButton mGPUBoost;
    private ToggleButton mFreezes;
    private ToggleButton mColorfix;
    private TextView mCPU_Freq_Value;
    private TextView mGPU_Freq_Value;
    private TextView mMS_Value;
    private boolean mCPUBoost_State;
    private boolean mGPUBoost_State;
    private boolean mFreezes_State;
    private boolean mColorfix_State;
    private boolean mWD_State;

    private Handler cpu_hand = new Handler() {
        public void handleMessage(Message msg) {
            mCPU_Freq_Value.setText(toMHzCPU((String) msg.obj));
        }
    };

    private Handler gpu_hand = new Handler() {
        public void handleMessage(Message msg) {
            mGPU_Freq_Value.setText(toMHzGPU((String) msg.obj));
        }
    };

    private Handler ms_hand = new Handler() {
        public void handleMessage(Message msg) {
            mMS_Value.setText(toKbs((String) msg.obj));
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
                    String Freq = fileReadOneLine(cpu_freq_file);
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
                    String Freq = fileReadOneLine(gpu_freq_file);
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
                    String Speed = fileReadOneLine(memory_speed_file);
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

        /* Compability checker */
        String cd = "/sys/devices/system/cpu/cpuidle/current_driver";
        String driver = fileReadOneLine(cd);
        if (!driver.trim().equals("leopard_idle")) {
            showWarningDialog(getString(R.string.unsupport_device, driver), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }

        /* Initialize layout */
        mCPUBoost = (ToggleButton) findViewById(R.id.cpuboost_btn);
        mGPUBoost = (ToggleButton) findViewById(R.id.gpuboost_btn);
        mFreezes = (ToggleButton) findViewById(R.id.freezes_btn);
        mColorfix = (ToggleButton) findViewById(R.id.colorfix_btn);
        mCPU_Freq_Value = (TextView) findViewById(R.id.cpu_freq_value);
        mGPU_Freq_Value = (TextView) findViewById(R.id.gpu_freq_value);
        mMS_Value = (TextView) findViewById(R.id.ms_value);

        String[] c_freq = new String[0];
        String cpu_freq_line;
        String[] cpu_frequencies;
        String[] g_freq = new String[0];
        String gpu_freq_line;
        String[] gpu_frequencies;
        String[] m_s = new String[0];
        String ms_line;
        String[] memory_speed;

        /* Root checker */
        if (!RootTools.isAccessGiven()) { 
            showWarningDialog(getString(R.string.no_root), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }

        /* Create AT_DIR and backups folder on sdcard */
        File sdcard = Environment.getExternalStorageDirectory();
        sdcard = new File(sdcard.getAbsolutePath() + "/" + AT_DIR);
        sdcard.mkdirs();

        /* Force fix values for cpu */
        ExecuteRoot("echo '0-3' > /sys/devices/system/cpu/online");
        ExecuteRoot("echo ' ' > /sys/devices/system/cpu/offline");
        ExecuteRoot("echo '1' > /sys/devices/system/cpu/cpu2/online");
        ExecuteRoot("echo '1' > /sys/devices/system/cpu/cpu3/online");
        ExecuteRoot("echo '1200000' > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
        ExecuteRoot("echo '1200000' > /sys/devices/system/cpu/cpu1/cpufreq/scaling_max_freq");
        ExecuteRoot("echo '1200000' > /sys/devices/system/cpu/cpu2/cpufreq/scaling_max_freq");
        ExecuteRoot("echo '1200000' > /sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq");
        ExecuteRoot("chmod 444 /sys/devices/system/cpu/online");
        ExecuteRoot("chmod 444 /sys/devices/system/cpu/offline");
        ExecuteRoot("chmod 444 /sys/devices/system/cpu/cpu2/online");
        ExecuteRoot("chmod 444 /sys/devices/system/cpu/cpu3/online");
        ExecuteRoot("chmod 444 /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
        ExecuteRoot("chmod 444 /sys/devices/system/cpu/cpu1/cpufreq/scaling_max_freq");
        ExecuteRoot("chmod 444 /sys/devices/system/cpu/cpu2/cpufreq/scaling_max_freq");
        ExecuteRoot("chmod 444 /sys/devices/system/cpu/cpu3/cpufreq/scaling_max_freq");

        // Change current cpu freq text if we dont have a list file
        if (!fileExists(cpu_freq_file) || (cpu_freq_line = fileReadOneLine(cpu_freq_file)) == null) {
            mCPU_Freq_Value.setText(getString(R.string.cpu_freq_value));
        } else {
            mCPU_Freq_Value.setText(toMHzCPU(cpu_freq_line));
            mCurCPUThread.start();
            c_freq = cpu_freq_line.split(" ");
            cpu_frequencies = new String[c_freq.length];
            for (int i = 0; i < cpu_frequencies.length; i++) {
                cpu_frequencies[i] = toMHzCPU(c_freq[i]);
            }
        }

        // Change current gpu freq text if we dont have a list file
        if (!fileExists(gpu_freq_file) || (gpu_freq_line = fileReadOneLine(gpu_freq_file)) == null) {
            mGPU_Freq_Value.setText(getString(R.string.gpu_freq_value));
        } else {
            mGPU_Freq_Value.setText(toMHzGPU(gpu_freq_line));
            mCurGPUThread.start();
            g_freq = gpu_freq_line.split(" ");
            gpu_frequencies = new String[g_freq.length];
            for (int i = 0; i < gpu_frequencies.length; i++) {
                gpu_frequencies[i] = toMHzGPU(g_freq[i]);
            }
        }

        // Change current memory speed text if we dont have a list file
        if (!fileExists(memory_speed_file) || (ms_line = fileReadOneLine(memory_speed_file)) == null) {
            mMS_Value.setText(getString(R.string.ms_value));
        } else {
            mMS_Value.setText(toKbs(ms_line));
            mCurMSThread.start();
            m_s = ms_line.split(" ");
            memory_speed = new String[m_s.length];
            for (int i = 0; i < memory_speed.length; i++) {
                memory_speed[i] = toKbs(m_s[i]);
            }
        }

        // Save variables
        final SharedPreferences sharedPrefs = getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE);
        mCPUBoost.setChecked(sharedPrefs.getBoolean("cpuboost_state", false));
        mGPUBoost.setChecked(sharedPrefs.getBoolean("gpuboost_state", false));
        mFreezes.setChecked(sharedPrefs.getBoolean("freezes_state", false));
        mColorfix.setChecked(sharedPrefs.getBoolean("colorfix_state", false));
        mWD_State = sharedPrefs.getBoolean("wd_state", false);

        // Warning dialog for freezes and colorfix functions
        if (!mWD_State) {
            String wdtitle = getString(R.string.wd_title);
            String wdtext = getString(R.string.wd_text);
            String ok = getString(R.string.ok);
    
            mAlerDialogBuilder = new AlertDialog.Builder(ATMain.this);
            mAlerDialogBuilder.setTitle(wdtitle);
            mAlerDialogBuilder.setMessage(wdtext);
            mAlerDialogBuilder.setCancelable(false);
            mAlerDialogBuilder.setNeutralButton(ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean("wd_state", true);
                    editor.commit();
                    dialog.cancel();
                }
            });

            AlertDialog ad = mAlerDialogBuilder.create();
            ad.show();
        }

        // CPU boost function
        mCPUBoost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCPUBoost.isChecked()) {
                    function("cpuon");
                    Toast.makeText(ATMain.this, getString(R.string.cpuboost_unlocked), ttime).show();
                    Log.d(TAG, "Warning: Maximum CPU freq unlocked!");
                    mCPUBoost_State = true;
                } else {
                    function("cpuoff");
                    Toast.makeText(ATMain.this, getString(R.string.cpuboost_locked), ttime).show();
                    Log.d(TAG, "Warning: Maximum CPU freq locked!");
                    mCPUBoost_State = false;
                }
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("cpuboost_state", mCPUBoost_State);
                editor.commit();
            }
        });

        // GPU boost function
        mGPUBoost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGPUBoost.isChecked()) {
                    function("gpuon");
                    Toast.makeText(ATMain.this, getString(R.string.gpuboost_unlocked), ttime).show();
                    Log.d(TAG, "Warning: Maximum GPU freq unlocked!");
                    mGPUBoost_State = true;
                } else {
                    function("gpuoff");
                    Toast.makeText(ATMain.this, getString(R.string.gpuboost_locked), ttime).show();
                    Log.d(TAG, "Warning: Maximum GPU freq locked!");
                    mGPUBoost_State = false;
                }
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("gpuboost_state", mGPUBoost_State);
                editor.commit();
            }
        }); 

        // Freezes function
        mFreezes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFreezes.isChecked()) {
                    function("freezeson");
                    FreezeFuncDialog();
                    Toast.makeText(ATMain.this, getString(R.string.function_enabled), ttime).show();
                    Log.d(TAG, "Warning: Freeze function enabled!");
                    mFreezes_State = true;
                } else {
                    function("freezesoff");
                    FreezeFuncDialog();
                    Toast.makeText(ATMain.this, getString(R.string.function_disabled), ttime).show();
                    Log.d(TAG, "Warning: Freeze function disabled!");
                    mFreezes_State = false;
                }
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("freezes_state", mFreezes_State);
                editor.commit();
            }
        });

        // Colorfix function
        mColorfix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mColorfix.isChecked()) {
                    function("colorfixon");
                    Toast.makeText(ATMain.this, getString(R.string.function_enabled), ttime).show();
                    Log.d(TAG, "Warning: Colorfix function enabled!");
                    mColorfix_State = true;
                } else {
                    function("colorfixoff");
                    Toast.makeText(ATMain.this, getString(R.string.function_disabled), ttime).show();
                    Log.d(TAG, "Warning: Colorfix function disabled!");
                    mColorfix_State = false;
                }
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("colorfix_state", mColorfix_State);
                editor.commit();
            }
        });

        // Recovery backup function
        final Button crimg_button = (Button) findViewById(R.id.crimg_button);
        crimg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    File rf = Environment.getExternalStorageDirectory();
                    rf = new File(rf.getAbsolutePath() + "/" + AT_DIR + "/" + ATRIMG_Name);
                    rf.mkdirs();
                    Process lp;
                    lp = Runtime.getRuntime().exec("su");
                    DataOutputStream ldos = new DataOutputStream(lp.getOutputStream());
                    ldos.writeBytes("dd if=/dev/block/acta of=/sdcard/" + AT_DIR + "/" + ATRIMG_Name + "/" + REC_IMG + "\n");
                    ldos.writeBytes("exit\n");
                    ldos.flush();
                    ldos.close();
                    lp.waitFor();
                    lp.destroy();
                    SystemClock.sleep(1000);
                    Toast.makeText(ATMain.this, getString(R.string.crimg_install) + " " + AT_DIR + "/" + ATRIMG_Name, ttime).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ATMain.this, getString(R.string.error), ttime).show();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Toast.makeText(ATMain.this, getString(R.string.error), ttime).show();
                }
            }
        });

        // Recovery install function
        final Button rec_button = (Button) findViewById(R.id.recins_button);
        rec_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File rec = Environment.getExternalStorageDirectory();
                rec = new File(rec.getAbsolutePath() + "/" + AT_DIR + "/" + REC_IMG);
                if (rec.exists()) {
                    try {
                        Process lp;
                        lp = Runtime.getRuntime().exec("su");
                        DataOutputStream ldos = new DataOutputStream(lp.getOutputStream());
                        ldos.writeBytes("dd if=/sdcard/" + AT_DIR + "/" + REC_IMG + " " + "of=/dev/block/acta\n");
                        ldos.writeBytes("exit\n");
                        ldos.flush();
                        ldos.close();
                        lp.waitFor();
                        lp.destroy();
                        SystemClock.sleep(1000);
                        Toast.makeText(ATMain.this, getString(R.string.rec_install), ttime).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(ATMain.this, getString(R.string.rec_uninstall), ttime).show();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Toast.makeText(ATMain.this, getString(R.string.rec_uninstall), ttime).show();
                    }
                } else if (!rec.exists()) {
                    Toast.makeText(ATMain.this, getString(R.string.rec_copy), ttime).show();
                }
            }
        });

        // Modules activity
        final Button modules_button = (Button) findViewById(R.id.modules_button);
        modules_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mb = new Intent(ATMain.this, ModulesActivity.class);
                startActivity(mb);
            }
        });

        // About system
        final Button as_button = (Button) findViewById(R.id.as_button);
        as_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutSystem();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        moveTaskToBack(true);
        System.runFinalizersOnExit(true);

        mCurCPUThread.interrupt();
        try {
            mCurCPUThread.join();
        } catch (InterruptedException e) {
        }

        mCurGPUThread.interrupt();
        try {
            mCurGPUThread.join();
        } catch (InterruptedException e) {
        }

        mCurMSThread.interrupt();
        try {
            mCurMSThread.join();
        } catch (InterruptedException e) {
        }

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

        /* HDMI mode */
        String hdmi = getProp("ro.hdmi.onoffmode");
        String hdmimode = null;
        if (hdmi.trim().equals("auto")) {
            hdmimode = getString(R.string.hm1);
        } else if (hdmi.trim().equals("alwayson")) {
            hdmimode = getString(R.string.hm2);
        }

        /* Message text */
        String message = getString(R.string.product_model) + "   " + model + "\n\n"
                + getString(R.string.android_version) + "   " + android + "\n\n"
                + getString(R.string.build_date) + "   " + date + "\n\n"
                + getString(R.string.platform) + "   " + platform + "\n\n"
                + getString(R.string.cpu) + "   " + cpu + "\n\n"
                + getString(R.string.gpu) + "   " + gpu + "\n\n"
                + getString(R.string.ram) + "   " + ram + " DDR3" + "\n\n"
                + getString(R.string.hdmi) + "   " + hdmimode + "\n";

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

    public void function(String func) {
        /* For freezes function */
        AssetManager am = getAssets();
        InputStream ins = null;
        OutputStream outs = null;

        if (func == "cpuon") {
            ExecuteRoot("chmod 644 /sys/devices/system/cpu/cpufreq/user/boost");
            ExecuteRoot("echo '1' > /sys/devices/system/cpu/cpufreq/user/boost");
            ExecuteRoot("chmod 444 /sys/devices/system/cpu/cpufreq/user/boost");
        } else if (func == "cpuoff") {
            ExecuteRoot("chmod 644 /sys/devices/system/cpu/cpufreq/user/boost");
            ExecuteRoot("echo '0' > /sys/devices/system/cpu/cpufreq/user/boost");
            ExecuteRoot("chmod 444 /sys/devices/system/cpu/cpufreq/user/boost");
        } else if (func == "gpuon") {
            ExecuteRoot("chmod 644 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
            ExecuteRoot("echo '2' > /sys/devices/system/cpu/cpufreq/gpufreq/policy");
            ExecuteRoot("chmod 444 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
        } else if (func == "gpuoff") {
            ExecuteRoot("chmod 644 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
            ExecuteRoot("echo '0' > /sys/devices/system/cpu/cpufreq/gpufreq/policy");
            ExecuteRoot("chmod 444 /sys/devices/system/cpu/cpufreq/gpufreq/policy");
        } else if (func == "freezeson") {
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
            } else if (!pdevice.trim().equals("hero2v2") || !pdevice.trim().equals("hero2v1") || 
                    !pdevice.trim().equals("venus") || !pdevice.trim().equals("captain")) {
                Toast.makeText(ATMain.this, getString(R.string.need_device), ttime).show();
                fixinst = false;
            } else {
                Toast.makeText(ATMain.this, getString(R.string.need_fw), ttime).show();
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
            }
        } else if (func == "freezesoff") {
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
            } else if (!pdevice.trim().equals("hero2v2") || !pdevice.trim().equals("hero2v1") || 
                    !pdevice.trim().equals("venus") || !pdevice.trim().equals("captain")) {
                Toast.makeText(ATMain.this, getString(R.string.need_device), ttime).show();
                unfixinst = false;
            } else {
                Toast.makeText(ATMain.this, getString(R.string.need_fw), ttime).show();
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
            }
        } else if (func == "colorfixon") {
            ExecuteRoot("chmod 644 /sys/devices/system/cpu/cpufreq/interactive/boost");
            ExecuteRoot("echo '1' > /sys/devices/system/cpu/cpufreq/interactive/boost");
            ExecuteRoot("chmod 444 /sys/devices/system/cpu/cpufreq/interactive/boost");
        } else if (func == "colorfixoff") {
            ExecuteRoot("chmod 644 /sys/devices/system/cpu/cpufreq/interactive/boost");
            ExecuteRoot("echo '0' > /sys/devices/system/cpu/cpufreq/interactive/boost");
            ExecuteRoot("chmod 444 /sys/devices/system/cpu/cpufreq/interactive/boost");
        } else return;
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
    public AlertDialog showWarningDialog(String text, DialogInterface.OnClickListener onClickListener) {
        return new AlertDialog.Builder(this)
                .setMessage(text)
                .setNeutralButton(R.string.exit, onClickListener)
                .setCancelable(false)
                .show();
    }
    
    // Root checker
    public void ExecuteRoot(String commandString) {
        Command command = new Command(0, commandString);

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