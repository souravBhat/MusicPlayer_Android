package com.sourav.musicon;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.internal.app.ToolbarActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.w3c.dom.Text;

import java.io.IOException;

import java.util.logging.LogRecord;

public class Main_screen extends AppCompatActivity implements  SeekBar.OnSeekBarChangeListener {

    private ImageButton play_btn;
    private ImageButton get_playlist;
    private ImageButton stream_audio;
    private TextView song_title;
    private TextView current_duration;
    private TextView total_duration;
    private Handler mhandler=new Handler();

    private MediaPlayer mediaPlayer;
    private SeekBar song_progress;
    private Utilities utilities=new Utilities();
    MyMediaService myMediaService;
    boolean service_connected=false;
    public  Context context;

    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);

        android.support.v7.widget.Toolbar toolbar=(android.support.v7.widget.Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("MusicOn");

        context=getApplicationContext();
        //All player buttons
        play_btn=(ImageButton)findViewById(R.id.play_btn);
        //get_playlist=(ImageButton)findViewById(R.id.get_playlist);
        //stream_audio=(ImageButton)findViewById(R.id.stream_audio);
        song_title=(TextView)findViewById(R.id.song_title);
        current_duration=(TextView)findViewById(R.id.currentSongTime);
        total_duration=(TextView)findViewById(R.id.totalSongTime);
        song_progress=(SeekBar)findViewById(R.id.songProgress);

        /////Media player

        mediaPlayer=new MediaPlayer();
        ////listeners
        song_progress.setOnSeekBarChangeListener(this);



        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myMediaService.isSongSelected() && myMediaService.isPlaying())
                {
                    myMediaService.handlePause();
                    play_btn.setImageResource(R.drawable.btn_play);
                }
                else if(myMediaService.isSongSelected() && !myMediaService.isPlaying())
                {
                    myMediaService.handleResume();
                    play_btn.setImageResource(R.drawable.btn_pause);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please select a song",Toast.LENGTH_SHORT).show();
                }
            }
        });


        BroadcastReceiver receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String i=intent.getStringExtra("Type");
                if(myMediaService.REGULAR.equals(i))
                {
                    int progress=intent.getIntExtra("song_progress",0);
                    song_progress.setProgress(progress);
                    total_duration.setText(intent.getStringExtra("totalTime"));
                    current_duration.setText(intent.getStringExtra("currentTime"));
                }
                else if(myMediaService.COMPLETE.equals(i))
                {
                    Log.d("player","about to publis complete");
                    song_progress.setProgress(0);
                    Toast.makeText(getApplicationContext(),"The song has finished playing",Toast.LENGTH_SHORT).show();
                    current_duration.setText("0:00");
                    play_btn.setImageResource(R.drawable.btn_play);
                }
                else if(myMediaService.STREAM.equals(i))
                {
                    boolean prepared=intent.getBooleanExtra("prepared",false);
                    if(!prepared &&!isFinishing())
                    {
                       dialog=ProgressDialog.show(Main_screen.this,"Loading","Preparing music..please wait",true);
                       dialog.setCancelable(true);
                        //Toast.makeText(getApplicationContext(), "Preparing music..please wait",Toast.LENGTH_SHORT).show();

                    }
                    else
                    {
                        play_btn.setImageResource(R.drawable.btn_pause);
                        current_duration.setText("0:00");
                        total_duration.setText("0:00");
                        dialog.dismiss();
                        //Toast.makeText(getApplicationContext(), "Music is prepared",Toast.LENGTH_SHORT).show();

                    }


                }
            }
        };
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(MyMediaService.SERVICE_NAME);
        getApplicationContext().registerReceiver(receiver,intentFilter);


        /////audio focus
        AudioManager.OnAudioFocusChangeListener focusChangeListener=new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {
                switch (i)
                {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        prepare();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        myMediaService.release();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        myMediaService.handlePause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        myMediaService.setVolume(0.1f,0.1f);
                }
            }
        };
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        {
            Toast.makeText(getBaseContext(), "cannot get audio focus", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        ////bind to service
        prepare();


    }

    public void prepare()
    {
        Intent intent1=new Intent(getApplicationContext(), MyMediaService.class);
        bindService(intent1, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

       // myMediaService.handleStop();
        unbindService(serviceConnection);
        service_connected=false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==1 )
        {
            if(service_connected)
                myMediaService.handlePlay(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(data.getStringExtra("song_id"))));
                /*mediaPlayer.reset();
                mediaPlayer.setDataSource(this, ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(data.getStringExtra("song_id"))));
                mediaPlayer.prepare();
                mediaPlayer.start();
*/
            song_title.setText(data.getStringExtra("song_title"));
            play_btn.setImageResource(R.drawable.btn_pause);

            song_progress.setProgress(0);
            song_progress.setMax(100);

            //updateProgressBar();
        }
        else if(resultCode==RESULT_OK && requestCode==2)
        {
            myMediaService.handleStream(data.getStringExtra("uri"));
            song_title.setText(data.getStringExtra("song_title"));
            song_progress.setProgress(0);
            song_progress.setMax(100);
        }
    }

  /*  private Runnable mRunnable=new Runnable() {
        @Override
        public void run() {
            long totalduration=mediaPlayer.getDuration();
            long currentposition=mediaPlayer.getCurrentPosition();
            int progress=utilities.getProgressPercentage(currentposition, totalduration);
            Log.d("player",""+progress);
            total_duration.setText(""+utilities.millisecondsToTimer(totalduration));
            current_duration.setText("" + utilities.millisecondsToTimer(currentposition));
            song_progress.setProgress(progress);


            mhandler.postDelayed(mRunnable, 100);
        }
    };
    public void updateProgressBar()
    {
        mhandler.postDelayed(mRunnable, 100);
    }
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id==R.id.see_list)
        {
            Intent intent = new Intent(getApplicationContext(), Playlist.class);
            startActivityForResult(intent, 1);
        }
        if(id==R.id.stream_audio)
        {
            Intent intent=new Intent(getApplicationContext(), Stream_Activity.class);
            startActivityForResult(intent, 2);
        }

        return super.onOptionsItemSelected(item);
    }


    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyMediaService.LocalBinder localBinder=(MyMediaService.LocalBinder)iBinder;
            myMediaService=localBinder.getService();
            service_connected=true;
            Toast.makeText(getApplicationContext(),"Service bound",Toast.LENGTH_SHORT).show();
            Log.d("player","Service bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
                service_connected=false;
        }
    };
    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        myMediaService.removeCallBacks();

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        myMediaService.onStopTracking(song_progress.getProgress());

    }

}
