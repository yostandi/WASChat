package org.thoughtcrime.securesms.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

public class MemoryUtil {
  public static boolean isLowMemory(Context context) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

    boolean lowRam = VERSION.SDK_INT >= VERSION_CODES.KITKAT && activityManager.isLowRamDevice();
    return lowRam || activityManager.getMemoryClass() < 32;
  }
}
