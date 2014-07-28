package com.example.clingservice.dlan.service;

import java.util.Locale;

import org.teleal.cling.binding.annotations.UpnpAction;
import org.teleal.cling.binding.annotations.UpnpInputArgument;
import org.teleal.cling.binding.annotations.UpnpOutputArgument;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.AVTransportException;
import org.teleal.cling.support.avtransport.AbstractAVTransportService;
import org.teleal.cling.support.model.DeviceCapabilities;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PlayMode;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.SeekMode;
import org.teleal.cling.support.model.StorageMedium;
import org.teleal.cling.support.model.TransportAction;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.support.model.TransportSettings;
import org.teleal.cling.support.model.TransportState;
import org.teleal.cling.support.model.TransportStatus;

import com.example.clingservice.media.PlayServiceGetter;
import com.example.clingservice.util.LogManager;

public class MyAVTransportService extends AbstractAVTransportService {
    private final static int TIME_SECOND = 1000;
    private final static int TIME_MINUTE = TIME_SECOND * 60;
    private final static int TIME_HOUR = TIME_MINUTE * 60;

    private String mCurrentURI;
    private String mCurrentMetaData;

    private PlayServiceGetter mGetter;

    public MyAVTransportService(PlayServiceGetter getter) {
        mGetter = getter;
    }

    @Override
    @UpnpAction(out = @UpnpOutputArgument(name = "Actions"))
    public String getCurrentTransportActions(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
//        LogManager.i("getCurrentTransportActions");
        TransportAction[] actions = null;
        if (mGetter.getPlayService() != null) {

            TransportState state = TransportState.valueOrCustomOf(mGetter.getPlayService().IGetPlayerState());
            switch (state) {
            case STOPPED:
                actions = new TransportAction[] { TransportAction.Play };
                break;
            case PLAYING:
                actions = new TransportAction[] { TransportAction.Stop, TransportAction.Seek, TransportAction.Pause };
            case PAUSED_PLAYBACK:
                actions = new TransportAction[] { TransportAction.Play, TransportAction.Stop, TransportAction.Pause, TransportAction.Seek };
            default:
                actions = null;
                break;
            }

        }
        return ModelUtil.toCommaSeparatedList(actions);
    }

    @Override
    @UpnpAction(out = { @UpnpOutputArgument(name = "PlayMedia", stateVariable = "PossiblePlaybackStorageMedia", getterName = "getPlayMediaString"),
            @UpnpOutputArgument(name = "RecMedia", stateVariable = "PossibleRecordStorageMedia", getterName = "getRecMediaString"),
            @UpnpOutputArgument(name = "RecQualityModes", stateVariable = "PossibleRecordQualityModes", getterName = "getRecQualityModesString") })
    public DeviceCapabilities getDeviceCapabilities(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0)
            throws AVTransportException {
//        LogManager.i("getDeviceCapabilities");
        return new DeviceCapabilities(new StorageMedium[] { StorageMedium.NETWORK });
    }

    @Override
    @UpnpAction(out = { @UpnpOutputArgument(name = "NrTracks", stateVariable = "NumberOfTracks", getterName = "getNumberOfTracks"),
            @UpnpOutputArgument(name = "MediaDuration", stateVariable = "CurrentMediaDuration", getterName = "getMediaDuration"),
            @UpnpOutputArgument(name = "CurrentURI", stateVariable = "AVTransportURI", getterName = "getCurrentURI"),
            @UpnpOutputArgument(name = "CurrentURIMetaData", stateVariable = "AVTransportURIMetaData", getterName = "getCurrentURIMetaData"),
            @UpnpOutputArgument(name = "NextURI", stateVariable = "NextAVTransportURI", getterName = "getNextURI"),
            @UpnpOutputArgument(name = "NextURIMetaData", stateVariable = "NextAVTransportURIMetaData", getterName = "getNextURIMetaData"),
            @UpnpOutputArgument(name = "PlayMedium", stateVariable = "PlaybackStorageMedium", getterName = "getPlayMedium"),
            @UpnpOutputArgument(name = "RecordMedium", stateVariable = "RecordStorageMedium", getterName = "getRecordMedium"),
            @UpnpOutputArgument(name = "WriteStatus", stateVariable = "RecordMediumWriteStatus", getterName = "getWriteStatus") })
    public MediaInfo getMediaInfo(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
//        LogManager.i("getMediaInfo");
        
        return new MediaInfo(mCurrentURI, mCurrentMetaData);
    }

    @Override
    @UpnpAction(out = { @UpnpOutputArgument(name = "Track", stateVariable = "CurrentTrack", getterName = "getTrack"),
            @UpnpOutputArgument(name = "TrackDuration", stateVariable = "CurrentTrackDuration", getterName = "getTrackDuration"),
            @UpnpOutputArgument(name = "TrackMetaData", stateVariable = "CurrentTrackMetaData", getterName = "getTrackMetaData"),
            @UpnpOutputArgument(name = "TrackURI", stateVariable = "CurrentTrackURI", getterName = "getTrackURI"),
            @UpnpOutputArgument(name = "RelTime", stateVariable = "RelativeTimePosition", getterName = "getRelTime"),
            @UpnpOutputArgument(name = "AbsTime", stateVariable = "AbsoluteTimePosition", getterName = "getAbsTime"),
            @UpnpOutputArgument(name = "RelCount", stateVariable = "RelativeCounterPosition", getterName = "getRelCount"),
            @UpnpOutputArgument(name = "AbsCount", stateVariable = "AbsoluteCounterPosition", getterName = "getAbsCount") })
    public PositionInfo getPositionInfo(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
//        LogManager.i("getPositionInfo");
        
        if (mGetter.getPlayService() != null) {
            int trackDuration = 0;

            trackDuration = mGetter.getPlayService().IGetDuration();

            if (trackDuration < 0) {
                trackDuration = 0;
            }
            int currentPos = 0;

            currentPos = mGetter.getPlayService().IGetCurrentPosition();

            if (currentPos < 0) {
                currentPos = 0;
            }
            PositionInfo curPosInfo = new PositionInfo(0, formatTimeInfo(trackDuration), mCurrentURI, formatTimeInfo(currentPos),
                    formatTimeInfo(currentPos));
            return curPosInfo;
        }
        return new PositionInfo();
    }

