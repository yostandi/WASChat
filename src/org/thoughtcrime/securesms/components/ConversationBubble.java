package org.thoughtcrime.securesms.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Build.VERSION_CODES;
import android.support.annotation.AttrRes;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import org.thoughtcrime.securesms.R;

public abstract class ConversationBubble extends RelativeLayout {
  private static final String TAG = ConversationBubble.class.getSimpleName();

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

  private View       conversationParent;
  private View       triangleTick;
  private View       mmsContainer;
  private TypedArray transportDrawables;
  private int        shadowColor;

  private LayerDrawable    messageParentDrawable;
  private GradientDrawable tickDrawable;
  private GradientDrawable messageDrawable;
  private GradientDrawable shadowDrawable;
  private GradientDrawable mmsContainerDrawable;

  public ConversationBubble(Context context) {
    super(context);
    initialize();
  }

  public ConversationBubble(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public ConversationBubble(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  public ConversationBubble(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize();
  }

  protected abstract void onCreateView();

  protected abstract int getForegroundColor(@TransportState int transportState);

  protected abstract boolean[] getMessageCorners(@MediaState int mediaState);
  protected abstract boolean[] getMediaCorners(@MediaState int mediaState);

  protected void initialize() {
    onCreateView();
    this.conversationParent = findViewById(R.id.conversation_item_parent);
    this.triangleTick       = findViewById(R.id.triangle_tick);
    this.mmsContainer       = findViewById(R.id.mms_view);

    this.shadowColor        = getThemedColor(getContext(), R.attr.conversation_item_shadow);
  }

  protected void getDrawables() {
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
  }

  public void setTransportState(@TransportState int transportState) {
    getDrawables();
    setColors(transportState);
  }

  public void setMediaState(@MediaState int mediaState) {
    getDrawables();
    setCorners(mediaState);
    setAlignment(mediaState == MEDIA_STATE_CAPTIONED);
    setShadowDistance(mediaState == MEDIA_STATE_CAPTIONED || mediaState == MEDIA_STATE_NO_MEDIA
                      ? getContext().getResources().getDimensionPixelSize(R.dimen.conversation_item_drop_shadow_dist)
                      : 0);
    setMediaVisibility(mediaState == MEDIA_STATE_CAPTIONED || mediaState == MEDIA_STATE_CAPTIONLESS);
  }

  private void setMediaVisibility(boolean visible) {
    mmsContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
  }

  private void setColors(@TransportState int transportState) {
    final int foregroundColor = getForegroundColor(transportState);
    Log.w(TAG, String.format("setting foreground to #%08X", foregroundColor));
    messageDrawable.setColor(foregroundColor);
    tickDrawable.setColor(foregroundColor);
    mmsContainerDrawable.setColor(foregroundColor);
    shadowDrawable.setColor(shadowColor);
  }

  private void setCorners(@MediaState int mediaState) {
    int radius = getContext().getResources().getDimensionPixelSize(R.dimen.conversation_item_corner_radius);

    final boolean[] messageCorners = getMessageCorners(mediaState);
    final boolean[] mediaCorners   = getMediaCorners(mediaState);

    messageDrawable.setCornerRadii(cornerBooleansToRadii(messageCorners, radius));
    mmsContainerDrawable.setCornerRadii(cornerBooleansToRadii(mediaCorners, radius));
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

  protected static int getThemedColor(Context context, @AttrRes int attr) {
    final TypedArray styledShadow = context.obtainStyledAttributes(new int[]{attr});
    final int        result       = styledShadow.getColor(0, -1);
    styledShadow.recycle();
    return result;
  }

  private static float[] cornerBooleansToRadii(boolean[] corners, int radius) {
    if (corners == null || corners.length != 4) {
      throw new AssertionError("there are four corners in a rectangle, silly");
    }

    float[] radii = new float[8];
    int     i     = 0;

    for (boolean corner : corners) {
      radii[i] = radii[i+1] = corner ? radius : 0;
      i += 2;
    }

    return radii;
  }

}