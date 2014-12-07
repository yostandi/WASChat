package org.thoughtcrime.securesms.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

public class MediaAutoDownloadPreference extends DialogPreference {
  private String transport;
  private CheckBox images;
  private CheckBox audio;
  private CheckBox video;
  public MediaAutoDownloadPreference(Context context, AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MediaAutoDownloadPreference, 0, 0);
    transport = a.getString(R.styleable.MediaAutoDownloadPreference_transport);
    setPersistent(false);
    setDialogLayoutResource(R.layout.media_auto_download_preference);
  }

  @Override
  protected void onBindDialogView(final View view) {
    super.onBindDialogView(view);
    images = (CheckBox) view.findViewById(R.id.media_images);
    audio  = (CheckBox) view.findViewById(R.id.media_audio);
    video  = (CheckBox) view.findViewById(R.id.media_video);

    switch (transport) {
    case "wifi":
      images.setChecked(TextSecurePreferences.isWifiImageAutoDownloadAllowed(getContext()));
      audio.setChecked(TextSecurePreferences.isWifiAudioAutoDownloadAllowed(getContext()));
      video.setChecked(TextSecurePreferences.isWifiVideoAutoDownloadAllowed(getContext()));
      break;
    case "roaming":
      images.setChecked(TextSecurePreferences.isRoamingImageAutoDownloadAllowed(getContext()));
      audio.setChecked(TextSecurePreferences.isRoamingAudioAutoDownloadAllowed(getContext()));
      video.setChecked(TextSecurePreferences.isRoamingVideoAutoDownloadAllowed(getContext()));
      break;
    case "mobile":
    default:
      images.setChecked(TextSecurePreferences.isDataImageAutoDownloadAllowed(getContext()));
      audio.setChecked(TextSecurePreferences.isDataAudioAutoDownloadAllowed(getContext()));
      video.setChecked(TextSecurePreferences.isDataVideoAutoDownloadAllowed(getContext()));
      break;
    }
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);

    if (positiveResult) {
      switch (transport) {
        case "wifi":
          TextSecurePreferences.setWifiImageAutoDownloadAllowed(getContext(), images.isChecked());
          TextSecurePreferences.setWifiAudioAutoDownloadAllowed(getContext(), audio.isChecked());
          TextSecurePreferences.setWifiVideoAutoDownloadAllowed(getContext(), video.isChecked());
          break;
        case "roaming":
          TextSecurePreferences.setRoamingImageAutoDownloadAllowed(getContext(), images.isChecked());
          TextSecurePreferences.setRoamingAudioAutoDownloadAllowed(getContext(), audio.isChecked());
          TextSecurePreferences.setRoamingVideoAutoDownloadAllowed(getContext(), video.isChecked());
          break;
        case "mobile":
        default:
          TextSecurePreferences.setDataImageAutoDownloadAllowed(getContext(), images.isChecked());
          TextSecurePreferences.setDataAudioAutoDownloadAllowed(getContext(), audio.isChecked());
          TextSecurePreferences.setDataVideoAutoDownloadAllowed(getContext(), video.isChecked());
          break;
      }
      if (getOnPreferenceChangeListener() != null) getOnPreferenceChangeListener().onPreferenceChange(this, null);
    }
  }
}
