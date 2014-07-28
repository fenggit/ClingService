package com.example.clingservice.media;

import org.teleal.cling.support.model.TransportState;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.clingservice.util.LogManager;
import com.example.clingservice.util.VolumeCotroller;

/**
 * 
 * @author hefeng
 * 
 */
public class MyMediaPlayerService extends Service {
    private MusicBinder mMusicBinder = new MusicBinder();
    private TransportState mState;
    private MediaPlayer mMediaPlayer;
    private String mPath;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return mMusicBinder;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mState = TransportState.STOPPED;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                LogManager.i("MyMediaPlayerService onCompletion");
                mState = TransportState.STOPPED;
            }
        });

//        mMediaPlayer.setOnErrorListener(new OnErrorListener() {
//
//            @Override
//            public boolean onError(MediaPlayer mp, int what, int extra) {
//                // TODO Auto-generated method stub
//                LogManager.e("MyMediaPlayerService what:extra=" + what + ":" + extra);
//
//                mState = TransportState.STOPPED;
//                return false;
//            }
//        });
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

    }

    public class MusicBinder extends Binder implements IPlayService {
        public IPlayService getPlayService() {
            return this;
        }

        @Override
        public void IPlay() {
            // TODO Auto-generated method stub

//            Log.d("hefeng", "MyMediaPlayerService-----------------IPlay");
            try {
                if (!mMediaPlayer.isPlaying() && mState == TransportState.PAUSED_PLAYBACK) {

                    mMediaPlayer.start();

                } else {

                    if (mMediaPlayer != null) {
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.stop();
                            mMediaPlayer.release();
                        }
                        
                        LogManager.i("MyMediaPlayerService:IPlay loading ...");
                        
                        mMediaPlayer = new MediaPlayer();
                        mMediaPlayer.setDataSource(mPath);
//                        mMediaPlayer.setLooping(true);
                        mMediaPlayer.prepareAsync();
                        mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                // TODO Auto-generated method stub
                                LogManager.i("MyMediaPlayerService:IPlay onPrepared ok ...");

                                mState = TransportState.PLAYING;
                                mp.start();
                            }
                        });
                    }
                }

                mState = TransportState.PLAYING;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                LogManager.e("play error :" + Log.getStackTraceString(e));
            }
        }

        @Override
        public void IPause() {
            // TODO Auto-generated method stub
            mState = TransportState.PAUSED_PLAYBACK;
//            Log.d("hefeng", "MyMediaPlayerService-----------------IPause");

            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
            }
        }

        @Override
        public void IStop() {
            // TODO Auto-generated method stub
            mState = TransportState.STOPPED;
//            Log.d("hefeng", "MyMediaPlayerService-----------------IStop");

            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
            }
        }

        @Override
        public void ISeek(int whereto) {
            // TODO Auto-generated method stub
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(whereto);
            }
            Log.d("hefeng", "MyMediaPlayerService-----------------ISeek=" + whereto);
        }

        @Override
        public void ISetVolume(int volume) {
            // TODO Auto-generated method stub
            Log.d("hefeng", "MyMediaPlayerService-----------------ISetVolume=" + volume);
            VolumeCotroller.setVolume(MyMediaPlayerService.this, volume);
        }

        @Override
        public String IGetPlayerState() {
            // TODO Auto-generated method stub
//            Log.d("hefeng", "MyMediaPlayerService-----------------IGetPlayerState="+mState.getValue());

            return mState.getValue();
        }

        @Override
        public int IGetDuration() {
            // TODO Auto-generated method stub
//            Log.d("hefeng", "MyMediaPlayerService-----------------IGetDuration");
            if (mMediaPlayer != null) {
//                Log.d("hefeng", "MyMediaPlayerService-----------------IGetDuration="+mMediaPlayer.getDuration());
                return mMediaPlayer.getDuration();
            }
            return 0;
        }

        @Override
        public int IGetCurrentPosition() {
            // TODO Auto-generated method stub
//            Log.d("hefeng", "MyMediaPlayerService-----------------IGetCurrentPosition");
            if (mMediaPlayer != null) {
//                Log.d("hefeng", "MyMediaPlayerService-----------------IGetCurrentPosition="+mMediaPlayer.getCurrentPosition());
                return mMediaPlayer.getCurrentPosition();
            }

            return 0;
        }

        @Override
        public void ISetUrl(String uri, String uriMetaData) {
            // TODO Auto-generated method stub
//            Log.d("hefeng", "MyMediaPlayerService-----------------ISetUrl");
            LogManager.e("play uri :" + uri + "||uriMetaData=" + uriMetaData);
            mPath = uri;
        }
    }
}
