package org.thoughtcrime.securesms.service;


import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import org.thoughtcrime.securesms.ConversationListActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.notifications.NotificationChannels;

import java.util.concurrent.atomic.AtomicInteger;

public class ForegroundTaskManager {

  private static final int NOTIFICATION_ID = 827353982;

  private static final String KEY_TITLE      = "title";
  private static final String KEY_CHANNEL_ID = "channel_id";

  private static final ForegroundTaskManager INSTANCE = new ForegroundTaskManager();

  private final AtomicInteger foregroundCount = new AtomicInteger(0);

  public static ForegroundTaskManager getInstance() {
    return INSTANCE;
  }

  public void startTask(@NonNull Context context, @NonNull String task) {
    startTask(context, task, NotificationChannels.OTHER);
  }

  public void startTask(@NonNull Context context, @NonNull String task, @NonNull String channelId) {
    if (foregroundCount.getAndIncrement() == 0) {
      Intent intent = new Intent(context, ForegroundTask.class);
      intent.putExtra(KEY_TITLE, task);
      intent.putExtra(KEY_CHANNEL_ID, channelId);
      ContextCompat.startForegroundService(context, intent);
    }
  }

  public void stopTask(@NonNull Context context) {
    if (foregroundCount.decrementAndGet() == 0) {
      context.stopService(new Intent(context, ForegroundTask.class));
    }
  }

  public static class ForegroundTask extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      String title     = intent.getStringExtra(KEY_TITLE);
      String channelId = intent.getStringExtra(KEY_CHANNEL_ID);

      startForeground(NOTIFICATION_ID, new NotificationCompat.Builder(this, channelId)
          .setSmallIcon(R.drawable.ic_signal_grey_24dp)
          .setContentTitle(title)
          .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, ConversationListActivity.class), 0))
          .build());

      return START_NOT_STICKY;
    }
  }
}
