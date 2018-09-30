package org.thoughtcrime.securesms.jobs;

import android.content.Context;

import org.thoughtcrime.securesms.jobmanager.Job;
import org.thoughtcrime.securesms.jobmanager.JobParameters;
import org.thoughtcrime.securesms.jobmanager.dependencies.ContextDependent;

public abstract class ContextJob extends Job implements ContextDependent {

  private static final long serialVersionUID = 504820803255161092L;

  protected transient Context context;

  protected ContextJob(Context context) {
    this.context = context;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  protected Context getContext() {
    return context;
  }
}
