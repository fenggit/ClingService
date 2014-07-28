package com.example.clingservice.util;

import android.content.Context;
import android.media.AudioManager;

public class VolumeCotroller {
    
    public static void setVolume(Context context, int volume) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }
}
