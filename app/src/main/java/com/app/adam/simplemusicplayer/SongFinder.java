package com.app.adam.simplemusicplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Path;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v24Frames;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Adam on 3/13/2015.
 */
public class SongFinder {
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<>();
    public SongFinder(){
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    };

    /**
     * Returns the full media library with track id,artist,title,data(song path),album
     * @param crIn
     * @param type
     * @return
     */
    public ArrayList<HashMap<String, String>> getTracks(ContentResolver crIn,int type){
        ContentResolver cr = crIn;

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM
        };
        Cursor cur = cr.query(uri, projection, selection, null, sortOrder);
        int count = 0;
        if(cur != null && type==0)
        {
            count = cur.getCount();

            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String id=cur.getString(0);
                    String artist=cur.getString(1);
                    String title=cur.getString(2);
                    String data=cur.getString(3);
                    HashMap<String,String> tmp=new HashMap<>();
                    tmp.put("songId",id);
                    tmp.put("songArtist",artist);
                    tmp.put("songTitle",title);
                    tmp.put("songPath",data);
                    tmp.put("songBPM",getBPMtagForSong(data));
                    songsList.add(tmp);
                }
            }
        }
        if(cur != null && type==1)
        {
            count = cur.getCount();

            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String id=cur.getString(0);
                    String artist=cur.getString(1);
                    String title=cur.getString(2);
                    String data=cur.getString(3);
                    HashMap<String,String> tmp=new HashMap<>();
                    tmp.put("songId",id);
                    tmp.put("songArtist",artist);
                    tmp.put("songTitle",title);
                    tmp.put("songPath",data);
                    songsList.add(tmp);
                }
            }
        }
        if(cur != null && type==2)
        {
            count = cur.getCount();
            HashMap<Integer,String> tempArtList = new HashMap<>();
            int i=0;
            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String artist=cur.getString(1);
                    if(!tempArtList.containsValue(artist)){
                        HashMap<String,String> tmp=new HashMap<>();
                        //Log.i("SongFinder",artist);
                        tempArtList.put(i++,artist);
                        tmp.put("songArtist", artist);
                        songsList.add(tmp);
                    }
                }
            }
        }
        if(cur != null && type==3)
        {
            count = cur.getCount();
            HashMap<Integer,String> tempAlbList = new HashMap<>();
            int i=0;
            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String album=cur.getString(4);
                    String artist=cur.getString(1);
                    //Log.i("SongFinder",album);
                    if(!tempAlbList.containsValue(album)){
                        HashMap<String,String> tmp=new HashMap<>();
                        tempAlbList.put(i++,album);
                        tmp.put("songAlbum",album);
                        tmp.put("songArtist",artist);
                        songsList.add(tmp);
                    }
                }
            }
        }

        cur.close();
        return songsList;
    }
    public ArrayList<HashMap<String, String>> getTracksByArtist(ContentResolver crIn,String artistIn){
        ContentResolver cr = crIn;

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM
        };
        Cursor cur = cr.query(uri, projection, selection, null, sortOrder);
        int count = 0;

        if(cur != null )
        {
            count = cur.getCount();
            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String artist=cur.getString(1);
                    String title=cur.getString(2);
                    if(artist.equals(artistIn)) {
                        HashMap<String,String> tmp=new HashMap<>();
                        tmp.put("songTitle",title);
                        tmp.put("songArtist",artist);
                        songsList.add(tmp);
                    }

                }
            }
        }
        cur.close();
        return songsList;
    }
    public ArrayList<HashMap<String, String>> getTracksByAlbumAndArtist(ContentResolver crIn,String artistIn,String albumIn){
        ContentResolver cr = crIn;

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM
        };
        Cursor cur = cr.query(uri, projection, selection, null, sortOrder);
        int count = 0;

        if(cur != null )
        {
            count = cur.getCount();
            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String artist=cur.getString(1);
                    String title=cur.getString(2);
                    String album=cur.getString(4);
                    if(artist.equals(artistIn) && album.equals(albumIn)) {
                        HashMap<String,String> tmp=new HashMap<>();
                        tmp.put("songTitle",title);
                        tmp.put("songArtist",artist);
                        songsList.add(tmp);
                    }

                }
            }
        }
        cur.close();
        return songsList;
    }
    public int getTrackByArtistAndTitle(ContentResolver crIn,String artistIn,String titleIn){
        ContentResolver cr = crIn;

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM
        };
        Cursor cur = cr.query(uri, projection, selection, null, sortOrder);
        int count = 0;

        if(cur != null )
        {
            count = cur.getCount();
            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String artist=cur.getString(1);
                    String title=cur.getString(2);
                    HashMap<String,String> tmp=new HashMap<>();
                    tmp.put("songTitle",title);
                    tmp.put("songArtist",artist);
                    songsList.add(tmp);


                }
            }
        }
        cur.close();
        int tmp=0;
        for(int i=0;i<songsList.size();i++){
            if(songsList.get(i).get("songTitle").equals(titleIn) && songsList.get(i).get("songArtist").equals(artistIn)){
                tmp=i;
                break;
            }

        }
        return tmp;
    }
    private String getBPMtagForSong(String path){
      String tmp="";
        MP3File f      = null;

        try {
            File file=new File(path);
            f = (MP3File) AudioFileIO.read(file);
            Tag tag        = f.getTag();
            AbstractID3v2Tag v2tag  = f.getID3v2Tag();
            tmp=v2tag.getFirst(ID3v24Frames.FRAME_ID_BPM);

        } catch (CannotReadException e) {

        } catch (IOException e) {

        } catch (TagException e) {

        } catch (ReadOnlyFileException e) {

        } catch (InvalidAudioFrameException e) {

        }
      return tmp;
    }
    public void printSongList(){
        for(int i=0;i<songsList.size();i++){
            Log.i("Songlist",songsList.get(i).get("songTitle")+" "+songsList.get(i).get("songBPM"));
        }
    }


}
