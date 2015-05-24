package com.app.adam.simplemusicplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener,SensorEventListener {

    private SensorManager sensorMan;
    private Sensor proxiSen;
    private ImageButton nextButton,prevButton,playButton,shuffleButton,repeatButton;
    private TextView titleTv,elpasedTV,totalTV;
    private ImageView albumArt;
    private int currentSongIndex = 0;
    private MediaPlayer mp;
    private SongFinder songFinder;
    private SeekBar seekBar;
    private Handler mHandler = new Handler();
    private Converter converter;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> songsListWithBPM = new ArrayList<>();
    private MediaMetadataRetriever metaRetriver ;
    private Boolean shuffle = false;
    private Boolean repeat = false;
    private PowerManager powerManager;
    private KeyguardManager keyguardManager;
    private long lastUpdateProxiSen = 0;
    private Boolean proximytySenSwitch = false;
    private Boolean contextON_OFF = false;
    private LocalBroadcastManager mBroadcastManager;
    private LogFile mLogFile;
    private ActivityUtils.REQUEST_TYPE mRequestType;
    IntentFilter mBroadcastFilter;
    private DetectionRequester mDetectionRequester;
    private DetectionRemover mDetectionRemover;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBroadcastManager = LocalBroadcastManager.getInstance(this);

        mBroadcastFilter = new IntentFilter(ActivityUtils.ACTION_REFRESH_STATUS_LIST);
        mBroadcastFilter.addCategory(ActivityUtils.CATEGORY_LOCATION_SERVICES);

        mDetectionRequester = new DetectionRequester(this);
        mDetectionRemover = new DetectionRemover(this);

        mLogFile = LogFile.getInstance(this);

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            printActivityHistory();
                        }
                        catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 2000);


        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        sensorMan = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proxiSen = sensorMan.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorMan.registerListener(this, proxiSen, SensorManager.SENSOR_DELAY_NORMAL);
        setTitle("");

        playButton = (ImageButton) findViewById(R.id.playButton);
        nextButton = (ImageButton) findViewById(R.id.nextButton);
        prevButton = (ImageButton) findViewById(R.id.prevButton);
        shuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
        repeatButton = (ImageButton) findViewById(R.id.repeatButton);
        titleTv = (TextView) findViewById(R.id.titleTV);
        titleTv.setSelected(true);
        elpasedTV = (TextView) findViewById(R.id.elpassedTV);
        totalTV = (TextView) findViewById(R.id.totalTV);
        albumArt = (ImageView) findViewById(R.id.imageView);


        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        metaRetriver = new MediaMetadataRetriever();

        mp = new MediaPlayer();
        mp.setOnCompletionListener(this);
        songFinder = new SongFinder();

        songsList = songFinder.getTracks(getContentResolver(), 1);
        new makeBPM_PlaylistInBack().execute("");
        converter = new Converter();

        playSong(0);

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
                if (shuffle) {
                    Random rand = new Random();
                    currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1);
                    playSong(currentSongIndex);
                } else {
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
                if (shuffle) {
                    Random rand = new Random();
                    currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1);
                    playSong(currentSongIndex);
                } else {
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
                if (repeat) {
                    repeat = false;
                    repeatButton.setImageResource(R.drawable.repeat48);
                    Toast.makeText(getBaseContext(), "Repeat OFF", Toast.LENGTH_SHORT).show();
                } else {
                    repeat = true;
                    repeatButton.setImageResource(R.drawable.repeat2_48);
                    Toast.makeText(getBaseContext(), "Repeat ON", Toast.LENGTH_SHORT).show();

                }
            }
        });
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffle) {
                    shuffle = false;
                    shuffleButton.setImageResource(R.drawable.shuffle48);
                    Toast.makeText(getBaseContext(), "Shuffle OFF", Toast.LENGTH_SHORT).show();

                } else {
                    shuffle = true;
                    shuffleButton.setImageResource(R.drawable.shuffle2_48);
                    Toast.makeText(getBaseContext(), "Shuffle ON", Toast.LENGTH_SHORT).show();

                }
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Boolean datas = extras.getBoolean("contextOn");
            if (datas) {
                contextON_OFF = true;
            }
            else{
                contextON_OFF = false;
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
            currentSongIndex = data.getExtras().getInt("songIndex");
            playSong(currentSongIndex);
        }
        if(resultCode == 200){
            Boolean datas = data.getExtras().getBoolean("contextOn");
            if (datas) {
                contextON_OFF = true;
                Toast.makeText(getBaseContext(),"Contextuality ON",Toast.LENGTH_SHORT).show();
            }
            else{
                contextON_OFF = false;
                Toast.makeText(getBaseContext(),"Contextuality OFF",Toast.LENGTH_SHORT).show();
            }
        }
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // If the request was to start activity recognition updates
                        if (ActivityUtils.REQUEST_TYPE.ADD == mRequestType) {

                            // Restart the process of requesting activity recognition updates
                            mDetectionRequester.requestUpdates();

                            // If the request was to remove activity recognition updates
                        } else if (ActivityUtils.REQUEST_TYPE.REMOVE == mRequestType ){

                                /*
                                 * Restart the removal of all activity recognition updates for the
                                 * PendingIntent.
                                 */
                            mDetectionRemover.removeUpdates(
                                    mDetectionRequester.getRequestPendingIntent());

                        }
                        break;

                    // If any other result was returned by Google Play services
                    default:

                        // Report that Google Play services was unable to resolve the problem.
                        Log.d(ActivityUtils.APPTAG, getString(R.string.no_resolution));
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.d(ActivityUtils.APPTAG,
                        getString(R.string.unknown_activity_request_code, requestCode));

                break;
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

               String songTitle = songsList.get(songIndex).get("songArtist")+": "+songsList.get(songIndex).get("songTitle");
               titleTv.setText(songTitle);
               makeNotification(songIndex,1);
               metaRetriver.setDataSource(songsList.get(songIndex).get("songPath"));
               try { byte [] art = metaRetriver.getEmbeddedPicture();
                   Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
                   albumArt.setImageBitmap(songImage);
//                album.setText(metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
//                artist.setText(metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
//                genre.setText(metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
               }
               catch (Exception e) {
                   albumArt.setImageDrawable(getDrawable(R.drawable.mustache_player3));
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
        makeNotification(0,2);
        mDetectionRequester.getRequestPendingIntent().cancel();
        super.onDestroy();
        mp.stop();
        mp.release();
    }
