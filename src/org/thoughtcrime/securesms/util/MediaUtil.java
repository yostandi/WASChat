package org.thoughtcrime.securesms.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.crypto.MasterSecret;

import java.io.FileNotFoundException;

import ws.com.google.android.mms.ContentType;

public class MediaUtil {
  private static final String TAG = MediaUtil.class.getSimpleName();

  public static Bitmap generateThumbnail(Context context, MasterSecret masterSecret, Uri uri, String type)
      throws FileNotFoundException, BitmapDecodingException, OutOfMemoryError
  {
    if      (ContentType.isImageType(type)) return generateImageThumbnail(context, masterSecret, uri);
    else                                    return null;
  }

  private static Bitmap generateImageThumbnail(Context context, MasterSecret masterSecret, Uri uri)
      throws FileNotFoundException, BitmapDecodingException, OutOfMemoryError
  {
    int maxSize = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_max_size);
    return BitmapUtil.createScaledBitmap(context, masterSecret, uri, maxSize, maxSize);
  }

}
