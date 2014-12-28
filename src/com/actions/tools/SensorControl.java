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

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SensorControl {
	
  public static final String TAG = "SensorControl";
  public static final String CALIB_FILE = "gsensor_calib.txt";
  public static final String CALIB_RESET_ATTR = "calibration_reset";
  public static final String CALIB_RUN_ATTR = "calibration_run";
  public static final String CALIB_VAL_ATTR = "calibration_value";
  public static final String INPUT_DIR = "/sys/class/input";
  public static final String GSENSORS = "mma8452,mc3210";
  public String classPath = null;
  public String dataPath = null;

  public SensorControl(Context paramContext) {
    dataPath = paramContext.getFilesDir().getAbsolutePath();
  }

  public String getClassPath() {
    File[] arrayOfFile = null;
    if (classPath == null)
    arrayOfFile = new File(INPUT_DIR).listFiles();
    for (int i = 0; ; i++)
    if (i < arrayOfFile.length) {
      if ((arrayOfFile[i].isDirectory()) && (arrayOfFile[i].getName().contains("input"))) {
        String str = readFile(arrayOfFile[i].getAbsolutePath() + "/name");
        if ((str != null) && (GSENSORS.contains(str.trim()))) {
          classPath = arrayOfFile[i].getAbsolutePath();
          Log.d(TAG, "classPath: " + classPath);
        }
      }
    }
    else
    return classPath;
  }

  public String getDataPath(String paramString) {
    return dataPath + "/" + paramString;
  }

  public String getDevPath(String paramString) {
    return getClassPath() + "/" + paramString;
  }

  public String readFile(String paramString) {
    byte[] arrayOfByte = null;
    try {
      FileInputStream localFileInputStream = new FileInputStream(paramString);
      arrayOfByte = new byte[localFileInputStream.available()];
      localFileInputStream.read(arrayOfByte);
      localFileInputStream.close();
      return new String(arrayOfByte);
    }
    catch (Exception localException) {
      while (true) {
        Log.d(TAG, "read " + paramString + " error!");
        localException.printStackTrace();
      }
    }
  }

  public void writeFile(String paramString1, String paramString2) {
    try {
      FileOutputStream localFileOutputStream = new FileOutputStream(paramString1);
      localFileOutputStream.write(paramString2.getBytes());
      localFileOutputStream.close();
      return;
    }
    catch (Exception localException) {
      Log.d(TAG, "write " + paramString1 + " error!");
      localException.printStackTrace();
    }
  }

  public String getCalibValue() {
    return readFile(getDevPath(CALIB_VAL_ATTR)).trim();
  }

  public void resetCalib() {
    writeFile(getDevPath(CALIB_RESET_ATTR), "1");
  }

  public void runCalib() {
    writeFile(getDevPath(CALIB_RUN_ATTR), "1");
  }

  public void writeCalibFile(String paramString) {
    writeFile(getDataPath(CALIB_FILE), paramString);
  }
}