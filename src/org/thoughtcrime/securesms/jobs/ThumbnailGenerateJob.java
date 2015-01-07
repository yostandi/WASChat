package org.thoughtcrime.securesms.jobs;

import android.content.Context;
import android.util.Log;

import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.PartDatabase;
import org.thoughtcrime.securesms.dependencies.InjectableType;
import org.thoughtcrime.securesms.jobs.requirements.MasterSecretRequirement;
import org.whispersystems.jobqueue.JobParameters;

import java.io.IOException;

import javax.inject.Inject;

import ws.com.google.android.mms.MmsException;

public class ThumbnailGenerateJob extends MasterSecretJob implements InjectableType {

  private static final String TAG = ThumbnailGenerateJob.class.getSimpleName();

  @Inject transient PartDatabase partDatabase;
  private final long partId;

  public ThumbnailGenerateJob(Context context, long partId) {
    super(context, JobParameters.newBuilder()
                                .withRequirement(new MasterSecretRequirement(context))
                                .create());

    this.partId = partId;
  }

  @Override
  public void onAdded() { }

  @Override
  public void onRun(MasterSecret masterSecret) throws IOException, OutOfMemoryError, MmsException {
    Log.w(TAG, "onRun() for part " + partId);
    if (!partDatabase.markThumbnailTaskStartedIfAbsent(partId)) {
      Log.w(TAG, "thumbnail already exists for " + partId + ", not running job");
      return;
    }

    try {
      partDatabase.generatePartThumbnail(masterSecret, partId);
    } finally {
      partDatabase.markThumbnailTaskEnded(partId);
    }
  }

  @Override
  public boolean onShouldRetryThrowable(Exception exception) {
    return false;
  }

  @Override
  public void onCanceled() { }
}