    @Override
    @UpnpAction(out = {
            @UpnpOutputArgument(name = "CurrentTransportState", stateVariable = "TransportState", getterName = "getCurrentTransportState"),
            @UpnpOutputArgument(name = "CurrentTransportStatus", stateVariable = "TransportStatus", getterName = "getCurrentTransportStatus"),
            @UpnpOutputArgument(name = "CurrentSpeed", stateVariable = "TransportPlaySpeed", getterName = "getCurrentSpeed") })
    public TransportInfo getTransportInfo(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
//        LogManager.i("getTransportInfo");
        
        if (mGetter.getPlayService() != null) {
            TransportState state = TransportState.STOPPED;

            state = TransportState.valueOrCustomOf(mGetter.getPlayService().IGetPlayerState());

//            LogManager.i("TransportStatus->" + state);
            return new TransportInfo(state, TransportStatus.OK);
        }
        return null;
    }

    @Override
    @UpnpAction(out = { @UpnpOutputArgument(name = "PlayMode", stateVariable = "CurrentPlayMode", getterName = "getPlayMode"),
            @UpnpOutputArgument(name = "RecQualityMode", stateVariable = "CurrentRecordQualityMode", getterName = "getRecQualityMode") })
    public TransportSettings getTransportSettings(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        LogManager.i("getTransportSettings");
        return new TransportSettings(PlayMode.NORMAL);
    }

    @Override
    @UpnpAction
    public void next(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        LogManager.i("next");

    }

    @Override
    @UpnpAction
    public void pause(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        LogManager.i("pause");
        if (mGetter.getPlayService() != null) {

            mGetter.getPlayService().IPause();

        }
    }

    @Override
    @UpnpAction
    public void play(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
            @UpnpInputArgument(name = "Speed", stateVariable = "TransportPlaySpeed") String arg1) throws AVTransportException {
        LogManager.i("play");
        if (mGetter.getPlayService() != null) {
            mGetter.getPlayService().IPlay();
        }
    }

    @Override
    @UpnpAction
    public void previous(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        LogManager.i("previous");
    }

    @Override
    @UpnpAction
    public void record(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        LogManager.i("record");
    }

    @Override
    @UpnpAction
    public void seek(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
            @UpnpInputArgument(name = "Unit", stateVariable = "A_ARG_TYPE_SeekMode") String arg1,
            @UpnpInputArgument(name = "Target", stateVariable = "A_ARG_TYPE_SeekTarget") String arg2) throws AVTransportException {
        LogManager.i("seek");
        if (mGetter.getPlayService() != null) {
            SeekMode seekMode;
            seekMode = SeekMode.valueOrExceptionOf(arg1);
            if (!seekMode.equals(SeekMode.REL_TIME)) {
                throw new IllegalArgumentException();
            }
            int seconds = (int) ModelUtil.fromTimeString(arg2);
            mGetter.getPlayService().ISeek(seconds * TIME_SECOND);
        }
    }

    @Override
    @UpnpAction
    public void setAVTransportURI(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
            @UpnpInputArgument(name = "CurrentURI", stateVariable = "AVTransportURI") String arg1,
            @UpnpInputArgument(name = "CurrentURIMetaData", stateVariable = "AVTransportURIMetaData") String arg2) throws AVTransportException {
        LogManager.i("setAVTransportURI");
        mCurrentURI = arg1;
        mCurrentMetaData = arg2;
        if (mGetter.getPlayService() != null) {
            mGetter.getPlayService().ISetUrl(arg1, arg2);
        }
    }

    @Override
    @UpnpAction
    public void setNextAVTransportURI(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
            @UpnpInputArgument(name = "NextURI", stateVariable = "AVTransportURI") String arg1,
            @UpnpInputArgument(name = "NextURIMetaData", stateVariable = "AVTransportURIMetaData") String arg2) throws AVTransportException {
        LogManager.i("setNextAVTransportURI");
    }

    @Override
    @UpnpAction
    public void setPlayMode(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
            @UpnpInputArgument(name = "NewPlayMode", stateVariable = "CurrentPlayMode") String arg1) throws AVTransportException {
        LogManager.i("setPlayMode");
    }

    @Override
    @UpnpAction
    public void setRecordQualityMode(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
            @UpnpInputArgument(name = "NewRecordQualityMode", stateVariable = "CurrentRecordQualityMode") String arg1) throws AVTransportException {
        LogManager.i("setRecordQualityMode");
    }

    @Override
    @UpnpAction
    public void stop(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0) throws AVTransportException {
        LogManager.i("stop");
        if (mGetter.getPlayService() != null) {
            mGetter.getPlayService().IStop();
        }
    }

    private String formatTimeInfo(int timeVal) {
        int hour = timeVal / TIME_HOUR;
        int minute = (timeVal - hour * TIME_HOUR) / TIME_MINUTE;
        int second = (timeVal - hour * TIME_HOUR - minute * TIME_MINUTE) / TIME_SECOND;
        return String.format(Locale.US, "%02d:%02d:%02d", hour, minute, second);
    }
}
