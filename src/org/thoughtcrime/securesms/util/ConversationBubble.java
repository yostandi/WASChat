package org.thoughtcrime.securesms.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RotateDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import org.thoughtcrime.securesms.R;

public class ConversationBubble {
  private static final String TAG = ConversationBubble.class.getSimpleName();

  private final static int TRANSPORT_STYLE_ATTRIBUTES[] = new int[]{R.attr.conversation_item_sent_push_background,
                                                                    R.attr.conversation_item_sent_background,
                                                                    R.attr.conversation_item_sent_pending_background,
                                                                    R.attr.conversation_item_sent_push_pending_background};

  public static final int TRANSPORT_STATE_PUSH_SENT    = 0;
  public static final int TRANSPORT_STATE_SMS_SENT     = 1;
  public static final int TRANSPORT_STATE_SMS_PENDING  = 2;
  public static final int TRANSPORT_STATE_PUSH_PENDING = 3;

  public static final int MEDIA_STATE_NO_MEDIA    = 0;
  public static final int MEDIA_STATE_CAPTIONLESS = 1;
  public static final int MEDIA_STATE_CAPTIONED   = 2;

  @IntDef({TRANSPORT_STATE_PUSH_SENT, TRANSPORT_STATE_PUSH_PENDING, TRANSPORT_STATE_SMS_SENT, TRANSPORT_STATE_SMS_PENDING})
  public @interface TransportState {}

  @IntDef({MEDIA_STATE_NO_MEDIA, MEDIA_STATE_CAPTIONLESS, MEDIA_STATE_CAPTIONED})
  public @interface MediaState {}

  private final Context    context;
  private final boolean    outgoing;
  private final View       conversationParent;
  private final View       triangleTick;
  private final View       mmsContainer;
  private final TypedArray transportDrawables;
  private final int        shadowColor;

  private final LayerDrawable    messageParentDrawable;
  private final GradientDrawable tickDrawable;
  private final GradientDrawable messageDrawable;
  private final GradientDrawable shadowDrawable;
  private final GradientDrawable mmsContainerDrawable;

  private ConversationBubble(Context context, boolean outgoing,
                             View conversationParent, View triangleTick, View mmsContainer)
  {
    long startTime = System.currentTimeMillis();
    this.context            = context;
    this.outgoing           = outgoing;
    this.conversationParent = conversationParent;
    this.triangleTick       = triangleTick;
    this.mmsContainer       = mmsContainer;

    this.transportDrawables = context.obtainStyledAttributes(TRANSPORT_STYLE_ATTRIBUTES);
    this.shadowColor        = getThemedColor(context, R.attr.conversation_item_shadow);

    this.messageParentDrawable = (LayerDrawable   ) conversationParent.getBackground();
    this.tickDrawable          = (GradientDrawable) ((RotateDrawable)triangleTick.getBackground()).getDrawable();
    this.messageDrawable       = (GradientDrawable) messageParentDrawable.getDrawable(1);
    this.shadowDrawable        = (GradientDrawable) messageParentDrawable.getDrawable(0);
    this.mmsContainerDrawable  = (GradientDrawable) mmsContainer.getBackground();

    this.messageParentDrawable.mutate();
    this.messageDrawable.mutate();
    this.shadowDrawable.mutate();
    this.tickDrawable.mutate();
    this.mmsContainerDrawable.mutate();
    Log.w(TAG, "construction took " + (System.currentTimeMillis() - startTime) + "ms");
  }

  public static ConversationBubble from(Context context, boolean outgoing,
                                        View conversationParent, View triangleTick, View mmsContainer)
  {
    return new ConversationBubble(context, outgoing, conversationParent, triangleTick, mmsContainer);
  }

  public void setTransportState(@TransportState int transportState) {
    if (outgoing) {
      setColors(transportDrawables.getColor(transportState, -1), shadowColor);
    }
  }

  public void setMediaCaptionState(@MediaState int mediaState) {
    setCorners(mediaState == MEDIA_STATE_CAPTIONED);
    setAlignment(mediaState == MEDIA_STATE_CAPTIONED);
    setShadowDistance(mediaState == MEDIA_STATE_CAPTIONED || mediaState == MEDIA_STATE_NO_MEDIA
                      ? context.getResources().getDimensionPixelSize(R.dimen.conversation_item_drop_shadow_dist)
                      : 0);
  }

  private void setColors(final int foregroundColor, final int shadowColor) {
    messageDrawable.setColor(foregroundColor);
    tickDrawable.setColor(foregroundColor);
    mmsContainerDrawable.setColor(foregroundColor);
    shadowDrawable.setColor(shadowColor);
  }

  private void setCorners(final boolean extruded) {
    int radius = context.getResources().getDimensionPixelSize(R.dimen.conversation_item_corner_radius);

    final int incomingRadius = !outgoing && extruded ? 0 : radius;
    final int outgoingRadius =  outgoing && extruded ? 0 : radius;

    messageDrawable.setCornerRadii(new float[]{incomingRadius, incomingRadius,
                                               outgoingRadius, outgoingRadius,
                                               radius, radius,
                                               radius, radius});

    mmsContainerDrawable.setCornerRadii(new float[]{radius, radius,
                                                    radius, radius,
                                                    outgoingRadius, outgoingRadius,
                                                    incomingRadius, incomingRadius});
  }

  private void setShadowDistance(final int distance) {
    messageParentDrawable.setLayerInset(1, 0, 0, 0, distance);
  }

  private void setAlignment(final boolean extruded) {
    RelativeLayout.LayoutParams parentParams = (RelativeLayout.LayoutParams)conversationParent.getLayoutParams();
    if (!extruded) {
      parentParams.addRule(RelativeLayout.BELOW, 0);
      parentParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.mms_view);
    } else {
      parentParams.addRule(RelativeLayout.BELOW, R.id.mms_view);
      parentParams.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
    }
    conversationParent.setLayoutParams(parentParams);
  }

  private static int getThemedColor(Context context, @AttrRes int attr) {
    final TypedArray styledShadow = context.obtainStyledAttributes(new int[]{attr});
    final int result = styledShadow.getColor(0, -1);
    styledShadow.recycle();
    return result;
  }
}