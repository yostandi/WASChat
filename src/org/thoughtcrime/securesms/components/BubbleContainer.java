/**
 * Copyright (C) 2015 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Build.VERSION_CODES;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.ThemeUtil;

public abstract class BubbleContainer extends RelativeLayout {
  private static final String TAG = BubbleContainer.class.getSimpleName();

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

  private View                bodyBubble;
  private View                triangleTick;
  private View                mediaBubble;
  private ForegroundImageView media;
  private int                 shadowColor;
  private int                 mmsPendingOverlayColor;

  private LayerDrawable    messageParentDrawable;
  private GradientDrawable tickDrawable;
  private GradientDrawable messageDrawable;
  private GradientDrawable shadowDrawable;
  private GradientDrawable mmsContainerDrawable;

  public BubbleContainer(Context context) {
    super(context);
    initialize();
  }

  public BubbleContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public BubbleContainer(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  public BubbleContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize();
  }

  protected abstract void onCreateView();

  protected abstract int getForegroundColor(@TransportState int transportState);

  protected abstract boolean[] getMessageCorners(@MediaState int mediaState);
  protected abstract boolean[] getMediaCorners(@MediaState int mediaState);

  protected void initialize() {
    onCreateView();
    this.bodyBubble   = findViewById(R.id.body_bubble  );
    this.triangleTick = findViewById(R.id.triangle_tick);
    this.mediaBubble  = findViewById(R.id.media_bubble );
    this.media        = (ForegroundImageView) findViewById(R.id.image_view);

    this.shadowColor            = ThemeUtil.getStyledColor(getContext(), R.attr.conversation_item_shadow);
    this.mmsPendingOverlayColor = ThemeUtil.getStyledColor(getContext(), R.attr.conversation_item_mms_pending_mask);
  }

  protected void getDrawables() {
    this.messageParentDrawable = (LayerDrawable   ) bodyBubble.getBackground();
    this.tickDrawable          = (GradientDrawable) ((RotateDrawable)triangleTick.getBackground()).getDrawable();
    this.messageDrawable       = (GradientDrawable) messageParentDrawable.getDrawable(1);
    this.shadowDrawable        = (GradientDrawable) messageParentDrawable.getDrawable(0);
    this.mmsContainerDrawable  = (GradientDrawable) mediaBubble.getBackground();

    this.messageParentDrawable.mutate();
    this.messageDrawable.mutate();
    this.shadowDrawable.mutate();
    this.tickDrawable.mutate();
    this.mmsContainerDrawable.mutate();
  }

  public void setTransportState(@TransportState int transportState) {
    getDrawables();
    setColors(transportState);
    setMediaPendingMask(transportState);
  }

  public void setMediaState(@MediaState int mediaState) {
    getDrawables();
    setCorners(mediaState);
    setAlignment(mediaState == MEDIA_STATE_CAPTIONED);
    setShadowDistance(mediaState == MEDIA_STATE_CAPTIONED || mediaState == MEDIA_STATE_NO_MEDIA
                      ? getContext().getResources().getDimensionPixelSize(R.dimen.conversation_item_drop_shadow_dist)
                      : 0);
    setMediaVisibility(mediaState);
  }

  private void setMediaVisibility(@MediaState int mediaState) {
    mediaBubble.setVisibility(isMediaPresent(mediaState) ? VISIBLE : GONE);
  }

  private void setMediaPendingMask(@TransportState int transportState) {
    if (isPending(transportState)) {
      media.setForeground(new ColorDrawable(mmsPendingOverlayColor));
    } else {
      media.setForeground(new ColorDrawable(Color.TRANSPARENT));
    }
  }

  private void setColors(@TransportState int transportState) {
    final int foregroundColor = getForegroundColor(transportState);
    Log.w(TAG, String.format("setting foreground to #%08X", foregroundColor));
    messageDrawable.setColor(foregroundColor);
    tickDrawable.setColor(foregroundColor);
    mmsContainerDrawable.setColor(foregroundColor);
    media.setBorderColor(foregroundColor);
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
    RelativeLayout.LayoutParams parentParams = (RelativeLayout.LayoutParams) bodyBubble.getLayoutParams();
    if (!extruded) {
      parentParams.addRule(RelativeLayout.BELOW, 0);
      parentParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.media_bubble);
    } else {
      parentParams.addRule(RelativeLayout.BELOW, R.id.media_bubble);
      parentParams.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
    }
    bodyBubble.setLayoutParams(parentParams);
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

  private boolean isMediaPresent(@MediaState int mediaState) {
    return mediaState != MEDIA_STATE_NO_MEDIA;
  }

  private boolean isPending(@TransportState int transportState) {
    return transportState == TRANSPORT_STATE_PUSH_PENDING || transportState == TRANSPORT_STATE_SMS_PENDING;
  }
}