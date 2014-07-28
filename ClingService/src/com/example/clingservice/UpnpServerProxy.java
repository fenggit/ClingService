package com.example.clingservice;

import org.teleal.cling.UpnpService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.example.clingservice.dlan.service.DlnaUpnpInitService;
import com.example.clingservice.dlan.service.DlnaUpnpInitService.DlanUpnpBinder;
import com.example.clingservice.dlan.service.IService;
import com.example.clingservice.util.LogManager;

public class UpnpServerProxy implements IService{
    protected UpnpService mUpnpService;
    private Activity context;
    private DlanUpnpBinder mDlanUpnpBinder;

    public UpnpServerProxy(Activity context) {
        // TODO Auto-generated constructor stub
        this.context = context;
    }
    
    @Override
    public void bindService() {
        Intent intent = new Intent(context, DlnaUpnpInitService.class) ;
        context.startService(intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    public void unBindService() {
        if (mUpnpService != null) {
            mUpnpService.getRegistry().removeAllLocalDevices();
        }
        context.unbindService(serviceConnection);
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            LogManager.e("UpnpServerProxy:bind DlnaUpnpInitService(UPnP Service) success");
            
            mDlanUpnpBinder = (DlanUpnpBinder) service;
            mUpnpService = mDlanUpnpBinder.getAndroidUpnpService();

            if (mUpnpService == null) {
                LogManager.e("mUpnpService is null");
                return;
            } else {
                ((MyApplication) context.getApplication()).setUpnpService(mUpnpService);
            }
//            LocalDevice loalServer = binder.getLocalServer();
//            LocalDevice localRenderer = binder.getLocalRenderer();

//            if (loalServer != null && localRenderer != null) {
//                Log.e("hefeng", "the local device is not null!!");
//            }

            // Getting ready for future device advertisements
//            mUpnpService.getRegistry().addListener(mRegistryListener);

            // Search asynchronously for all devices
            //mUpnpService.getControlPoint().search();

//            mUpnpService.getRegistry().addDevice(binder.getLocalServer());
            mUpnpService.getRegistry().addDevice(mDlanUpnpBinder.getLocalRenderer());
        }

        public void onServiceDisconnected(ComponentName className) {
            mUpnpService = null;
            Log.e("hefeng", "UpnpServerProxy:onServiceDisconnected : " + className.getClassName());
        }
    };

}
