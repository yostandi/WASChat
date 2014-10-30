package org.thoughtcrime.securesms.mms;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.util.BitmapUtil;

import java.io.IOException;
import java.io.InputStream;

import ws.com.google.android.mms.pdu.PduPart;

public abstract class MediaConstraints {
  private static final String TAG = MediaConstraints.class.getSimpleName();

  public abstract int getImageMaxWidth();
  public abstract int getImageMaxHeight();
  public abstract int getImageMaxSize();

  public abstract int getVideoMaxSize();

  public abstract int getAudioMaxSize();

  public boolean isSatisfied(Context context, MasterSecret masterSecret, PduPart part) {
    try {
      return (part.isImage() && part.getDataSize() <= getImageMaxSize() && isWithinBounds(context, masterSecret, part.getDataUri())) ||
             (part.isAudio() && part.getDataSize() <= getAudioMaxSize()) ||
             (part.isVideo() && part.getDataSize() <= getVideoMaxSize()) ||
             (!part.isImage() && !part.isAudio() && !part.isVideo());
    } catch (IOException ioe) {
      Log.w(TAG, "Failed to determine if media's constraints are satisfied.", ioe);
      return false;
    }
  }

  public boolean isWithinBounds(Context context, MasterSecret masterSecret, Uri uri) throws IOException {
    InputStream is = PartAuthority.getPartStream(context, masterSecret, uri);
    Pair<Integer, Integer> dimensions = BitmapUtil.getDimensions(is);
    return dimensions.first  > 0 && dimensions.first  <= getImageMaxWidth() &&
           dimensions.second > 0 && dimensions.second <= getImageMaxHeight();
  }
}
