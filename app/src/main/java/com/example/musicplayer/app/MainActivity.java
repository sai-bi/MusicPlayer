package com.example.musicplayer.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    private static final String AUTHORITY = "com.example.musicplayer.app.content";
    static private final Uri provider;

    static {
        provider = Uri.parse("content://" + AUTHORITY);
    }

    private String song_name;
    private String song_url;
    private ArrayList<String> song_list;
    private ArrayList<String> url_list;
    private ArrayAdapter<String> song_list_view_adapter;
    private int current_index;
    private Boolean is_playing;
    private ArrayList<Integer> id_list;
    private int current_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button add_button = (Button) (findViewById(R.id.btn_add));
        final Button list_button = (Button) (findViewById(R.id.btn_play_list));
        final Button next_button = (Button) (findViewById(R.id.btn_next));
        final Button pre_button = (Button) (findViewById(R.id.btn_previous));
        final Button play_button = (Button) (findViewById(R.id.btn_play));
        final TextView text_view_song_title = (TextView) (findViewById(R.id.text_view_song_title));
        final TextView text_view_status = (TextView) (findViewById(R.id.text_view_status));


        StatusReceiver statusReceiver = new StatusReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("status_message");
        MainActivity.this.registerReceiver(statusReceiver, filter);


        Intent intent = new Intent();
        intent.setClass(MainActivity.this, PlayMusic.class);
        startService(intent);

        song_name = "";
        song_url = "";
        song_list = new ArrayList<String>();
        url_list = new ArrayList<String>();
        id_list = new ArrayList<Integer>();
        current_index = -1;
        is_playing = false;


        getSongList();
        setUpListView();
        setUpSearch();


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
                        //database.execSQL("insert into SONG_LIST values(null,?,?)", new Object[]{song_name, song_url});
                        try {
                            ContentValues values = new ContentValues();
                            values.put("title", song_name);
                            values.put("url", song_url);
                            getContentResolver().insert(provider, values);
                            Cursor c = getContentResolver().query(provider, null, null, null, null);
                            c.moveToNext();
                            int index = c.getColumnIndex("_id");
                            id_list.add(c.getInt(index));
                            song_list.add(song_name);
                            url_list.add(song_url);
                            //Log.e("Add",song_name);
                            song_list_view_adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

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

        play_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (song_list.size() == 0)
                    return;
                if (is_playing) {
                    text_view_status.setText("Pause");
                    is_playing = false;
                    Intent intent = new Intent();
                    intent.putExtra("stop", true);
                    intent.setAction("player_command");
                    sendBroadcast(intent);
                    play_button.setBackgroundResource(R.drawable.play_button);
                    return;
                } else {

                    is_playing = true;
                    play_button.setBackgroundResource(R.drawable.pause);
                    Intent intent = new Intent();
                    intent.setAction("player_command");

                    if (current_index == -1) {
                        text_view_status.setText("Loading...");
                        current_index = 0;
                        intent.putExtra("url", url_list.get(current_index));
                        intent.putExtra("title", song_list.get(current_index));
                        sendBroadcast(intent);
                        text_view_song_title.setText(song_list.get(current_index));
                    } else {
                        text_view_status.setText("Playing...");
                        intent.putExtra("resume", true);
                        sendBroadcast(intent);
                    }
                }
            }
        });

        next_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (song_list.size() == 0)
                    return;
                current_index = current_index + 1;
                current_index = current_index % song_list.size();
                Intent intent = new Intent();
                intent.setAction("player_command");
                intent.putExtra("url", url_list.get(current_index));
                intent.putExtra("title", song_list.get(current_index));
                sendBroadcast(intent);
                text_view_song_title.setText(song_list.get(current_index));
                play_button.setBackgroundResource(R.drawable.pause);
                is_playing = true;
            }
        });

        pre_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (song_list.size() == 0)
                    return;
                if (current_index == -1) {
                    current_index = 0;
                }
                current_index = current_index - 1;
                current_index = (current_index + song_list.size()) % song_list.size();
                Intent intent = new Intent();
                intent.setAction("player_command");
                intent.putExtra("url", url_list.get(current_index));
                intent.putExtra("title", song_list.get(current_index));
                sendBroadcast(intent);
                text_view_song_title.setText(song_list.get(current_index));
                play_button.setBackgroundResource(R.drawable.pause);
                is_playing = true;
            }
        });

    }

    public void getSongList() {


        //Cursor c = database.rawQuery("select * from SONG_LIST", null);
        Cursor c = getContentResolver().query(provider, null, null, null, null);
        while (c.moveToNext()) {
            int title_index = c.getColumnIndex("title");
            String title = c.getString(title_index);
            song_list.add(title);
            int url_index = c.getColumnIndex("url");
            String url = c.getString(url_index);
            url_list.add(url);
            int id_index = c.getColumnIndex("_id");
            int id = c.getInt(id_index);
            id_list.add(id);
        }
    }

    public void setUpListView() {
        final ListView list_view_song = (ListView) (findViewById(R.id.list_view_song));
        song_list_view_adapter = new ArrayAdapter<String>(this, R.layout.list_view_song, song_list);
        list_view_song.setAdapter(song_list_view_adapter);

        list_view_song.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == current_index) {
                    return;
                }
                current_index = i;
                Intent intent = new Intent();
                intent.setAction("player_command");
                intent.putExtra("url", url_list.get(i));
                intent.putExtra("title", song_list.get(i));
                sendBroadcast(intent);
                Log.d("send", "send a broadcast");
                TextView v = (TextView) (findViewById(R.id.text_view_song_title));
                v.setText(song_list.get(i));
                TextView text_view_status = (TextView) (findViewById(R.id.text_view_status));
                text_view_status.setText("Loading...");
                Button btn_play = (Button) (findViewById(R.id.btn_play));
                btn_play.setBackgroundResource(R.drawable.pause);
                is_playing = true;
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view_add_song = inflater.inflate(R.layout.update_dialog, null);

        builder.setView(view_add_song)
                // Add action buttons
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText input_song_name = (EditText) (view_add_song.findViewById(R.id.update_song_name));
                        EditText input_song_url = (EditText) (view_add_song.findViewById(R.id.update_song_url));
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
                        //database.execSQL("insert into SONG_LIST values(null,?,?)", new Object[]{song_name, song_url});
                        ContentValues values = new ContentValues();
                        values.put("url", song_url);
                        values.put("title", song_name);
                        String[] selection_args = {song_name, song_url, id_list.get(current_id).toString()};
                        //selection_args[0] = Integer.toString(current_id);

                        getContentResolver().update(provider, values, "_id = ?", selection_args);

                        song_list.set(current_id, song_name);
                        url_list.set(current_id, song_url);
                        song_list_view_adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        list_view_song.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {


                String[] choice = {"Update", "Delete"};
                final int index = i;
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                current_id = i;

                builder1.setTitle("Choose")
                        .setItems(choice, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 1) {
                                    if (current_index > index) {
                                        current_index = index - 1;
                                    }
                                    try {
                                        //database.execSQL("Delete from SONG_LIST where _id = " + id_list.get(index).toString());
                                        String selection_clause = "_id = ?";
                                        String[] selection_args = {""};
                                        selection_args[0] = id_list.get(index).toString();
                                        getContentResolver().delete(provider, selection_clause, selection_args);

                                        song_list.remove(index);
                                        url_list.remove(index);
                                        id_list.remove(index);
                                        song_list_view_adapter.notifyDataSetChanged();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if (which == 0) {
                                    builder.create().show();
                                }
                            }
                        });

                builder1.create().show();

                return true;
            }
        });


    }

    public void setUpSearch() {


        final Button btn_search = (Button) (findViewById(R.id.btn_search));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view_search_song = inflater.inflate(R.layout.search_dialog, null);

        builder.setView(view_search_song)
                // Add action buttons
                .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText song_title = (EditText) (view_search_song.findViewById(R.id.input_search_song_title));
                        String song_name;
                        if (song_title.getText() != null) {
                            song_name = song_title.getText().toString();
                        } else {
                            return;
                        }

                        final int index = song_list.indexOf(song_name);
                        if (index == -1) {
                            AlertDialog.Builder my_builder = new AlertDialog.Builder(MainActivity.this);
                            my_builder.setTitle("Search: " + song_name);
                            my_builder.setMessage("Not found!");
                            my_builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            my_builder.create().show();
                        } else {
                            AlertDialog.Builder my_builder = new AlertDialog.Builder(MainActivity.this);
                            my_builder.setTitle("Search: " + song_name);
                            my_builder.setMessage("URL: " + url_list.get(index));
                            my_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            my_builder.setPositiveButton("Play", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent();
                                    intent.putExtra("url", url_list.get(index));
                                    intent.setAction("player_command");
                                    sendBroadcast(intent);
                                    is_playing = true;

                                    TextView v = (TextView) (findViewById(R.id.text_view_song_title));
                                    v.setText(song_list.get(index));
                                    TextView text_view_status = (TextView) (findViewById(R.id.text_view_status));
                                    text_view_status.setText("Loading...");
                                    Button btn_play = (Button) (findViewById(R.id.btn_play));
                                    btn_play.setBackgroundResource(R.drawable.pause);
                                }
                            });
                            my_builder.create().show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        final Dialog dialog_search = builder.create();
        dialog_search.setCanceledOnTouchOutside(false);

        btn_search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_search.show();
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

    class StatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra("status", 0);
            // load complete
            TextView text_view_status = (TextView) (findViewById(R.id.text_view_status));
            if (status == 0) {
                text_view_status.setText("Playing...");
            } else {
                text_view_status.setText("Fail to load the music...");
                is_playing = false;
                Button play_button = (Button) (findViewById(R.id.btn_play));
                play_button.setBackgroundResource(R.drawable.play_button);
            }
        }
    }


}
