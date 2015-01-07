package org.thoughtcrime.securesms.database;

import android.content.Context;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;

import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.jobs.ThumbnailGenerateJob;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import ws.com.google.android.mms.pdu.PduPart;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PartDatabaseTest extends InstrumentationTestCase {
  private static final long PART_ID = 1L;

  private PartDatabase database;
  private Set<Long>    tasks;

  @Override
  public void setUp() {
    database = spy(DatabaseFactory.getPartDatabase(getInstrumentation().getTargetContext()));
    tasks = spy(new HashSet<Long>());
  }

  public void testTaskNotRunWhenThumbnailExists() throws Exception {
    ThumbnailGenerateJob job = getThumbnailGenerateJob(getInstrumentation().getTargetContext(), database);
    when(database.getPart(eq(PART_ID))).thenReturn(getPduPartSkeleton("x/x"));
    doReturn(true).when(database).isThumbnailInDatabase(anyLong());

    job.onRun(null);

    verify(database, never()).generatePartThumbnail(any(MasterSecret.class), anyLong());
    verify(database, never()).updatePartThumbnail(any(MasterSecret.class), anyLong(), any(PduPart.class), any(InputStream.class), anyFloat());
    verify(database, never()).markThumbnailTaskEnded(eq(PART_ID));
  }

  public void testTaskResizesImage() throws Exception {
    ThumbnailGenerateJob job = getThumbnailGenerateJob(getInstrumentation().getTargetContext(), database);
    doReturn(getPduPartSkeleton("image/png")).when(database).getPart(PART_ID);
    doReturn(true).when(database).markThumbnailTaskStartedIfAbsent(PART_ID);

    try {
      job.onRun(null);
      throw new AssertionError("should have thrown FNFE as it tried to resize an image");
    } catch (FileNotFoundException fnfe) {
      // success
    }

    verify(database, times(1)).markThumbnailTaskStartedIfAbsent(eq(PART_ID));
    verify(database, times(1)).markThumbnailTaskEnded(eq(PART_ID));
  }

//  public void testDoubleJob() throws Exception {
//    ThumbnailGenerateJob job = getThumbnailGenerateJob(getContext(), database);
//    when(database.getPart(PART_ID)).thenReturn(getPduPartSkeleton("image/png"));
//    doReturn(false).when(database).isThumbnailInDatabase(anyLong());
//
//    try {
//      job.onRun(null);
//      throw new AssertionError("should have thrown FNFE as it tried to resize an image");
//    } catch (FileNotFoundException fnfe) {
//      // success
//    }
//
//    verify(database, times(1)).markThumbnailTaskStarted(eq(PART_ID));
//    verify(database, times(1)).markThumbnailTaskEnded(eq(PART_ID));
//  }

  public void testThumbnailStreamExistsCase() throws Exception {
    doReturn(mock(InputStream.class)).when(database).getDataStream(any(MasterSecret.class), anyLong(), eq("thumbnail"));
    doCallRealMethod().when(database).getThumbnailStream(any(MasterSecret.class), anyLong());

    assertNotNull(database.getThumbnailStream(null, PART_ID));

    verify(database, never()).getThumbnailGenerateJob(anyLong());
    verify(database, never()).updatePartThumbnail(any(MasterSecret.class), anyLong(), any(PduPart.class), any(InputStream.class), anyFloat());
    verify(database, never()).markThumbnailTaskStarted(eq(PART_ID));
  }

  public void testThumbnailStreamBlocksOnRunningJob() throws Exception {
    doReturn(getPduPartSkeleton("x/x")).when(database).getPart(PART_ID);
    doReturn(null).when(database).getDataStream(any(MasterSecret.class), anyLong(), eq("thumbnail"));

    database.markThumbnailTaskStarted(PART_ID);
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(1000L);
          database.markThumbnailTaskEnded(PART_ID);
        } catch (Exception e) {
          throw new AssertionError("interrupted");
        }
      }
    }).start();
    database.getThumbnailStream(null, 1);

    verify(database, times(3)).getDataStream(any(MasterSecret.class), anyLong(), eq("thumbnail"));
    verify(database, never()).getThumbnailGenerateJob(anyLong());
    verify(database, never()).updatePartThumbnail(any(MasterSecret.class), anyLong(), any(PduPart.class), any(InputStream.class), anyFloat());
    verify(database, times(1)).markThumbnailTaskStarted(eq(PART_ID));
    verify(database, times(1)).markThumbnailTaskEnded(eq(PART_ID));
  }

  public void testThumbnailStreamGeneratesWhenMissing() throws Exception {
    doReturn(getPduPartSkeleton("x/x")).when(database).getPart(PART_ID);
    doReturn(getThumbnailGenerateJob(getInstrumentation().getTargetContext(), database)).when(database).getThumbnailGenerateJob(anyLong());
    doReturn(null).when(database).getDataStream(any(MasterSecret.class), anyLong(), eq("thumbnail"));

    doCallRealMethod().when(database).markThumbnailTaskStarted(anyLong());
    doCallRealMethod().when(database).markThumbnailTaskEnded(anyLong());
    doCallRealMethod().when(database).getThumbnailStream(any(MasterSecret.class), anyLong());

    database.getThumbnailStream(null, PART_ID);

    verify(database, times(3)).getDataStream(any(MasterSecret.class), anyLong(), eq("thumbnail"));
    verify(database, times(1)).markThumbnailTaskStarted(eq(PART_ID));
    verify(database, times(1)).markThumbnailTaskEnded(eq(PART_ID));
  }

  private PduPart getPduPartSkeleton(String contentType) {
    PduPart part = new PduPart();
    part.setContentType(contentType.getBytes());
    part.setDataUri(Uri.EMPTY);
    return part;
  }

  public static ThumbnailGenerateJob getThumbnailGenerateJob(Context context, PartDatabase database) {
    ThumbnailGenerateJob job = new ThumbnailGenerateJob(context, PART_ID);
    ObjectGraph objectGraph = ObjectGraph.create(new DatabaseModule(database));
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

    @Provides
    PartDatabase providePartDatabase() {
      return partDatabase;
    }
  }

}
