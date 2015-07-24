package com.mobile.android.smartick.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.pojos.Installation;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sbarrio on 20/03/15.
 */
public class AudioPlayer {

    private Context context = null;
    public MediaPlayer player = null;
    private File audioCache;

    private final int MAX_SIZE_AUDIO_CACHE = 1024 * 1024; //1MB
    private final int MAX_DAYS_CACHE_REFRESH = 5;

    public void init(Context ctx){
        context = ctx;
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        createOrInitAudioCache(ctx.getExternalCacheDir());
    }

    //play from file
    public void playFile(Uri path){
        if (player == null){
            Log.d(Constants.AUDIO_LOG_TAG, "Player is null. Call init() before using it! ");
            return;
        }
        try {
            player.reset();
            player.setDataSource(context, path);
            player.prepare();
            player.start();


        }catch (IllegalStateException e) {
            Log.d(Constants.AUDIO_LOG_TAG, "IllegalStateException: " + e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e) {
            Log.d(Constants.AUDIO_LOG_TAG, "IOException: " + e.getMessage());
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            Log.d(Constants.AUDIO_LOG_TAG, "IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
        }
        catch (SecurityException e) {
            Log.d(Constants.AUDIO_LOG_TAG, "SecurityException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //play from url
    public void playURL(String url){
        if (player == null){
            Log.d(Constants.AUDIO_LOG_TAG, "Player is null. Call init() before using it! ");
            return;
        }
        try {
            player.reset();

            //check if file exists
            File f = retrieveAudioFileFromCache(url);
            if (f != null){
                Log.d(Constants.AUDIO_LOG_TAG, url + " found on cache, playing from local");
                Uri uri = Uri.fromFile(f);
                playFile(uri);
                return;
            }

            Log.d(Constants.AUDIO_LOG_TAG, url + " NOT found on cache, storing and playing");
            //file does not exist, store and play from url
            storeAudioFileOnAudioCache(null,url);
            player.setDataSource(url);
            player.prepare(); // might take long! (for buffering, etc)
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //stop sound
    public void stop(){
        if (player == null){
            Log.d(Constants.AUDIO_LOG_TAG, "Player is null. Call init() before using it! ");
            return;
        }
        if (player.isPlaying()){
            player.stop();
            player.reset();
        }
    }

    //release
    public void release(){
        if (player == null){
            Log.d(Constants.AUDIO_LOG_TAG, "Player is null. Can't release.");
            return;
        }
        player.release();
        player = null;
    }

//Callbacks

    public void setPlayerCallbacks(MediaPlayer player,MediaPlayer.OnCompletionListener completionListener){
        player.setOnCompletionListener(completionListener);
        player.setOnErrorListener(new MediaPlayer.OnErrorListener(){
            public boolean onError(MediaPlayer mp,int what, int extra){
                return playerReceivedError(what,extra);
            }
        });
    }

    public void finishedPlayback(){
        Log.d(Constants.AUDIO_LOG_TAG, "Player has finished playback!");
        player.reset();
    }

    public boolean playerReceivedError(int what, int extra){
        Log.d(Constants.AUDIO_LOG_TAG, "Player has received an error!" + what + " " + extra);
        return true;
    }


//Cache

    public void createOrInitAudioCache(File parentPath){
        audioCache = new File(parentPath, "/smk_audio_cache/");

        //audio cache does no exist -> create
        if (!audioCache.exists()){
            Log.d(Constants.AUDIO_LOG_TAG, "Audio Cache didn't exist, creating directory");
            audioCache.mkdirs();
        }

        Date lastRefreshDate = getAudioCacheLastRefreshDate();
        Calendar currentTime = Calendar.getInstance();
        currentTime.add(Calendar.DAY_OF_YEAR,-MAX_DAYS_CACHE_REFRESH);

        //cache outdated -> refresh
        if (currentTime.getTime().after(lastRefreshDate)){
            Log.d(Constants.AUDIO_LOG_TAG, "Audio Cache outdated, refreshing");
            //refresh cache
            storeAudioCacheLastRefreshDate(new Date());
            clearAudioCache();
        }

        //cache oversized -> refresh
        if (getAudioCacheSize() > MAX_SIZE_AUDIO_CACHE){
            Log.d(Constants.AUDIO_LOG_TAG, "Audio Cache oversized, refreshing");
            clearAudioCache();
        }
    }

    public void clearAudioCache(){
        if (audioCache!= null && audioCache.exists() && audioCache.isDirectory()){
            File files[] = audioCache.listFiles();
            for (File f: files){
                f.delete();
            }
        }
    }

    public void storeAudioFileOnAudioCache(File file, String url){
        if (audioCache != null){
            Log.d(Constants.AUDIO_LOG_TAG, "Storing file " + url + "on audio cache");

            //file already existed?
            File f = retrieveAudioFileFromCache(url);
            if (f!= null && f.exists()){
                f.delete();
            }

            //start download and store new audio file
            new DownloadFile().execute(url);
        }
    }

    public File retrieveAudioFileFromCache(String url){
        if (audioCache != null){
            File[] fileList = audioCache.listFiles();
            String fileName = getFileNameFromUrl(url);
            if (fileList != null){
                for (File f: fileList){
                    if (f.getName().equals(fileName)){
                        Log.d(Constants.AUDIO_LOG_TAG, fileName + " found on audio cache");
                        return f;
                    }
                }
            }
        }
        return null;
    }

    public long getAudioCacheSize(){
        if (audioCache != null && audioCache.isDirectory()){
            long result = 0;
            File[] fileList = audioCache.listFiles();
            for (File f: fileList){
                result += f.length();
            }
            Log.d(Constants.AUDIO_LOG_TAG, "Audio cache size is " + result);
            return result;
        }
        Log.d(Constants.AUDIO_LOG_TAG, "Audio cache size is 0");
        return 0;
    }

//File download

    private class DownloadFile extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... url) {
            int count;
            try {
                Log.d(Constants.AUDIO_LOG_TAG, "Downloading audio file: " + url[0]);
                URL urlDownload = new URL(url[0]);
                URLConnection conn = urlDownload.openConnection();
                conn.connect();

                // this will be useful so that you can show a tipical 0-100% progress bar
                int lengthOfFile = conn.getContentLength();

                //cache over sized?
                if (getAudioCacheSize() + lengthOfFile > MAX_SIZE_AUDIO_CACHE){
                    clearAudioCache();
                }

                // download the file
                InputStream input = new BufferedInputStream(urlDownload.openStream());
                String fileName = getFileNameFromUrl(url[0]);
                File file = new File(audioCache, fileName);
                OutputStream output = new FileOutputStream(file);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    publishProgress((int)(total*100/lengthOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                Log.d(Constants.AUDIO_LOG_TAG, fileName + " created on audio cache");
            } catch (Exception e) {
                Log.d(Constants.AUDIO_LOG_TAG,e.toString());
            }
            return null;
        }
    }

// Last cache update from preferences
    private Date getAudioCacheLastRefreshDate(){
        if (context == null){
            return null;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SMARTICK_PREFS, Context.MODE_PRIVATE);
        long ms = sharedPreferences.getLong(Constants.AUDIO_CACHE_LAST_REFRESH_DATE, 0L);
        Date date = new Date(ms);
        Log.d(Constants.AUDIO_LOG_TAG, "Last Audio Cache Refresh date is: " + date);
        return date;
    }

    private void storeAudioCacheLastRefreshDate(Date date){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SMARTICK_PREFS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(Constants.AUDIO_CACHE_LAST_REFRESH_DATE, date.getTime());
        editor.commit();
        Log.d(Constants.AUDIO_LOG_TAG, "Storing new Audio Cache Refresh date as: " + date);
    }


//Util
    private String getFileNameFromUrl(String url){
        return url.substring( url.lastIndexOf('/')+1, url.length());
    }


}
