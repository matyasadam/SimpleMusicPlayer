package com.app.adam.simplemusicplayer;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;


public class PlaylistActivity extends ListActivity {

    public ArrayList<HashMap<String, String>> songsList = new ArrayList<>();
    private Button songsButton,artistButton,albumButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);

        songsButton = (Button)findViewById(R.id.songsButton);
        artistButton = (Button)findViewById(R.id.artistButton);
        albumButton = (Button)findViewById(R.id.albumButton);

        ArrayList<HashMap<String, String>> songsListData = new ArrayList<HashMap<String, String>>();
        Intent myIntent=getIntent();
        final int pos=myIntent.getIntExtra("currentSongIndex",0);
        SongFinder sf = new SongFinder();

        makeSongList(songsListData,sf,pos);

        songsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeSongList(new ArrayList<HashMap<String, String>>(),new SongFinder(),pos);
            }
        });
        artistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeArtistList(new ArrayList<HashMap<String, String>>(), new SongFinder());
            }
        });
        albumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeAlbumList(new ArrayList<HashMap<String, String>>(),new SongFinder());
            }
        });
    }
    private void makeSongList(ArrayList<HashMap<String, String>> songsListData,SongFinder sf,int pos){
        songsList = sf.getTracks(getContentResolver(),1);

        for (int i = 0; i < songsList.size(); i++) {
            HashMap<String, String> song = songsList.get(i);
            songsListData.add(song);
        }
        songsListData.get(pos).put("songNowPlaying","Now Playing");
        ListAdapter adapter = new SimpleAdapter(this, songsListData,
                R.layout.playlist_item, new String[] { "songArtist","songTitle","songNowPlaying" },
                new int[] {R.id.songArtist, R.id.songTitle,R.id.songNowPlaying });
        setListAdapter(adapter);
        getListView().setSelection(pos);
        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                int songIndex = position;

                Intent in = new Intent(getApplicationContext(),MainActivity.class);
                in.putExtra("songIndex", songIndex);
                setResult(100, in);
                finish();
            }
        });
    }
    private void makeArtistList(final ArrayList<HashMap<String, String>> songsListData,SongFinder sf){
        songsList = sf.getTracks(getContentResolver(),2);

        for (int i = 0; i < songsList.size(); i++) {
            HashMap<String, String> song = songsList.get(i);
            songsListData.add(song);
        }
        ListAdapter adapter = new SimpleAdapter(this, songsListData,
                R.layout.playlist_item, new String[] { "songArtist" },
                new int[] {R.id.songTitle});
        setListAdapter(adapter);
        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                makeArtistSongList(new ArrayList<HashMap<String, String>>(),new SongFinder(),songsListData.get(position).get("songArtist"));
            }
        });
    }
    private void makeAlbumList(final ArrayList<HashMap<String, String>> songsListData,SongFinder sf) {
        songsList = sf.getTracks(getContentResolver(), 3);

        for (int i = 0; i < songsList.size(); i++) {
            HashMap<String, String> song = songsList.get(i);
            songsListData.add(song);
        }
        ListAdapter adapter = new SimpleAdapter(this, songsListData,
                R.layout.playlist_item, new String[]{"songAlbum", "songArtist"},
                new int[]{R.id.songTitle, R.id.songArtist});
        setListAdapter(adapter);
        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                makeAlbumSongList(new ArrayList<HashMap<String, String>>(),new SongFinder(),songsListData.get(position).get("songArtist"),songsListData.get(position).get("songAlbum"));
            }
        });
    }
    private void makeArtistSongList(final ArrayList<HashMap<String, String>> songsListData,SongFinder sf,String artistIn){
        songsList = sf.getTracksByArtist(getContentResolver(), artistIn);

        for (int i = 0; i < songsList.size(); i++) {
            HashMap<String, String> song = songsList.get(i);
            songsListData.add(song);
        }
        ListAdapter adapter = new SimpleAdapter(this, songsListData,
                R.layout.playlist_item, new String[] { "songTitle","songArtist" },
                new int[] {R.id.songTitle,R.id.songArtist});
        setListAdapter(adapter);
        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                int songIndex = new SongFinder().getTrackByArtistAndTitle(getContentResolver(), songsListData.get(position).get("songArtist"), songsListData.get(position).get("songTitle"));
                Intent in = new Intent(getApplicationContext(),MainActivity.class);
                in.putExtra("songIndex", songIndex);
                setResult(100, in);
                finish();
            }
        });
    }
    private void makeAlbumSongList(final ArrayList<HashMap<String, String>> songsListData,SongFinder sf,String albumIn,String artistIn){
        songsList = sf.getTracksByAlbumAndArtist(getContentResolver(), albumIn, artistIn);

        for (int i = 0; i < songsList.size(); i++) {
            HashMap<String, String> song = songsList.get(i);
            songsListData.add(song);
        }
        ListAdapter adapter = new SimpleAdapter(this, songsListData,
                R.layout.playlist_item, new String[] { "songTitle","songArtist" },
                new int[] {R.id.songTitle,R.id.songArtist});
        setListAdapter(adapter);
        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                int songIndex = new SongFinder().getTrackByArtistAndTitle(getContentResolver(),songsListData.get(position).get("songArtist"),songsListData.get(position).get("songTitle"));
                Intent in = new Intent(getApplicationContext(),MainActivity.class);
                in.putExtra("songIndex", songIndex);
                setResult(100, in);
                finish();
            }
        });
    }
}
