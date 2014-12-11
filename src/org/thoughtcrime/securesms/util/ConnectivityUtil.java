package org.thoughtcrime.securesms.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

public class ConnectivityUtil {
  private static final String TAG = ConnectivityUtil.class.getSimpleName();

  public static boolean isWifiConnected(Context context) {
    ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    return wifi != null && wifi.isConnected();
  }

  @SuppressWarnings("deprecation")
  public static boolean isRoaming(Context context) {
    try {
      return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.DATA_ROAMING) == 1;
    }
    catch (SettingNotFoundException e) {
      Log.w(TAG, "Couldn't retrieve roaming setting, returning vacuous false.");
      return false;
    }
  }
}
