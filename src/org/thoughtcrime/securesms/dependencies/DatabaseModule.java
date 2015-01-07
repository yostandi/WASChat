package org.thoughtcrime.securesms.dependencies;

import android.content.Context;

import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.PartDatabase;
import org.thoughtcrime.securesms.jobs.ThumbnailGenerateJob;

import dagger.Module;
import dagger.Provides;

@Module(injects = {ThumbnailGenerateJob.class})
public class DatabaseModule {

  private final Context context;

  public DatabaseModule(Context context) {
    this.context = context;
  }

  @Provides PartDatabase providePartDatabase() {
    return DatabaseFactory.getPartDatabase(context);
  }
}
