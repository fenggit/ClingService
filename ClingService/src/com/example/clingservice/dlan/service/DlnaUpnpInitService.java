package com.example.clingservice.dlan.service;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.android.AndroidUpnpServiceConfiguration;
import org.teleal.cling.android.AndroidWifiSwitchableRouter;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.model.ServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.protocol.ProtocolFactory;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.transport.Router;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;

import com.example.clingservice.MyApplication;
import com.example.clingservice.media.IPlayService;
import com.example.clingservice.media.MyMediaPlayerService;
import com.example.clingservice.media.PlayServiceGetter;
import com.example.clingservice.media.MyMediaPlayerService.MusicBinder;
import com.example.clingservice.util.Constants;
import com.example.clingservice.util.LogManager;

public class DlnaUpnpInitService extends Service implements PlayServiceGetter,IService {
    private final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    private final String MODEL_NAME = "MediaServer";
    private final String MODEL_DESCRIPTION = "MediaServer for Android";
    private final String MODEL_NUMBER = "v1";

    protected UpnpService mUpnpService;

    private LocalDevice mLocalDevice;
    private LocalDevice mLocalRendererDevice;

    private DlanUpnpBinder mDlanUpnpBinder = new DlanUpnpBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        bindService();
        createRouter();
        createDevice();
    }

    
    private void createRouter(){
        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mUpnpService = new UpnpServiceImpl(createConfiguration(wifiManager)) {
            @Override
            protected Router createRouter(ProtocolFactory protocolFactory, Registry registry) {
                AndroidWifiSwitchableRouter router = DlnaUpnpInitService.this.createRouter(getConfiguration(), protocolFactory, wifiManager,
                        connectivityManager);
                if (!ModelUtil.ANDROID_EMULATOR) {
                    // Only register for network connectivity changes if we are
                    // not running on emulator
                    registerReceiver(router.getBroadcastReceiver(), new IntentFilter(CONNECTIVITY_CHANGE_ACTION));
                }
                return router;
            }
        };
    }
    private ServiceManager<MyAVTransportService> mAVTransportManager;
    private ServiceManager<MyRendererControlService> mRenderingControl;

    private void createDevice() {
//        DeviceType typeServer = new UDADeviceType("MediaServer", 1);
        DeviceType typeRenderer = new UDADeviceType("MediaRenderer", 1);

        // create device's UDN.
//        UDN mUDN = UDN.uniqueSystemIdentifier("Hezi-MediaServer");
        UDN mRendererUDN = UDN.uniqueSystemIdentifier("Hezi-MediaRenderer");

        // create device's Details.
//        DeviceDetails details1 = new DeviceDetails("MediaServer Service", new ManufacturerDetails(android.os.Build.MANUFACTURER), new ModelDetails(
//                MODEL_NAME, MODEL_DESCRIPTION, MODEL_NUMBER));
        DeviceDetails details2 = new DeviceDetails(Constants.SERVICE_NAME, new ManufacturerDetails(android.os.Build.MANUFACTURER), new ModelDetails(
                MODEL_NAME, MODEL_DESCRIPTION, MODEL_NUMBER));

        //set connect type
        LocalService<MyConnectionManagerService> avConnectionService = new AnnotationLocalServiceBinder().read(MyConnectionManagerService.class);
        avConnectionService.setManager(new DefaultServiceManager<MyConnectionManagerService>(avConnectionService) {
            @Override
            protected MyConnectionManagerService createServiceInstance() throws Exception {
                return new MyConnectionManagerService();
            }
        });

        // create device's loacl service.
        LocalService<MyContentDirectoryService> contentService = new AnnotationLocalServiceBinder().read(MyContentDirectoryService.class);
        contentService.setManager(new DefaultServiceManager<MyContentDirectoryService>(contentService, MyContentDirectoryService.class) {
            @Override
            protected MyContentDirectoryService createServiceInstance() throws Exception {
                return new MyContentDirectoryService();
            }
        });

        LocalService<MyAVTransportService> transportService = new AnnotationLocalServiceBinder().read(MyAVTransportService.class);
        mAVTransportManager = new DefaultServiceManager<MyAVTransportService>(transportService, MyAVTransportService.class) {
            @Override
            protected MyAVTransportService createServiceInstance() throws Exception {
                return new MyAVTransportService(DlnaUpnpInitService.this);
            }
        };
        transportService.setManager(mAVTransportManager);

        LocalService<MyRendererControlService> renderService = new AnnotationLocalServiceBinder().read(MyRendererControlService.class);
        mRenderingControl = new DefaultServiceManager<MyRendererControlService>(renderService, MyRendererControlService.class) {
            @Override
            protected MyRendererControlService createServiceInstance() throws Exception {
                return new MyRendererControlService(DlnaUpnpInitService.this, DlnaUpnpInitService.this);
            }
        };
        renderService.setManager(mRenderingControl);

        // create a loacl device has a DMS.
        try {
//            mLocalDevice = new LocalDevice(new DeviceIdentity(mUDN), typeServer, details1, contentService);

            mLocalRendererDevice = new LocalDevice(new DeviceIdentity(mRendererUDN), typeRenderer, details2, new LocalService[] { renderService,
                    avConnectionService, transportService });
        } catch (ValidationException e) {
            e.printStackTrace();
        }
        runLastChangePushThread();
    }

    private void runLastChangePushThread() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        mAVTransportManager.getImplementation().fireLastChange();
                        mRenderingControl.getImplementation().fireLastChange();
                        Thread.sleep(500);
                    }
                } catch (Exception e) {

                }
            }
        }.start();
    }

    private IPlayService mPlayService;
    private MusicBinder mMusicBinder;
    @Override
    public void bindService() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, MyMediaPlayerService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void unBindService() {
        // TODO Auto-generated method stub
        unbindService(mConnection);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            LogManager.e("DlnaUpnpInitService:MyMediaPlayerService onServiceDisconnected");
            
            mPlayService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            LogManager.e("DlnaUpnpInitService:bind MyMediaPlayerService sucess");
            
            mMusicBinder = (MusicBinder) service;
            mPlayService = mMusicBinder.getPlayService();
            ((MyApplication)getApplication()).setPlayService(mPlayService);
            
        }
    };

    protected AndroidUpnpServiceConfiguration createConfiguration(WifiManager wifiManager) {
        // find which service devices, if not implement getExclusiveServiceTypes function,
        // all upnp device in the net work can be got.
        return new AndroidUpnpServiceConfiguration(wifiManager) {
            @Override
            public ServiceType[] getExclusiveServiceTypes() {
                return new ServiceType[] { new UDAServiceType("AVTransport"), new UDAServiceType("ContentDirectory"),
                        new UDAServiceType("RenderingControl") };
            }
        };
    }

    protected AndroidWifiSwitchableRouter createRouter(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory,
            WifiManager wifiManager, ConnectivityManager connectivityManager) {
        return new AndroidWifiSwitchableRouter(configuration, protocolFactory, wifiManager, connectivityManager);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mDlanUpnpBinder;
    }

    public class DlanUpnpBinder extends Binder {
        public UpnpService getAndroidUpnpService() {
            return mUpnpService != null ? mUpnpService : null;
        }

        public LocalDevice getLocalServer() {
            return mLocalDevice != null ? mLocalDevice : null;
        }

        public LocalDevice getLocalRenderer() {
            return mLocalRendererDevice != null ? mLocalRendererDevice : null;
        }
    }

    @Override
    public IPlayService getPlayService() {
        // TODO Auto-generated method stub
        if (mPlayService == null) {
            LogManager.e("bind music server error");
        }else{
//            LogManager.i("DlnaUpnpInitService all  mPlayService==null?"+(mPlayService==null)) ;
        }
        
        //06-16 11:44:02.414: E/MediaPlayer(15262): Attempt to call getDuration without a valid mediaplayer
        return mPlayService;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unBindService();
    }
}
