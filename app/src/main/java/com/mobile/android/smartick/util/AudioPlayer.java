package com.mobile.android.smartick.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.mobile.android.smartick.R;

import java.io.IOException;

/**
 * Created by sbarrio on 20/03/15.
 */
public class AudioPlayer {

    private Context context = null;
    private MediaPlayer player = null;

    public void init(Context ctx){
        context = ctx;
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    //play from file
    public void playFile(Uri path){
        if (player == null){
            Log.d(Constants.AUDIO_LOG_TAG, "Player is null. Call init() before using it! ");
            return;
        }
        try {
            player.setDataSource(context, path);
            player.prepare();
            player.start();
        } catch (IOException e) {
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
        }
    }

    //bind callback

    //unbind callback

    //release
    public void release(){
        if (player == null){
            Log.d(Constants.AUDIO_LOG_TAG, "Player is null. Can't release.");
            return;
        }
        player.release();
        player = null;
    }
}
