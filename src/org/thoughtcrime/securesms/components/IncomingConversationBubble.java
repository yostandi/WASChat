package org.thoughtcrime.securesms.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;

import org.thoughtcrime.securesms.R;

public class IncomingConversationBubble extends ConversationBubble {
  private static final String TAG = IncomingConversationBubble.class.getSimpleName();

  private static final boolean[] CORNERS_MESSAGE_CAPTIONED = new boolean[]{false, true, true, true };
  private static final boolean[] CORNERS_MEDIA_CAPTIONED   = new boolean[]{true,  true, true, false};
  private static final boolean[] CORNERS_ROUNDED           = new boolean[]{true,  true, true, true };

  @SuppressWarnings("UnusedDeclaration")
  public IncomingConversationBubble(Context context) {
    super(context);
  }

  @SuppressWarnings("UnusedDeclaration")
  public IncomingConversationBubble(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @SuppressWarnings("UnusedDeclaration")
  public IncomingConversationBubble(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @SuppressWarnings("UnusedDeclaration")
  public IncomingConversationBubble(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onCreateView() {
    Log.w(TAG, "onCreateView()");
    LayoutInflater inflater = LayoutInflater.from(getContext());
    inflater.inflate(R.layout.conversation_bubble_incoming, this, true);
  }

  @Override
  protected int getForegroundColor(@TransportState int transportState) {
    return getThemedColor(getContext(), R.attr.conversation_item_received_background);
  }

  @Override
  protected boolean[] getMessageCorners(@MediaState int mediaState) {
    return mediaState == MEDIA_STATE_CAPTIONED ? CORNERS_MESSAGE_CAPTIONED : CORNERS_ROUNDED;
  }

  @Override
  protected boolean[] getMediaCorners(@MediaState int mediaState) {
    return mediaState == MEDIA_STATE_CAPTIONED ? CORNERS_MEDIA_CAPTIONED : CORNERS_ROUNDED;
  }
}
