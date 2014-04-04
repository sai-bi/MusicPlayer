package com.example.musicplayer.app;

import android.app.IntentService;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class PlayMusic extends IntentService {


    public PlayMusic(String name) {
        super(name);
    }
    public PlayMusic(){
        super("PlayMusic");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra("url");
        String title = intent.getStringExtra("title");
        MediaPlayer player = new MediaPlayer();


        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            player.setDataSource(url);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
