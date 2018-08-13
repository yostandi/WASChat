package org.thoughtcrime.securesms.util;

import android.arch.lifecycle.DefaultLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;

/**
 * By default, upon subscribing to a lifecycle, you'll get "catchup" events for every part of the
 * lifcycle that has already been seen before you subscribed. For example, if the state is
 * {@link Lifecycle.State.RESUMED} when you subscribed, you'll immediate get events for
 * {@link Lifecycle.State.CREATED} and {@link Lifecycle.State.RESUMED}. This isn't always desireable,
 * so this observer has been created to ignore those catchup events and only notify the subscriber
 * of new changes.
 */
public class NoCatchupLifecycleObserver implements DefaultLifecycleObserver {

  private final Lifecycle.State          initialState;
  private final DefaultLifecycleObserver observer;

  private boolean caughtUp = false;

  public NoCatchupLifecycleObserver(@NonNull Lifecycle lifecycle, @NonNull DefaultLifecycleObserver observer) {
    this.initialState = lifecycle.getCurrentState();
    this.observer     = observer;
  }

  @Override
  public void onCreate(@NonNull LifecycleOwner owner) {
    if (initialState == Lifecycle.State.CREATED) {
      caughtUp = true;
      return;
    }

    if (caughtUp) {
      observer.onCreate(owner);
    }
  }

  @Override
  public void onStart(@NonNull LifecycleOwner owner) {
    if (initialState == Lifecycle.State.STARTED) {
      caughtUp = true;
      return;
    }

    if (caughtUp) {
      observer.onStart(owner);
    }
  }

  @Override
  public void onResume(@NonNull LifecycleOwner owner) {
    if (initialState == Lifecycle.State.RESUMED) {
      caughtUp = true;
      return;
    }

    if (caughtUp) {
      observer.onResume(owner);
    }
  }

  @Override
  public void onPause(@NonNull LifecycleOwner owner) {
    if (initialState == Lifecycle.State.RESUMED) {
      caughtUp = true;
      return;
    }

    if (caughtUp) {
      observer.onPause(owner);
    }
  }

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    if (initialState == Lifecycle.State.CREATED) {
      caughtUp = true;
      return;
    }

    if (caughtUp) {
      observer.onStop(owner);
    }
  }

  @Override
  public void onDestroy(@NonNull LifecycleOwner owner) {
    if (initialState == Lifecycle.State.DESTROYED) {
      caughtUp = true;
      return;
    }

    if (caughtUp) {
      observer.onDestroy(owner);
    }
  }
}
