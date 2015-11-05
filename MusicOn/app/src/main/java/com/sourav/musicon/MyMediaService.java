package com.sourav.musicon;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class MyMediaService extends Service implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mediaPlayer=new MediaPlayer();
    private Utilities utilities=new Utilities();
    private Handler mhandler=new Handler();

    final static String PLAY = "com.sourav.musicon.play";
    final static String PAUSE = "com.sourav.musicon.pause";
    final static String STOP = "com.sourav.musicon.stop";

    final static String SERVICE_NAME="mymediaservice";
    final static String REGULAR="regular";
    final static String COMPLETE="complete";
    final static String STREAM="stream";
    private boolean songSelected=false;



    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
                long totalduration = mediaPlayer.getDuration();
                long currentposition = mediaPlayer.getCurrentPosition();
                String totalTime=""+utilities.millisecondsToTimer(totalduration);
                String currentTime=""+utilities.millisecondsToTimer(currentposition);

                int progress = utilities.getProgressPercentage(currentposition, totalduration);
                Intent returnIntent = new Intent();
                returnIntent.setAction(SERVICE_NAME);
                returnIntent.putExtra("Type",REGULAR);
                returnIntent.putExtra("song_progress", progress);
                returnIntent.putExtra("totalTime",totalTime);
                returnIntent.putExtra("currentTime",currentTime);
                sendBroadcast(returnIntent);

                mhandler.postDelayed(mRunnable, 100);
            }

        }
    };


    public void removeCallBacks()
    {
        mhandler.removeCallbacks(mRunnable);
    }


    public void onStopTracking(int progress)
    {
        mhandler.removeCallbacks(mRunnable);
        long totalduration=mediaPlayer.getDuration();
        Log.d("player", "" + utilities.millisecondsToTimer(totalduration));

        int currentposition=utilities.getTimerFromProgress(progress,totalduration);
        mediaPlayer.seekTo(currentposition);
        updateProgressBar();
    }
    public void handleStop() {
        if (mediaPlayer.isPlaying()) {
            release();
        }
    }

    public void handlePause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mhandler.removeCallbacks(mRunnable);
        }
    }
    public void handleResume()
    {
        if(mediaPlayer!=null)
        {
            mediaPlayer.start();
            updateProgressBar();

        }
    }


    public void handlePlay(Uri uri) {

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            songSelected=true;
            updateProgressBar();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void handleStream(String uri)
    {
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            Log.d("player",uri);
            mediaPlayer.setDataSource(uri);
        }
        catch(IllegalArgumentException e)
        {
            Toast.makeText(getApplicationContext(), "Illegal argument exception", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "streaming audio illegal state exception", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try
        {
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Intent intent = new Intent();
                    intent.setAction(SERVICE_NAME);
                    intent.putExtra("Type", STREAM);
                    intent.putExtra("prepared", true);
                    Log.d("player", "second after prepared intent sent");
                    sendBroadcast(intent);
                    mediaPlayer.start();
                    songSelected = true;
                    updateProgressBar();
                }
            });
            Intent intent=new Intent();
            intent.setAction(SERVICE_NAME);
            intent.putExtra("Type", STREAM);
            intent.putExtra("prepared",false);
            Log.d("player","first intent sent");
            sendBroadcast(intent);
            mediaPlayer.prepareAsync();
        }catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
    }
    public void setOnSelected(boolean bool)
    {
        songSelected=bool;
    }
    public boolean isSongSelected()
    {
        return  songSelected;
    }
    public void updateProgressBar() {
        mhandler.postDelayed(mRunnable, 100);
    }

    public boolean isPlaying()
    {
        return mediaPlayer.isPlaying();
    }
    public void release()
    {
        if(mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mhandler.removeCallbacks(mRunnable);
        release();
        return super.onUnbind(intent);
    }

    LocalBinder binder=new LocalBinder();

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Intent intent=new Intent();
        intent.setAction(SERVICE_NAME);
        intent.putExtra("Type", COMPLETE);
        Log.d("player", "player completed");
        sendBroadcast(intent);

    }

    public void setVolume(float v, float v1) {
        mediaPlayer.setVolume(v,v1);
    }

    public class LocalBinder extends Binder{
         MyMediaService getService()
        {
            return MyMediaService.this;
        }

    }
    @Override
    public IBinder onBind(Intent intent) {
        mediaPlayer.setOnCompletionListener(this);
        Log.d("player","returned binder");
        return binder;
    }
}
