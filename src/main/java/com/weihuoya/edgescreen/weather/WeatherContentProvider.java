package com.weihuoya.edgescreen.weather;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.CancellationSignal;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;


public class WeatherContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.weihuoya.weather.WeatherContentProvider";

    /** TAG */
    private static final String TAG = WeatherContentProvider.class.getSimpleName();

    /** Database filename */
    private static final String DB_NAME = "weather.db";
    /** Current database version */
    private static final int DB_VERSION = 100;
    /** Name of table in the database */
    private static final String DB_TABLE_RESPONSE = "response";
    private static final String DB_TABLE_CONTENT = "content";

    /** URI matcher used to recognize URIs sent by applications */
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int MATCH_RESPONSE = 1;
    private static final int MATCH_CONTENT = 2;

    private static final int MATCH_SEARCHABLE = 3;
    private static final int MATCH_SEARCHABLE_QUERY = 4;
    private static final int MATCH_SEARCHABLE_PREFIX_QUERY = 5;

    static {
        // match /content
        sURIMatcher.addURI(AUTHORITY, "content", MATCH_CONTENT);

        // match /response
        sURIMatcher.addURI(AUTHORITY, "response", MATCH_RESPONSE);

        // match searchable
        sURIMatcher.addURI(AUTHORITY, "search_suggest_regex_query", MATCH_SEARCHABLE);
        sURIMatcher.addURI(AUTHORITY, "search_suggest_regex_query/*", MATCH_SEARCHABLE_QUERY);
        sURIMatcher.addURI(AUTHORITY, "search_suggest_regex_prefix_query/*", MATCH_SEARCHABLE_PREFIX_QUERY);
    }

    private SQLiteOpenHelper mSQLiteHelper;

    public WeatherContentProvider() {
    }

    @Override
    public boolean onCreate() {
        mSQLiteHelper = new MyDatabaseHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sURIMatcher.match(uri);
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        String tableName;

        if(match == MATCH_RESPONSE) {
            tableName = DB_TABLE_RESPONSE;
        } else if(match == MATCH_CONTENT) {
            tableName = DB_TABLE_CONTENT;
        } else {
            Log.e(TAG, "delete unknown/invalid URI: " + uri);
            throw new UnsupportedOperationException("Cannot delete URI: " + uri);
        }

        return db.delete(tableName, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        final int match = sURIMatcher.match(uri);
        if(match == MATCH_RESPONSE) {
            return "vnd.android.cursor.item/response";
        } else if(match == MATCH_CONTENT) {
            return "vnd.android.cursor.dir/content";
        } else if(match == MATCH_SEARCHABLE ||
                match == MATCH_SEARCHABLE_QUERY ||
                match == MATCH_SEARCHABLE_PREFIX_QUERY) {
            return "vnd.android.cursor.dir/vnd.android.search.suggest";
        } else {
            Log.e(TAG, "getType unknown/invalid URI: " + uri);
            throw new UnsupportedOperationException("Cannot getType URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sURIMatcher.match(uri);
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        String tableName;

        if(match == MATCH_RESPONSE) {
            tableName = DB_TABLE_RESPONSE;
        } else if(match == MATCH_CONTENT) {
            tableName = DB_TABLE_CONTENT;
        } else {
            Log.e(TAG, "insert unknown/invalid URI: " + uri);
            throw new UnsupportedOperationException("Cannot insert URI: " + uri);
        }

        long rowID = db.insert(tableName, null, values);
        if (rowID == -1) {
            Log.d(TAG, "couldn't insert into downloads database");
            return null;
        }
        return ContentUris.withAppendedId(uri, rowID);
    }

    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection, String[] selectionArgs,
                        String sortOrder) {
        final int match = sURIMatcher.match(uri);
        SQLiteDatabase db = mSQLiteHelper.getReadableDatabase();
        String tableName;

        if(match == MATCH_RESPONSE) {
            tableName = DB_TABLE_RESPONSE;
        } else if(match == MATCH_CONTENT) {
            tableName = DB_TABLE_CONTENT;
        } else if(match == MATCH_SEARCHABLE ||
                match == MATCH_SEARCHABLE_QUERY ||
                match == MATCH_SEARCHABLE_PREFIX_QUERY) {

            tableName = DB_TABLE_RESPONSE;

            // select api as suggest_text_1, query as suggest_text_2, content as suggest_text_3 from DB_TABLE_RESPONSE where content like %word% limit 10

            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();


            if(projection != null) {
                Log.v("weihuoya", "projection: " + TextUtils.join(", ", projection));
            }

            if(selection != null) {
                Log.v("weihuoya", "selection: " + selection);
            }

            if(selectionArgs != null) {
                Log.v("weihuoya", "selectionArgs: " + TextUtils.join(", ", selectionArgs));
            }

            if(sortOrder != null) {
                Log.v("weihuoya", "sortOrder: " + sortOrder);
            }

            List<String> querys = uri.getPathSegments();
            if(querys != null) {
                Log.v("weihuoya", "querys: " + TextUtils.join(", ", querys));
            }
            return null;
        } else {
            Log.e(TAG, "query unknown/invalid URI: " + uri);
            throw new UnsupportedOperationException("Cannot query URI: " + uri);
        }

        return db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sURIMatcher.match(uri);
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        String tableName;

        if(match == MATCH_RESPONSE) {
            tableName = DB_TABLE_RESPONSE;
        } else if(match == MATCH_CONTENT) {
            tableName = DB_TABLE_CONTENT;
        } else {
            Log.e(TAG, "update unknown/invalid URI: " + uri);
            throw new UnsupportedOperationException("Cannot delete URI: " + uri);
        }

        return db.update(tableName, values, selection, selectionArgs);
    }

    //
    protected static final class MyDatabaseHelper extends SQLiteOpenHelper {

        /*
         * Instantiates an open helper for the provider's SQLite data repository
         * Do not do database creation and upgrade here.
         */
        MyDatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        /*
         * Creates the data repository. This is called when the provider attempts to open the
         * repository and SQLite reports that it doesn't exist.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            // Creates the main table
            createWeatherTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // todo
        }

        private void createWeatherTable(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_RESPONSE);
                db.execSQL("CREATE TABLE " + DB_TABLE_RESPONSE + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "timestamp" + " INTEGER NOT NULL," +
                        "api" + " TEXT NOT NULL," +
                        "query" + " TEXT NOT NULL," +
                        "content" + " TEXT NOT NULL" +
                        ");");

                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_CONTENT);
                db.execSQL("CREATE TABLE " + DB_TABLE_CONTENT + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "type" + " INTEGER NOT NULL," +
                        "title" + " TEXT NOT NULL," +
                        "content" + " TEXT NOT NULL" +
                        ");");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}