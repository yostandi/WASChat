package org.thoughtcrime.securesms.database;

import android.net.Uri;
import android.test.AndroidTestCase;

import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.jobs.ThumbnailGenerateJob;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import ws.com.google.android.mms.pdu.PduPart;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.thoughtcrime.securesms.jobs.ThumbnailGenerateJobTest.getThumbnailGenerateJob;

public class PartDatabaseTest extends AndroidTestCase {
  private static final long PART_ID = 1L;

  private PartDatabase database;
  private Set<Long>    tasks = new HashSet<>();

  @Override
  public void setUp() {
    database = mock(PartDatabase.class);
  }

  public void testThumbnailStreamExistsCase() throws Exception {
    when(database.getDataStream(any(MasterSecret.class), anyLong(), eq("thumbnail"))).thenReturn(mock(InputStream.class));
    doCallRealMethod().when(database).getThumbnailStream(any(MasterSecret.class), anyLong());

    assertNotNull(database.getThumbnailStream(null, PART_ID));

    verify(database, never()).getThumbnailGenerateJob(anyLong());
    verify(database, never()).updatePartThumbnail(any(MasterSecret.class), anyLong(), any(PduPart.class), any(InputStream.class), anyFloat());
    verify(database, never()).markThumbnailTaskStarted(eq(PART_ID));
  }

  public void testThumbnailStreamBlocksOnRunningJob() throws Exception {
    when(database.getPart(eq(PART_ID))).thenReturn(getPduPartSkeleton("x/x"));

    when(database.getDataStream(any(MasterSecret.class), anyLong(), eq("thumbnail"))).thenReturn(null);
    when(database.getThumbnailTasks()).thenReturn(tasks);

    doCallRealMethod().when(database).markThumbnailTaskStarted(anyLong());
    doCallRealMethod().when(database).markThumbnailTaskEnded(anyLong());
    doCallRealMethod().when(database).getThumbnailStream(any(MasterSecret.class), anyLong());

    final ThumbnailGenerateJob job = getThumbnailGenerateJob(getContext(), database);
    job.onAdded();
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(1000L);
          job.onRun(null);
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
    final PartDatabase database = mock(PartDatabase.class);
    final Set<Long>    tasks    = new HashSet<>();

    when(database.getPart(PART_ID)).thenReturn(getPduPartSkeleton("x/x"));

    when(database.getThumbnailGenerateJob(anyLong())).thenReturn(getThumbnailGenerateJob(getContext(), database));
    when(database.getDataStream(any(MasterSecret.class), anyLong(), eq("thumbnail"))).thenReturn(null);
    when(database.getThumbnailTasks()).thenReturn(tasks);

    doCallRealMethod().when(database).markThumbnailTaskStarted(anyLong());
    doCallRealMethod().when(database).markThumbnailTaskEnded(anyLong());
    doCallRealMethod().when(database).getThumbnailStream(any(MasterSecret.class), anyLong());

    database.getThumbnailStream(null, PART_ID);

    verify(database, times(3)).getDataStream(any(MasterSecret.class), anyLong(), eq("thumbnail"));
    verify(database, times(1)).getThumbnailGenerateJob(anyLong());
    verify(database, times(1)).markThumbnailTaskStarted(eq(PART_ID));
    verify(database, times(1)).markThumbnailTaskEnded(eq(PART_ID));
  }

  private PduPart getPduPartSkeleton(String contentType) {
    PduPart part = new PduPart();
    part.setContentType(contentType.getBytes());
    part.setDataUri(Uri.EMPTY);
    return part;
  }
}
