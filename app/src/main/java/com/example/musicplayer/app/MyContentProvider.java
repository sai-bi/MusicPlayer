package com.example.musicplayer.app;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.auth.AUTH;

public class MyContentProvider extends ContentProvider {
    private SQLiteDatabase database;
    private static final String AUTHORITY = "com.example.musicplayer.app.content";
    private static final int SONG_ID = 1;
    private static final String PATH = "SONG";

    private static final UriMatcher uri_matcher;
    static{
        uri_matcher = new UriMatcher(UriMatcher.NO_MATCH);
        uri_matcher.addURI(AUTHORITY,PATH,SONG_ID);
    }
    public MyContentProvider() {

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        database.execSQL("Delete from SONG_LIST where _id = '" + selectionArgs[0] + "'");
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        switch(uri_matcher.match(uri)){
            case SONG_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + AUTHORITY + PATH;
            default:
                throw new IllegalArgumentException("unsupported uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        long row_id = database.insert("SONG_LIST", "", values);

        if(row_id > 0)
        {
            Uri content_uri = Uri.parse("content://" + AUTHORITY);
            Uri _uri = ContentUris.withAppendedId(content_uri, row_id);
            getContext().getContentResolver().notifyChange(_uri, null);

            return _uri;
        }
        else
        {
            throw new SQLException("failed to insert row into " + uri);
        }
    }



    @Override
    public boolean onCreate() {
        database = new DatabaseHelper(getContext()).getWritableDatabase();
        return (database == null? false:true);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sql_builder = new SQLiteQueryBuilder();
        sql_builder.setTables("SONG_LIST");

        if(uri_matcher.match(uri) == SONG_ID)
        {
            sql_builder.appendWhere("_id" + " = " + uri.getPathSegments().get(1));
        }

        if(sortOrder == null || sortOrder == "")
        {
            sortOrder = "_id";
        }

        Cursor cursor = sql_builder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int count = 0;

        database.execSQL("update SONG_LIST set title = '" + selectionArgs[0] + "', url = '" + selectionArgs[1] + "' where _id = '" + selectionArgs[2] + "'");
        return count;
    }


    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, "song", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase database)
        {
            try {
                //database.execSQL(DATABASE_CREATE);
                database.execSQL("create table SONG_LIST(_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT)");

                String name = "让她降落";
                String url = "http://i.cs.hku.hk/fyp/2013/fyp13027/music/rang_ta_jiang_luo.mp3";
                database.execSQL("insert into SONG_LIST values(null,?,?)", new Object[]{name, url});

                name = "优美地低于生活";
                url = "http://i.cs.hku.hk/fyp/2013/fyp13027/music/you_mei_de.mp3";
                database.execSQL("insert into SONG_LIST values(null,?,?)", new Object[]{name, url});

                name = "You are not alone";
                url = "http://i.cs.hku.hk/fyp/2013/fyp13027/music/you_are_not_alone.mp3";
                database.execSQL("insert into SONG_LIST values(null,?,?)", new Object[]{name, url});

                name = "We are the world";
                url = "http://i.cs.hku.hk/fyp/2013/fyp13027/music/we_are_the_world.mp3";
                database.execSQL("insert into SONG_LIST values(null,?,?)", new Object[]{name, url});

                name = "Heal the world";
                url = "http://i.cs.hku.hk/fyp/2013/fyp13027/music/heal_the_world.mp3";
                database.execSQL("insert into SONG_LIST values(null,?,?)", new Object[]{name, url});

                name = "Earth song";
                url = "http://i.cs.hku.hk/fyp/2013/fyp13027/music/earth_song.mp3";
                database.execSQL("insert into SONG_LIST values(null,?,?)", new Object[]{name, url});

                name = "Viva la vida";
                url = "http://i.cs.hku.hk/fyp/2013/fyp13027/music/viva_la_vida.mp3";
                database.execSQL("insert into SONG_LIST values(null,?,?)", new Object[]{name, url});
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            //Log.w(LOG_TAG, "upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + "SONG_LIST");
            onCreate(db);
        }

    }

}
