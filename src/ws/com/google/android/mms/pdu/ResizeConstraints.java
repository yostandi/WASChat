package ws.com.google.android.mms.pdu;

import org.whispersystems.libaxolotl.util.guava.Optional;

public class ResizeConstraints {

  public static class Builder {
    private Integer maxWidth;
    private Integer maxHeight;
    private Integer maxSize;

    public Builder() {}

    public void withMaxWidth(int width) {
      maxWidth = width;
    }

    public void withMaxHeight(int height) {
      maxHeight = height;
    }

    public void withMaxSize(int bytes) {
      maxSize = bytes;
    }

    public ResizeConstraints build() {
      return new ResizeConstraints(Optional.fromNullable(maxWidth),
                                   Optional.fromNullable(maxHeight),
                                   Optional.fromNullable(maxSize));
    }
  }

  public Optional<Integer> maxWidth;
  public Optional<Integer> maxHeight;
  public Optional<Integer> maxSize;

  private ResizeConstraints(Optional<Integer> maxWidth, Optional<Integer> maxHeight, Optional<Integer> maxSize) {
    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;
    this.maxSize = maxSize;
  }
}
