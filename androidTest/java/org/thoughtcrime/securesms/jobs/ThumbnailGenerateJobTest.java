package org.thoughtcrime.securesms.jobs;

import android.content.Context;
import android.net.Uri;
import android.test.AndroidTestCase;

import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.PartDatabase;

import java.io.FileNotFoundException;
import java.io.InputStream;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import ws.com.google.android.mms.pdu.PduPart;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ThumbnailGenerateJobTest extends AndroidTestCase {
  private static final long PART_ID = 1L;

  private PartDatabase database;

  @Override
  public void setUp() {
    database = mock(PartDatabase.class);
  }

  public void testTaskAddedRemoved() throws Exception {
    ThumbnailGenerateJob job = getThumbnailGenerateJob(getContext(), database);
    when(database.getPart(PART_ID)).thenReturn(getPduPartSkeleton("x/x"));

    job.onAdded();
    verify(database).markThumbnailTaskStarted(eq(PART_ID));

    job.onRun(null);
    verify(database, never()).updatePartThumbnail(any(MasterSecret.class), anyLong(), any(PduPart.class), any(InputStream.class), anyFloat());
    verify(database).markThumbnailTaskEnded(eq(PART_ID));
  }

  public void testTaskResizesImage() throws Exception {
    ThumbnailGenerateJob job = getThumbnailGenerateJob(getContext(), database);
    when(database.getPart(PART_ID)).thenReturn(getPduPartSkeleton("image/png"));

    job.onAdded();
    verify(database).markThumbnailTaskStarted(eq(PART_ID));

    try {
      job.onRun(null);
      throw new AssertionError("should have thrown FNFE as it tried to resize an image");
    } catch (FileNotFoundException fnfe) {
      // success
    }
    verify(database).markThumbnailTaskEnded(eq(PART_ID));
  }

  private PduPart getPduPartSkeleton(String contentType) {
    PduPart part = new PduPart();
    part.setContentType(contentType.getBytes());
    part.setDataUri(Uri.EMPTY);
    return part;
  }

  public static ThumbnailGenerateJob getThumbnailGenerateJob(Context context, PartDatabase database) {
    ThumbnailGenerateJob job = new ThumbnailGenerateJob(context, PART_ID);
    ObjectGraph objectGraph = ObjectGraph.create(new ThumbnailGenerateJobTest.DatabaseModule(database));
    objectGraph.inject(job);
    return job;
  }

  @SuppressWarnings("unused")
  @Module(injects = ThumbnailGenerateJob.class)
  public static class DatabaseModule {

    private final PartDatabase partDatabase;

    public DatabaseModule(PartDatabase partDatabase) {
      this.partDatabase = partDatabase;
    }

    @Provides PartDatabase providePartDatabase() {
      return partDatabase;
    }
  }

}
