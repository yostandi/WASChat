package org.thoughtcrime.securesms.mms;

public class PushMediaConstraints extends MediaConstraints {
  private static final int MAX_IMAGE_DIMEN  = 2560;
  private static final int MB               = 1024 * 1024;

  @Override
  public int getImageMaxWidth() {
    return MAX_IMAGE_DIMEN;
  }

  @Override
  public int getImageMaxHeight() {
    return MAX_IMAGE_DIMEN;
  }

  @Override
  public int getImageMaxSize() {
    return 3*MB;
  }

  @Override
  public int getVideoMaxSize() {
    return 1024*MB;
  }

  @Override
  public int getAudioMaxSize() {
    return 300*MB;
  }
}
