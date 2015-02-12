package org.thoughtcrime.securesms.util;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RotateDrawable;
import android.view.View;

import org.thoughtcrime.securesms.R;

public class ConversationDrawable {
  public static void setColors(final View conversationParent,
                               final View triangleTick,
                               final View mmsContainer,
                               final int foregroundColor,
                               final int shadowColor) {
    final LayerDrawable    messageParentDrawable = (LayerDrawable   ) conversationParent.getBackground();
    final GradientDrawable tickDrawable          = (GradientDrawable) ((RotateDrawable)triangleTick.getBackground()).getDrawable();
    final GradientDrawable messageDrawable       = (GradientDrawable) messageParentDrawable.getDrawable(1);
    final GradientDrawable shadowDrawable        = (GradientDrawable) messageParentDrawable.getDrawable(0);
    final GradientDrawable mmsContainerDrawable  = (GradientDrawable) mmsContainer.getBackground();

    messageParentDrawable.mutate();
    tickDrawable.mutate();
    mmsContainerDrawable.mutate();

    messageDrawable.setColor(foregroundColor);
    tickDrawable.setColor(foregroundColor);
    mmsContainerDrawable.setColor(foregroundColor);

    shadowDrawable.setColor(shadowColor);
  }

  public static void setCorners(final Context context,
                                final View conversationParent,
                                final View mmsContainer,
                                final boolean extruded) {
    final LayerDrawable    messageParentDrawable = (LayerDrawable   ) conversationParent.getBackground();
    final GradientDrawable messageDrawable       = (GradientDrawable) messageParentDrawable.getDrawable(1);
    final GradientDrawable mmsContainerDrawable  = (GradientDrawable) mmsContainer.getBackground();

    int r = context.getResources().getDimensionPixelSize(R.dimen.conversation_item_corner_radius);

    final int topRadius = extruded ? 0 : r;
    messageDrawable.setCornerRadii(new float[]{r, r, topRadius, topRadius, r, r, r, r});
    mmsContainerDrawable.setCornerRadii(new float[]{r, r, r, r, topRadius, topRadius, r, r});

  }
}