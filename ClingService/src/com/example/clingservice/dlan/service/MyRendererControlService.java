package com.example.clingservice.dlan.service;

import java.lang.reflect.Method;

import org.teleal.cling.binding.annotations.UpnpAction;
import org.teleal.cling.binding.annotations.UpnpInputArgument;
import org.teleal.cling.binding.annotations.UpnpOutputArgument;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.model.types.UnsignedIntegerTwoBytes;
import org.teleal.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.teleal.cling.support.renderingcontrol.RenderingControlException;

import android.content.Context;
import android.media.AudioManager;

import com.example.clingservice.media.PlayServiceGetter;
import com.example.clingservice.util.LogManager;

public class MyRendererControlService extends AbstractAudioRenderingControl {
    private PlayServiceGetter mGetter;
    private AudioManager mAudioManager;

    public MyRendererControlService(PlayServiceGetter getter, Context context) {
        // TODO Auto-generated constructor stub
        mGetter = getter;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentMute", stateVariable = "Mute"))
    public boolean getMute(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0, @UpnpInputArgument(name = "Channel") String arg1)
            throws RenderingControlException {
        // TODO Auto-generated method stub
        LogManager.i("getMute :" + arg1);
        boolean isMute = false;
        try {
            Method method = AudioManager.class.getMethod("isStreamMute", int.class);
            isMute = ((Boolean) method.invoke(mAudioManager, AudioManager.STREAM_MUSIC)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isMute;
    }

    @Override
    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentVolume", stateVariable = "Volume"))
    public UnsignedIntegerTwoBytes getVolume(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
            @UpnpInputArgument(name = "Channel") String arg1) throws RenderingControlException {
        // TODO Auto-generated method stub

        LogManager.i("getVolume :" + arg1);
        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        LogManager.i("getVolume/volume :" + volume);
        return new UnsignedIntegerTwoBytes(volume);
    }

    @Override
    @UpnpAction
    public void setMute(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0, @UpnpInputArgument(name = "Channel") String arg1,
            @UpnpInputArgument(name = "DesiredMute", stateVariable = "Mute") boolean desiredMute) throws RenderingControlException {
        // TODO Auto-generated method stub
        LogManager.i("setMute :" + arg1 + "||arg2=" + desiredMute);
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, desiredMute);
    }

    @Override
    @UpnpAction
    public void setVolume(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0, @UpnpInputArgument(name = "Channel") String arg1,
            @UpnpInputArgument(name = "DesiredVolume", stateVariable = "Volume") UnsignedIntegerTwoBytes arg2) throws RenderingControlException {
        // TODO Auto-generated method stub
        if (mGetter.getPlayService() != null) {
            mGetter.getPlayService().ISetVolume(arg2.getValue().intValue());
        }
        LogManager.i("setVolume :" + arg0 + "||" + arg1 + "||arg2=" + arg2);
    }
    
    
}