//
    @Override
    protected void onPause() {
        mBroadcastManager.unregisterReceiver(updateListReceiver);
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastManager.registerReceiver(updateListReceiver,mBroadcastFilter);
        onStartUpdates(getCurrentFocus());
        //sensorMan.registerListener(this, proxiSen, SensorManager.SENSOR_DELAY_NORMAL);
    }
//    @Override
//    public void onStop(){
//        super.onStop();
//        makeNotification(1,2);
//
//    }
    BroadcastReceiver updateListReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {

            /*
             * When an Intent is received from the update listener IntentService, update
             * the displayed log.
             */
        //updateActivityHistory();
        }
    };
    private boolean servicesConnected() {

    // Check that Google Play services is available
    int resultCode =
            GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

    // If Google Play services is available
    if (ConnectionResult.SUCCESS == resultCode) {

        // In debug mode, log the status
        Log.d(ActivityUtils.APPTAG, getString(R.string.play_services_available));

        // Continue
        return true;

        // Google Play services was not available for some reason
    } else {

        // Display an error dialog
        GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
        return false;
    }
}
    public void onStartUpdates(View view) {
        if (!servicesConnected()) {
            return;
        }
        mRequestType = ActivityUtils.REQUEST_TYPE.ADD;
        mDetectionRequester.requestUpdates();
    }
    public void onStopUpdates(View view) {

        if (!servicesConnected()) {

            return;
        }
        mRequestType = ActivityUtils.REQUEST_TYPE.REMOVE;
        mDetectionRemover.removeUpdates(mDetectionRequester.getRequestPendingIntent());
        mDetectionRequester.getRequestPendingIntent().cancel();
    }
    private void printActivityHistory() {

//        inVechicle.setText("In Vechicle: "+mLogFile.getMyActivityData().getInVechiclePercent());
//        onFoot.setText("On Foot: "+mLogFile.getMyActivityData().getOnFootPercent());
//        onBycicle.setText("On Bycicle: "+mLogFile.getMyActivityData().getOnBicyclePercent());
//        still.setText("Still: "+mLogFile.getMyActivityData().getStillPercent());
//        unknown.setText("Unknown: "+mLogFile.getMyActivityData().getUnknownPercent());
//        tilting.setText("Tilting: "+mLogFile.getMyActivityData().getTiltingPercent());
        Log.i("MustachePlayer",
                "V: "+mLogFile.getMyActivityData().getInVechiclePercent()+
                "F: "+mLogFile.getMyActivityData().getOnFootPercent()+
                "B: "+mLogFile.getMyActivityData().getOnBicyclePercent()+
                "S: "+mLogFile.getMyActivityData().getStillPercent()+
                "U: "+mLogFile.getMyActivityData().getUnknownPercent()+
                "T: "+mLogFile.getMyActivityData().getTiltingPercent()
        );

    }

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
    private class makeBPM_PlaylistInBack extends AsyncTask<String,Integer,ArrayList<HashMap<String,String>>> {


        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(String... params) {
            return songFinder.getTracks(getContentResolver(),0);
        }
        @Override
        protected void onPostExecute(ArrayList<HashMap<String,String>> result) {
            songsListWithBPM=result;
            Toast.makeText(getBaseContext(),"BPM List Loaded",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            makeNotification(0,2);
            finish();
            System.exit(0);
        }
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            i.putExtra("contextOn",contextON_OFF);
            startActivityForResult(i, 200);
        }
        if (id== R.id.action_playlist){
            Intent i = new Intent(getApplicationContext(), PlaylistActivity.class);
            i.putExtra("currentSongIndex",currentSongIndex);
            startActivityForResult(i, 100);
        }
        return super.onOptionsItemSelected(item);
    }



    public void makeNotification(int songIndex,int type){
        if(type==1){
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder nBuilder  = new NotificationCompat.Builder(getBaseContext())
                    .setCategory(Notification.CATEGORY_EVENT)
                    .setContentTitle("CMP Playing")
                    .setContentText(songsList.get(songIndex).get("songArtist") + ": " + songsList.get(songIndex).get("songTitle"))
                    .setSmallIcon(R.mipmap.ic_stat_mustache)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setAutoCancel(false);



            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.setAction(Intent.ACTION_MAIN);
            resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    resultIntent, 0);

            nBuilder.setContentIntent(pendingIntent);

            notificationManager.notify(10, nBuilder.build());
        }
        if(type==2){
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder nBuilder  = new NotificationCompat.Builder(getBaseContext())
                    .setCategory(Notification.CATEGORY_EVENT)
                    .setContentTitle("CMP Not Playing")
                    .setContentText("Please press notification to restart playing")
                    .setSmallIcon(R.mipmap.ic_stat_mustache)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setAutoCancel(false);

            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.setAction(Intent.ACTION_MAIN);
            resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    resultIntent, 0);

            nBuilder.setContentIntent(pendingIntent);
            notificationManager.notify(10, nBuilder.build());
        }

    }

}
