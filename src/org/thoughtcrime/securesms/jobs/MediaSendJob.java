package org.thoughtcrime.securesms.jobs;

import android.content.Context;
import android.util.Log;

import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.mms.MediaConstraints;
import org.thoughtcrime.securesms.transport.UndeliverableMessageException;
import org.whispersystems.jobqueue.JobParameters;

import java.io.IOException;

import ws.com.google.android.mms.pdu.PduPart;
import ws.com.google.android.mms.pdu.SendReq;

public abstract class MediaSendJob extends MasterSecretJob {
  private final static String TAG = MediaSendJob.class.getSimpleName();

  protected MediaSendJob(Context context, JobParameters parameters) {
    super(context, parameters);
  }

  protected abstract MediaConstraints getMediaConstraints();

  protected void prepareMessageMedia(MasterSecret masterSecret, SendReq message) throws IOException, UndeliverableMessageException {
    for (int i=0;i<message.getBody().getPartsNum();i++) {
      final PduPart part = message.getBody().getPart(i);
      Log.w(TAG, "Sending MMS part of content-type: " + new String(part.getContentType()));
      if (!getMediaConstraints().isSatisfied(context, masterSecret, part)) {
        if (part.canResize()) {
          final long oldSize = part.getDataSize();
          part.resize(context, masterSecret, getMediaConstraints());
          Log.w(TAG, String.format("Resized part %.1fkb => %.1fkb", oldSize/1024.0, part.getDataSize()/1024.0));
        } else {
          throw new UndeliverableMessageException("Size constraints could not be satisfied.");
        }
      }
    }
  }
}
