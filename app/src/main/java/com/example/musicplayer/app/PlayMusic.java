package com.example.musicplayer.app;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class PlayMusic extends Service {

    private MediaPlayer player;
    private String url;
    private String title;
    private boolean playing_now;
    private int current_length;
    private CommandReceiver command_receiver;


    @Override
    public void onCreate() {
        player = new MediaPlayer();
        url = "";
        title = "";
        playing_now = false;
        command_receiver = new CommandReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("player_command");
        PlayMusic.this.registerReceiver(command_receiver,filter);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return super.onStartCommand(intent,flags,startId);
    }

    public void playMusic(String url, String title) {
        player.reset();
        this.url = url;
        this.title = title;
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        new AsyncPlayMusicTask().execute();
    }

    class AsyncPlayMusicTask extends AsyncTask<Void,Void,Void>{


        @Override
        protected Void doInBackground(Void... voids) {
            try{
                player.setDataSource(url);
                player.prepare();
                player.start();
                Intent intent = new Intent();
                intent.setAction("status_message");
                intent.putExtra("status",0);
                sendBroadcast(intent);
            } catch (Exception e){
                e.printStackTrace();
                Intent intent = new Intent();
                intent.setAction("status_message");
                intent.putExtra("status",-1);
                sendBroadcast(intent);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){

        }
    }


    @Override
    public void onDestroy(){
        PlayMusic.this.unregisterReceiver(command_receiver);
        player.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class CommandReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("receive","receiver a broadcast");
            Bundle bundle = intent.getExtras();
            boolean resume = bundle.getBoolean("resume", false);
            boolean stop = bundle.getBoolean("stop", false);
            String input_url = bundle.getString("url");
            String input_title = bundle.getString("title");
            if (input_url == null) {
                input_url = "";
            }
            if (input_title == null) {
                input_title = "";
            }
            if (stop) {
                player.pause();
                current_length = player.getCurrentPosition();
                return;
            }
            if (resume) {
                player.seekTo(current_length);
                player.start();
                return;
            }

            playMusic(input_url, input_title);

        }
    }


}
