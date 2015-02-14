package org.thoughtcrime.securesms.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import org.thoughtcrime.securesms.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OutgoingConversationBubble extends ConversationBubble {
  private static final boolean[] CORNERS_MESSAGE_CAPTIONED = new boolean[]{true, false, true,  true };
  private static final boolean[] CORNERS_MEDIA_CAPTIONED   = new boolean[]{true, true,  false, true};
  private static final boolean[] CORNERS_ROUNDED           = new boolean[]{true, true,  true,  true };

  private static final int TRANSPORT_STYLE_ATTRIBUTES[] = new int[]{R.attr.conversation_item_sent_push_background,
                                                                    R.attr.conversation_item_sent_background,
                                                                    R.attr.conversation_item_sent_pending_background,
                                                                    R.attr.conversation_item_sent_push_pending_background};

  private static final Map<Integer, Integer> TRANSPORT_STYLE_MAP;
  static {
    Map<Integer, Integer> styleMap = new HashMap<>(TRANSPORT_STYLE_ATTRIBUTES.length);
    styleMap.put(TRANSPORT_STATE_PUSH_SENT, 0);
    styleMap.put(TRANSPORT_STATE_SMS_SENT, 1);
    styleMap.put(TRANSPORT_STATE_SMS_PENDING, 2);
    styleMap.put(TRANSPORT_STATE_PUSH_PENDING, 3);
    TRANSPORT_STYLE_MAP = Collections.unmodifiableMap(styleMap);
  }

  private TypedArray styledDrawables;

  @SuppressWarnings("UnusedDeclaration")
  public OutgoingConversationBubble(Context context) {
    super(context);
  }

  @SuppressWarnings("UnusedDeclaration")
  public OutgoingConversationBubble(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @SuppressWarnings("UnusedDeclaration")
  public OutgoingConversationBubble(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @SuppressWarnings("UnusedDeclaration")
  public OutgoingConversationBubble(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onCreateView() {
    LayoutInflater inflater = LayoutInflater.from(getContext());
    inflater.inflate(R.layout.conversation_bubble_outgoing, this, true);
  }

  @Override
  protected void initialize() {
    super.initialize();
    styledDrawables = getContext().obtainStyledAttributes(TRANSPORT_STYLE_ATTRIBUTES);
  }

  @Override
  protected int getForegroundColor(@TransportState int transportState) {
    return styledDrawables.getColor(TRANSPORT_STYLE_MAP.get(transportState), -1);
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
