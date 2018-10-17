package org.thoughtcrime.securesms.service;

import android.app.Service;
import android.arch.lifecycle.DefaultLifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.thoughtcrime.securesms.logging.Log;

import org.thoughtcrime.securesms.ApplicationContext;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.dependencies.InjectableType;
import org.thoughtcrime.securesms.jobmanager.requirements.NetworkRequirement;
import org.thoughtcrime.securesms.jobmanager.requirements.NetworkRequirementProvider;
import org.thoughtcrime.securesms.jobmanager.requirements.RequirementListener;
import org.thoughtcrime.securesms.jobs.PushContentReceiveJob;
import org.thoughtcrime.securesms.notifications.NotificationChannels;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.signalservice.api.SignalServiceMessagePipe;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

public class MessageRetrievalService extends Service implements InjectableType, RequirementListener {

  private static final String TAG = MessageRetrievalService.class.getSimpleName();

  public static final  String ACTION_ACTIVITY_STARTED  = "ACTIVITY_STARTED";
  public static final  String ACTION_INITIALIZE        = "INITIALIZE";
  public static final  int    FOREGROUND_ID            = 313399;

  private static final long   REQUEST_TIMEOUT_MINUTES  = 1;

  private NetworkRequirement         networkRequirement;
  private NetworkRequirementProvider networkRequirementProvider;

  @Inject
  public SignalServiceMessageReceiver receiver;

  private boolean                isAppForeground = false;
  private MessageRetrievalThread retrievalThread = null;


  public static SignalServiceMessagePipe pipe = null;

  @Override
  public void onCreate() {
    super.onCreate();
    ApplicationContext.getInstance(this).injectDependencies(this);

    networkRequirement         = new NetworkRequirement(this);
    networkRequirementProvider = new NetworkRequirementProvider(this);

    networkRequirementProvider.setListener(this);

    retrievalThread = new MessageRetrievalThread();
    retrievalThread.start();

    setForegroundIfNecessary();

    ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
      @Override
      public void onStart(@NonNull LifecycleOwner owner) {
        onAppForegrounded();
      }

      @Override
      public void onStop(@NonNull LifecycleOwner owner) {
        onAppBackgrounded();
      }
    });
  }

  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (retrievalThread != null) {
      retrievalThread.stopThread();
    }

    sendBroadcast(new Intent("org.thoughtcrime.securesms.RESTART"));
  }

  @Override
  public void onRequirementStatusChanged() {
    synchronized (this) {
      notifyAll();
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private void setForegroundIfNecessary() {
    if (TextSecurePreferences.isGcmDisabled(this)) {
      NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannels.OTHER);
      builder.setContentTitle(getString(R.string.MessageRetrievalService_signal));
      builder.setContentText(getString(R.string.MessageRetrievalService_background_connection_enabled));
      builder.setPriority(NotificationCompat.PRIORITY_MIN);
      builder.setWhen(0);
      builder.setSmallIcon(R.drawable.ic_signal_grey_24dp);
      startForeground(FOREGROUND_ID, builder.build());
    }
  }

  private synchronized void onAppForegrounded() {
    Log.d(TAG, "App foregrounded.");
    isAppForeground = true;
    notifyAll();
  }

  private synchronized void onAppBackgrounded() {
    Log.d(TAG, "App backgrounded.");
    isAppForeground = false;
    notifyAll();
  }

  private synchronized boolean isConnectionNecessary() {
    boolean isGcmDisabled = TextSecurePreferences.isGcmDisabled(this);

    Log.d(TAG, String.format("Network requirement: %s, app in foreground: %s, gcm disabled: %b",
                             networkRequirement.isPresent(), isAppForeground, isGcmDisabled));

    return TextSecurePreferences.isPushRegistered(this)      &&
           TextSecurePreferences.isWebsocketRegistered(this) &&
           (isAppForeground || isGcmDisabled)                &&
           networkRequirement.isPresent();
  }

  private synchronized void waitForConnectionNecessary() {
    try {
      while (!isConnectionNecessary()) wait();
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  private void shutdown(SignalServiceMessagePipe pipe) {
    try {
      pipe.shutdown();
    } catch (Throwable t) {
      Log.w(TAG, t);
    }
  }

  public static void registerActivityStarted(Context activity) {
    Intent intent = new Intent(activity, MessageRetrievalService.class);
    intent.setAction(MessageRetrievalService.ACTION_ACTIVITY_STARTED);
    activity.startService(intent);
  }

  public static @Nullable SignalServiceMessagePipe getPipe() {
    return pipe;
  }

  private class MessageRetrievalThread extends Thread implements Thread.UncaughtExceptionHandler {

    private AtomicBoolean stopThread = new AtomicBoolean(false);

    MessageRetrievalThread() {
      super("MessageRetrievalService");
      setUncaughtExceptionHandler(this);
    }

    @Override
    public void run() {
      while (!stopThread.get()) {
        Log.i(TAG, "Waiting for websocket state change....");
        waitForConnectionNecessary();

        Log.i(TAG, "Making websocket connection....");
        pipe = receiver.createMessagePipe();

        SignalServiceMessagePipe localPipe = pipe;

        try {
          while (isConnectionNecessary() && !stopThread.get()) {
            try {
              Log.i(TAG, "Reading message...");
              localPipe.read(REQUEST_TIMEOUT_MINUTES, TimeUnit.MINUTES,
                             envelope -> {
                               Log.i(TAG, "Retrieved envelope! " + envelope.getSource());
                               new PushContentReceiveJob(getApplicationContext()).processEnvelope(envelope);
                             });
            } catch (TimeoutException e) {
              Log.w(TAG, "Application level read timeout...");
            } catch (InvalidVersionException e) {
              Log.w(TAG, e);
            }
          }
        } catch (Throwable e) {
          Log.w(TAG, e);
        } finally {
          Log.w(TAG, "Shutting down pipe...");
          shutdown(localPipe);
        }

        Log.i(TAG, "Looping...");
      }

      Log.i(TAG, "Exiting...");
    }

    private void stopThread() {
      stopThread.set(true);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
      Log.w(TAG, "*** Uncaught exception!");
      Log.w(TAG, e);
    }
  }
}
