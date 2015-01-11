package org.thoughtcrime.securesms.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.PartDatabase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ws.com.google.android.mms.ContentType;
import ws.com.google.android.mms.MmsException;
import ws.com.google.android.mms.pdu.PduPart;

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

//  public Thumbnail generatePartThumbnail(Context context, MasterSecret masterSecret, long partId) throws IOException, MmsException {
//    PartDatabase database = DatabaseFactory.getPartDatabase(context);
//    try {
//      Log.w(TAG, "generatePartThumbnail()");
//      PduPart part        = database.getPart(partId);
//      long    startMillis = System.currentTimeMillis();
//      Bitmap  thumbnail   = MediaUtil.generateThumbnail(context, masterSecret, part.getDataUri(), Util.toIsoString(part.getContentType()));
//
//      if (thumbnail == null) {
//        Log.w(TAG, "media doesn't have thumbnail support, skipping.");
//        return;
//      }
//
//      InputStream jpegStream  = BitmapUtil.toCompressedJpeg(thumbnail);
//      float       aspectRatio = (float) thumbnail.getWidth() / (float) thumbnail.getHeight();
//
//      Log.w(TAG, String.format("generated thumbnail for part #%d, %dx%d (%.3f:1) in %dms",
//                               part.getId(), thumbnail.getWidth(), thumbnail.getHeight(),
//                               aspectRatio, System.currentTimeMillis() - startMillis));
//
//      thumbnail.recycle();
//
//      database.updatePartThumbnail(masterSecret, partId, part, jpegStream, aspectRatio);
//    } catch (BitmapDecodingException bde) {
//      throw new IOException(bde);
//    }
//  }
//
//  public static class Thumbnail {
//    Bitmap bitmap;
//    float aspectRatio;
//
//    public Thumbnail(Bitmap bitmap) {
//      this.bitmap      = bitmap;
//      this.aspectRatio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
//    }
//
//    public Bitmap getBitmap() {
//      return bitmap;
//    }
//
//    public float getAspectRatio() {
//      return aspectRatio;
//    }
//  }
}
