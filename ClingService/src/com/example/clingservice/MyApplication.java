package com.example.clingservice;

import org.teleal.cling.UpnpService;
import org.teleal.cling.model.meta.Device;

import com.example.clingservice.media.IPlayService;

import android.app.Application;

public class MyApplication extends Application {
    private UpnpService upnpService;
    private Device mSelectedDevice;
    private IPlayService mPlayService;

    public IPlayService getPlayService() {
        return mPlayService;
    }

    public void setPlayService(IPlayService playService) {
        if (playService != null) {
            this.mPlayService = playService;
        }
    }

    public UpnpService getUpnpService() {
        return upnpService;
    }

    public void setUpnpService(UpnpService upnpService) {
        this.upnpService = upnpService;
    }

    public Device getmSelectedDevice() {
        return mSelectedDevice;
    }

    public void setmSelectedDevice(Device mSelectedDevice) {
        this.mSelectedDevice = mSelectedDevice;
    }
}
