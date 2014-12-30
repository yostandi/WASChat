package org.thoughtcrime.securesms.jobs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.PartDatabase;
import org.thoughtcrime.securesms.jobs.requirements.MasterSecretRequirement;
import org.thoughtcrime.securesms.util.BitmapDecodingException;
import org.thoughtcrime.securesms.util.BitmapUtil;
import org.thoughtcrime.securesms.util.Util;
import org.whispersystems.jobqueue.JobParameters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import ws.com.google.android.mms.ContentType;
import ws.com.google.android.mms.MmsException;
import ws.com.google.android.mms.pdu.PduPart;

public class ThumbnailGenerateJob extends MasterSecretJob {

  private static final String TAG = ThumbnailGenerateJob.class.getSimpleName();

  private final long partId;

  public ThumbnailGenerateJob(Context context, long partId) {
    super(context, JobParameters.newBuilder()
                                .withRequirement(new MasterSecretRequirement(context))
                                .create());

    this.partId = partId;
  }

  @Override
  public void onAdded() {
    DatabaseFactory.getPartDatabase(context).markThumbnailTaskStarted(partId);
  }

  @Override
  public void onRun(MasterSecret masterSecret) throws IOException, OutOfMemoryError, MmsException {
    PartDatabase database = DatabaseFactory.getPartDatabase(context);
    try {
      PduPart part        = database.getPart(partId);
      long    startMillis = System.currentTimeMillis();
      Bitmap  thumbnail   = generateThumbnailForPart(masterSecret, part);

      if (thumbnail != null) {
        ByteArrayOutputStream thumbnailBytes = new ByteArrayOutputStream();
        thumbnail.compress(CompressFormat.JPEG, 85, thumbnailBytes);

        float aspectRatio = (float) thumbnail.getWidth() / (float) thumbnail.getHeight();
        Log.w(TAG, String.format("generated thumbnail for part #%d, %dx%d (%.3f:1) in %dms",
                                 partId,
                                 thumbnail.getWidth(),
                                 thumbnail.getHeight(),
                                 aspectRatio, System.currentTimeMillis() - startMillis));

        thumbnail.recycle();

        database.updatePartThumbnail(masterSecret, partId, part, new ByteArrayInputStream(thumbnailBytes.toByteArray()), aspectRatio);
      } else {
        Log.w(TAG, "media doesn't have thumbnail support, skipping.");
      }
    } catch (BitmapDecodingException bde) {
      throw new IOException(bde);
    } finally {
      database.markThumbnailTaskEnded(partId);
    }
  }

  private Bitmap generateThumbnailForPart(MasterSecret masterSecret, PduPart part)
      throws FileNotFoundException, BitmapDecodingException, OutOfMemoryError
  {
    String contentType = Util.toIsoString(part.getContentType());

    if      (ContentType.isImageType(contentType)) return generateImageThumbnail(masterSecret, part);
    else                                           return null;
  }

  private Bitmap generateImageThumbnail(MasterSecret masterSecret, PduPart part)
      throws FileNotFoundException, BitmapDecodingException, OutOfMemoryError
  {
    int maxSize = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_max_size);
    return BitmapUtil.createScaledBitmap(context, masterSecret, part.getDataUri(), maxSize, maxSize);
  }

  @Override
  public boolean onShouldRetryThrowable(Exception exception) {
    return false;
  }

  @Override
  public void onCanceled() { }
}
