package com.example.musicplayer.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    private String song_name;
    private String song_url;
    private ArrayList<String> song_list;
    private ArrayList<String> url_list;
    private SQLiteDatabase database;
    private ArrayAdapter<String> song_list_view_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button add_button = (Button) (findViewById(R.id.btn_add));
        final Button list_button = (Button) (findViewById(R.id.btn_play_list));


        song_name = "";
        song_url = "";
        song_list = new ArrayList<String>();
        url_list = new ArrayList<String>();


        song_list.add("让她降落");
        url_list.add("http://i.cs.hku.hk/fyp/2013/fyp13027/music/test.mp3");


        getSongList();
        setUpListView();


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View view_add_song = inflater.inflate(R.layout.mydialog, null);

        builder.setView(view_add_song)
                // Add action buttons
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText input_song_name = (EditText) (view_add_song.findViewById(R.id.input_song_name));
                        EditText input_song_url = (EditText) (view_add_song.findViewById(R.id.input_song_url));
                        if (input_song_name.getText() != null) {
                            song_name = input_song_name.getText().toString();
                        } else {
                            return;
                        }
                        if (input_song_url.getText() != null) {
                            song_url = input_song_url.getText().toString();
                        } else {
                            return;
                        }
                        database.execSQL("insert into SONG_LIST values(null,?,?)", new Object[]{song_name, song_url});
                        song_list.add(song_name);
                        url_list.add(song_url);
                        //Log.e("Add",song_name);
                        song_list_view_adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        final Dialog dialog_add_song = builder.create();
        dialog_add_song.setCanceledOnTouchOutside(false);


        add_button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                dialog_add_song.show();
            }
        });

        list_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListView v = (ListView) (findViewById(R.id.list_view_song));
                if (v.getVisibility() == View.GONE) {
                    v.setVisibility(View.VISIBLE);
                } else {
                    v.setVisibility(View.GONE);
                }
            }
        });


    }

    public void getSongList() {
        database = openOrCreateDatabase("song.db", Context.MODE_PRIVATE, null);
        try {
            database.execSQL("create table SONG_LIST(_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT)");
        } catch (Exception e) {
            // table already exists
        }
        Cursor c = database.rawQuery("select * from SONG_LIST", null);
        while (c.moveToNext()) {
            int title_index = c.getColumnIndex("title");
            String title = c.getString(title_index);
            song_list.add(title);
            int url_index = c.getColumnIndex("url");
            String url = c.getString(url_index);
            url_list.add(url);
        }
    }

    public void setUpListView() {
        final ListView list_view_song = (ListView) (findViewById(R.id.list_view_song));
        song_list_view_adapter = new ArrayAdapter<String>(this, R.layout.list_view_song, song_list);
        list_view_song.setAdapter(song_list_view_adapter);

        list_view_song.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getBaseContext(), PlayMusic.class);
                intent.putExtra("url",url_list.get(i));
                intent.putExtra("title",song_list.get(i));
                startService(intent);
            }
        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
