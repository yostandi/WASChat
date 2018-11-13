package org.thoughtcrime.securesms.database;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.thoughtcrime.securesms.database.helpers.SQLCipherOpenHelper;
import org.whispersystems.libsignal.util.guava.Optional;

import java.util.UUID;

import androidx.work.Worker;

public class WorkResultDatabase extends Database {

  public static final String TABLE_NAME = "work_result";

  private static final String ID        = "_id";
  private static final String WORK_ID   = "work_id";
  private static final String RESULT    = "result";
  private static final String TIMESTAMP = "timestamp";

  public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
      ID        + " INTEGER PRIMARY KEY, " +
      WORK_ID   + " TEXT UNIQUE, " +
      RESULT    + " INTEGER, " +
      TIMESTAMP + " INTEGER);";

  public WorkResultDatabase(@NonNull Context context, SQLCipherOpenHelper databaseHelper) {
    super(context, databaseHelper);
  }

  public Optional<Worker.Result> getResult(@NonNull UUID workId) {
    SQLiteDatabase db = databaseHelper.getReadableDatabase();
    try (Cursor cursor = db.query(TABLE_NAME, new String[] { RESULT }, WORK_ID + " = ?", new String[] { workId.toString() }, null, null, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        int result = cursor.getInt(cursor.getColumnIndexOrThrow(RESULT));
        return Optional.fromNullable(deserializeResult(result));
      }
      return Optional.absent();
    }
  }

  public void saveResult(@NonNull UUID workId, @NonNull Worker.Result result, long timestamp) {
    ContentValues contentValues = new ContentValues(3);
    contentValues.put(WORK_ID, workId.toString());
    contentValues.put(RESULT, serializeResult(result));
    contentValues.put(TIMESTAMP, timestamp);

    SQLiteDatabase db = databaseHelper.getReadableDatabase();
    db.insert(TABLE_NAME, null, contentValues);
  }

  public void removeResult(@NonNull UUID workId) {
    SQLiteDatabase db = databaseHelper.getReadableDatabase();
    db.delete(TABLE_NAME, WORK_ID + " = ?", new String[] { workId.toString() });
  }

  public void trim(long cutoffTimestamp) {
    SQLiteDatabase db = databaseHelper.getReadableDatabase();
    db.delete(TABLE_NAME, TIMESTAMP + " < ?", new String[] { String.valueOf(cutoffTimestamp)});
  }

  private @Nullable Worker.Result deserializeResult(int value) {
    switch (value) {
      case 1:  return Worker.Result.SUCCESS;
      case 2:  return Worker.Result.RETRY;
      case 3:  return Worker.Result.FAILURE;
      default: return null;
    }
  }

  private int serializeResult(@NonNull Worker.Result result) {
    switch (result) {
      case SUCCESS: return 1;
      case RETRY:   return 2;
      case FAILURE: return 3;
      default:      throw new IllegalArgumentException("Invalid result.");
    }
  }

}
