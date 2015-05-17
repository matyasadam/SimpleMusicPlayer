package com.app.adam.simplemusicplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class MainActivity extends Activity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener,SensorEventListener {

    private SensorManager sensorMan;
    private Sensor proxiSen;
    private ImageButton playlistButton,nextButton,prevButton,playButton;
    private Button shuffleButton,repeatButton;
    private TextView titleTv,elpasedTV,totalTV;
    private ImageView albumArt;
    private int currentSongIndex = 0;
    private MediaPlayer mp;
    private SongFinder songFinder;
    private SeekBar seekBar;
    private Handler mHandler = new Handler();
    private Converter converter;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<>();
    private MediaMetadataRetriever metaRetriver ;
    private Boolean shuffle = false;
    private Boolean repeat = false;
    private PowerManager powerManager;
    private KeyguardManager keyguardManager;
    private long lastUpdateProxiSen = 0;
    private Boolean proximytySenSwitch = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        sensorMan = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proxiSen = sensorMan.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorMan.registerListener(this, proxiSen, SensorManager.SENSOR_DELAY_NORMAL);

        playlistButton = (ImageButton) findViewById(R.id.playlistButton);
        playButton = (ImageButton) findViewById(R.id.playButton);
        nextButton = (ImageButton) findViewById(R.id.nextButton);
        prevButton = (ImageButton) findViewById(R.id.prevButton);
        shuffleButton = (Button) findViewById(R.id.shuffleButton);
        repeatButton = (Button) findViewById(R.id.repeatButton);
        titleTv = (TextView) findViewById(R.id.titleTV);
        elpasedTV = (TextView) findViewById(R.id.elpassedTV);
        totalTV = (TextView) findViewById(R.id.totalTV);
        albumArt = (ImageView) findViewById(R.id.imageView);


        seekBar =(SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        metaRetriver = new MediaMetadataRetriever();

        mp = new MediaPlayer();
        mp.setOnCompletionListener(this);
        songFinder = new SongFinder();

        songsList=songFinder.getTracks(getContentResolver(),1);

        converter=new Converter();

        playSong(0);

        playlistButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(), PlaylistActivity.class);
                i.putExtra("currentSongIndex",currentSongIndex);
                startActivityForResult(i, 100);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mp.isPlaying()) {
                    if (mp != null) {
                        mp.pause();
                        playButton.setImageResource(R.drawable.play);
                    }
                } else {
                    if (mp != null) {
                        mp.start();
                        playButton.setImageResource(R.drawable.pause);
                    }
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(shuffle){
                    Random rand = new Random();
                    currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1);
                    playSong(currentSongIndex);
                }
                else {
                    if (currentSongIndex < (songsList.size() - 1)) {
                        playSong(currentSongIndex + 1);
                        currentSongIndex = currentSongIndex + 1;
                    } else {
                        playSong(0);
                        currentSongIndex = 0;
                    }
                }

            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(shuffle){
                    Random rand = new Random();
                    currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1);
                    playSong(currentSongIndex);
                }
                else {
                    if (currentSongIndex != 0) {
                        playSong(currentSongIndex - 1);
                        currentSongIndex = currentSongIndex - 1;
                    } else {
                        playSong(songsList.size() - 1);
                        currentSongIndex = songsList.size() - 1;
                    }
                }

            }
        });
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeat){
                    repeat=false;
                    repeatButton.setTextColor(Color.BLACK);
                    repeatButton.setText("R");
                    Toast.makeText(getBaseContext(),"Repeat OFF",Toast.LENGTH_SHORT).show();
                }
                else{
                    repeat=true;
                    repeatButton.setTextColor(Color.RED);
                    repeatButton.setText("R");
                    Toast.makeText(getBaseContext(),"Repeat ON",Toast.LENGTH_SHORT).show();

                }
            }
        });
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shuffle){
                    shuffle=false;
                    shuffleButton.setTextColor(Color.BLACK);
                    shuffleButton.setText("S");
                    Toast.makeText(getBaseContext(),"Shuffle OFF",Toast.LENGTH_SHORT).show();

                }
                else{
                    shuffle=true;
                    shuffleButton.setTextColor(Color.RED);
                    shuffleButton.setText("S");
                    Toast.makeText(getBaseContext(),"Shuffle ON",Toast.LENGTH_SHORT).show();

                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
            currentSongIndex = data.getExtras().getInt("songIndex");
            playSong(currentSongIndex);
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void  playSong(int songIndex){
       if(!songsList.isEmpty()){
           try {
               mp.reset();
               mp.setDataSource(songsList.get(songIndex).get("songPath"));
               mp.prepare();
               mp.start();

               String songTitle = songsList.get(songIndex).get("songTitle");
               titleTv.setText("Title: "+songTitle);

               metaRetriver.setDataSource(songsList.get(songIndex).get("songPath"));
               try { byte [] art = metaRetriver.getEmbeddedPicture();
                   Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
                   albumArt.setImageBitmap(songImage);
//                album.setText(metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
//                artist.setText(metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
//                genre.setText(metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
               }
               catch (Exception e) {
                   albumArt.setImageDrawable(getDrawable(R.drawable.ic));
               }
               seekBar.setProgress(0);
               seekBar.setMax(100);
               updateProgressBar();
           } catch (IllegalArgumentException e) {
               e.printStackTrace();
           } catch (IllegalStateException e) {
               e.printStackTrace();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
        else{
           Toast.makeText(getBaseContext(),"No music on device",Toast.LENGTH_SHORT).show();
       }

    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {

            try {
                long totalDuration = mp.getDuration();
                long currentDuration = mp.getCurrentPosition();
                elpasedTV.setText("" + converter.milliSecondsToTimer(currentDuration));
                totalTV.setText("" + converter.milliSecondsToTimer(totalDuration));
                int progress = (converter.getProgressPercentage(currentDuration, totalDuration));
                seekBar.setProgress(progress);
                mHandler.postDelayed(this, 100);
            } catch (Exception e) {

            }
        }
    };
    @Override
    public void onCompletion(MediaPlayer arg0) {

        if(repeat){
            playSong(currentSongIndex);
        }
        else if(shuffle){
            Random rand = new Random();
            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1);
            playSong(currentSongIndex);
        }
        else{
            if (currentSongIndex < (songsList.size() - 1)) {
                playSong(currentSongIndex + 1);
                currentSongIndex = currentSongIndex + 1;
            }
            else {
                playSong(0);
                currentSongIndex = 0;
            }
        }
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        mp.stop();
        mp.release();
    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        sensorMan.unregisterListener(this,proxiSen);
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        sensorMan.registerListener(this, proxiSen, SensorManager.SENSOR_DELAY_NORMAL);
//    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = converter.progressToTimer(seekBar.getProgress(), totalDuration);
        mp.seekTo(currentPosition);
        updateProgressBar();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_PROXIMITY && proximytySenSwitch) {
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdateProxiSen) > 200) {
                float x = event.values[0];
                lastUpdateProxiSen = curTime;
                if(powerManager.isScreenOn() && x<5){
                    if (mp.isPlaying()) {
                        if (mp != null) {
                            mp.pause();
                            playButton.setImageResource(R.drawable.play);
                        }
                    }
                    else {
                        if (mp != null) {
                            mp.start();
                            playButton.setImageResource(R.drawable.pause);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
