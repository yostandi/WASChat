package org.thoughtcrime.securesms.service;


import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.DefaultLifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.thoughtcrime.securesms.ApplicationContext;
import org.thoughtcrime.securesms.ConversationListActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.notifications.NotificationChannels;
import org.whispersystems.libsignal.logging.Log;

import java.util.concurrent.atomic.AtomicInteger;

public class ForegroundTaskManager {

  private static final int NOTIFICATION_ID  = 827353982;

  private static volatile ForegroundTaskManager instance;

  private final AtomicInteger foregroundCount = new AtomicInteger(0);
  private final Context       context;

  private @Nullable ForegroundTask.LocalBinder binder;
  private @Nullable String                     lastTaskName;
  private @Nullable String                     lastChannelId;

  public static ForegroundTaskManager getInstance(@NonNull Context context) {
    if (instance == null) {
      synchronized (ForegroundTaskManager.class) {
        if (instance == null) {
          instance = new ForegroundTaskManager(context);
        }
      }
    }
    return instance;
  }

  private ForegroundTaskManager(@NonNull Context context) {
    this.context = context;

    ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
      @Override
      public void onStart(@NonNull LifecycleOwner owner) {
        if (binder != null && foregroundCount.get() > 0) {
          stop(binder);
        }
      }

      @Override
      public void onStop(@NonNull LifecycleOwner owner) {
        if (binder != null && foregroundCount.get() > 0 && lastTaskName != null && lastChannelId != null) {
          start(binder, lastTaskName, lastChannelId);
        }
      }
    });

    context.bindService(new Intent(context, ForegroundTask.class), new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (ForegroundTask.LocalBinder) service;
        if (foregroundCount.get() > 0 && lastTaskName != null && lastChannelId != null) {
          start(binder, lastTaskName, lastChannelId);
        }
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        assert binder != null;
        binder.getService().stopForeground(true);
        binder = null;
      }
    }, Context.BIND_AUTO_CREATE);
  }

  public void startTask(@NonNull String task) {
    startTask(task, NotificationChannels.OTHER);
  }

  public void startTask(@NonNull String task, @NonNull String channelId) {
    lastTaskName  = task;
    lastChannelId = channelId;

    if (foregroundCount.getAndIncrement() == 0 && binder != null && !ApplicationContext.getInstance(context).isAppVisible()) {
      start(binder, task, channelId);
    }
  }

  public void stopTask() {
    if (foregroundCount.decrementAndGet() == 0 && binder != null) {
      stop(binder);
    }
  }

  private void start(@NonNull ForegroundTask.LocalBinder binder, @NonNull String task, @NonNull String channelId) {
    Log.e("SPIDERMAN", "start");
    binder.getService().startForeground(task, channelId);
  }

  private void stop(@NonNull ForegroundTask.LocalBinder binder) {
    Log.e("SPIDERMAN", "stop");
    binder.getService().stopForeground(true);
    binder.getService().stopSelf();
  }


  public static class ForegroundTask extends Service {

    private final LocalBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
      return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      return START_NOT_STICKY;
    }

    void startForeground(@NonNull String title, @NonNull String channelId) {
      startForeground(NOTIFICATION_ID, new NotificationCompat.Builder(this, channelId)
          .setSmallIcon(R.drawable.ic_signal_grey_24dp)
          .setContentTitle(title)
          .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, ConversationListActivity.class), 0))
          .build());
    }

    class LocalBinder extends Binder {
      ForegroundTask getService() {
        return ForegroundTask.this;
      }
    }
  }
}
