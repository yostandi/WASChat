package org.thoughtcrime.securesms.jobs;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.PartDatabase;
import org.thoughtcrime.securesms.dependencies.InjectableType;
import org.thoughtcrime.securesms.jobs.requirements.MasterSecretRequirement;
import org.thoughtcrime.securesms.util.BitmapDecodingException;
import org.thoughtcrime.securesms.util.MediaUtil;
import org.thoughtcrime.securesms.util.MediaUtil.ThumbnailData;
import org.thoughtcrime.securesms.util.Util;
import org.whispersystems.jobqueue.JobParameters;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import ws.com.google.android.mms.MmsException;
import ws.com.google.android.mms.pdu.PduPart;

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
    Pair<InputStream, Boolean> streamAndLock = partDatabase.getThumbnailStreamOrLock(masterSecret, partId, false);

    if (!streamAndLock.second) {
      Log.w(TAG, "thumbnail already exists for " + partId + ", not running job");
      return;
    }

    try {
      Log.w(TAG, "generating part thumbnail");
      PduPart part = partDatabase.getPart(partId);

      ThumbnailData data = MediaUtil.generateThumbnail(context, masterSecret, part.getDataUri(), Util.toIsoString(part.getContentType()));
      partDatabase.updatePartThumbnail(masterSecret, partId, part, data.toDataStream(), data.getAspectRatio());
    } catch (BitmapDecodingException bde) {
      throw new IOException(bde);
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
