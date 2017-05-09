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
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class WeatherContentProvider extends ContentProvider {

    private static final String TAG = WeatherContentProvider.class.getSimpleName();

    /** Database filename */
    private static final String DB_NAME = "weather.db";
    /** Current database version */
    private static final int DB_VERSION = 100;
    /** Name of table in the database */
    private static final String DB_TABLE_WEATHER = "weather";


    private SQLiteOpenHelper mSQLiteHelper = null;
    private RequestQueue mRequestQueue = null;

    /** URI matcher used to recognize URIs sent by applications */
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final String AUTHORITY = "content://com.example.edgescreen.weather.WeatherContentProvider";

    private static final int MATCH_SEARCH = 1;
    private static final int MATCH_WGEO = 2;
    private static final int MATCH_SK_2D = 3;
    private static final int MATCH_WAP_40D = 4;
    private static final int MATCH_INDEX_AROUND = 5;

    static {
        // match /search/xxx
        sURIMatcher.addURI(AUTHORITY, "search/#", MATCH_SEARCH);
        // match /wgeo
        sURIMatcher.addURI(AUTHORITY, "wgeo", MATCH_WGEO);
        // match /sk_2d
        sURIMatcher.addURI(AUTHORITY, "sk_2d/#", MATCH_SK_2D);
        // match /wap_40d/xxx
        sURIMatcher.addURI(AUTHORITY, "wap_40d/#", MATCH_WAP_40D);
        // match /index_around
        sURIMatcher.addURI(AUTHORITY, "index_around/#", MATCH_INDEX_AROUND);
    }

    public WeatherContentProvider() {
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mRequestQueue = Volley.newRequestQueue(context);
        mSQLiteHelper = new MyDatabaseHelper(context);
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        int match = sURIMatcher.match(uri);
        String sql = "api=";

        switch (match) {
            case MATCH_SEARCH:
                sql += "search";
                break;
            case MATCH_WGEO:
                sql += "wgeo";
                break;
            case MATCH_SK_2D:
                sql += "sk_2d";
                break;
            case MATCH_WAP_40D:
                sql += "wap_40d";
                break;
            case MATCH_INDEX_AROUND:
                sql += "index_around";
                break;
            default:
                Log.d(TAG, "deleting unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot delete URI: " + uri);
        }

        if(!TextUtils.isEmpty(selection)) {
            sql += " and (" + selection + ")";
        }

        return db.delete(DB_TABLE_WEATHER, sql, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.item/weather";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        int match = sURIMatcher.match(uri);
        String api;

        switch (match) {
            case MATCH_SEARCH:
                api = "search";
                break;
            case MATCH_WGEO:
                api = "wgeo";
                break;
            case MATCH_SK_2D:
                api = "sk_2d";
                break;
            case MATCH_WAP_40D:
                api = "wap_40d";
                break;
            case MATCH_INDEX_AROUND:
                api = "index_around";
                break;
            default:
                Log.d(TAG, "deleting unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot delete URI: " + uri);
        }

        ContentValues filteredValues = new ContentValues();
        filteredValues.put("api", api);
        filteredValues.put("timestamp", System.currentTimeMillis());
        copyString("city", values, filteredValues);
        copyString("content", values, filteredValues);

        long rowID = db.insert(DB_TABLE_WEATHER, null, filteredValues);
        if (rowID == -1) {
            Log.d(TAG, "couldn't insert into downloads database");
            return null;
        }

        return ContentUris.withAppendedId(uri, rowID);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mSQLiteHelper.getReadableDatabase();
        int match = sURIMatcher.match(uri);

        String sql = "api=";

        switch (match) {
            case MATCH_SEARCH:
                sql += "search";
                break;
            case MATCH_WGEO:
                sql += "wgeo";
                break;
            case MATCH_SK_2D:
                sql += "sk_2d";
                break;
            case MATCH_WAP_40D:
                sql += "wap_40d";
                break;
            case MATCH_INDEX_AROUND:
                sql += "index_around";
                break;
            default:
                Log.d(TAG, "deleting unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot delete URI: " + uri);
        }

        if(!TextUtils.isEmpty(selection)) {
            sql += " and (" + selection + ")";
        }

        return db.query(DB_TABLE_WEATHER, projection, sql, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        int match = sURIMatcher.match(uri);
        String api;

        switch (match) {
            case MATCH_SEARCH:
                api = "search";
                break;
            case MATCH_WGEO:
                api = "wgeo";
                break;
            case MATCH_SK_2D:
                api = "sk_2d";
                break;
            case MATCH_WAP_40D:
                api = "wap_40d";
                break;
            case MATCH_INDEX_AROUND:
                api = "index_around";
                break;
            default:
                Log.d(TAG, "deleting unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot delete URI: " + uri);
        }

        String sql = "api=" + api;
        if(!TextUtils.isEmpty(selection)) {
            sql += " and (" + selection + ")";
        }

        ContentValues filteredValues = new ContentValues();
        filteredValues.put("api", api);
        filteredValues.put("timestamp", System.currentTimeMillis());
        copyString("city", values, filteredValues);
        copyString("content", values, filteredValues);

        return db.update(DB_TABLE_WEATHER, filteredValues, sql, selectionArgs);
    }


    private void requestWGEO() {
        WeatherRequest request = new WeatherRequest(Request.Method.GET,
                "http://wgeo.weather.com.cn/ip/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parseWGEO(response);
                insertWeatherContent("wgeo", "", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                Log.e("WGEO", "request: " + e.getMessage());
            }
        });

        mRequestQueue.add(request);
    }

    private void parseWGEO(final String content) {
        if(content.startsWith("<")) {
            Log.v("WGEO", "web address error!");
            return;
        }

        int valueBegin = 0;
        int valueEnd = 0;

        valueBegin = content.indexOf('"', valueEnd + 1) + 1;
        valueEnd = content.indexOf('"', valueBegin);
        String ip = content.substring(valueBegin, valueEnd);

        valueBegin = content.indexOf('"', valueEnd + 1) + 1;
        valueEnd = content.indexOf('"', valueBegin);
        String id = content.substring(valueBegin, valueEnd);

        valueBegin = content.indexOf('"', valueEnd + 1) + 1;
        valueEnd = content.indexOf('"', valueBegin);
        String addr = content.substring(valueBegin, valueEnd);

        //Toast.makeText(EdgeConfigure.this, ip + ", " + id + ", " + addr, Toast.LENGTH_LONG).show();
    }

    private void requestSK2D(final String id) {
        String query = id.replaceAll("\\D+", "");

        WeatherRequest request = new WeatherRequest(Request.Method.GET,
                "http://d1.weather.com.cn/sk_2d/" + query + ".html", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parseSK2D(response);
                insertWeatherContent("sk_2d", id, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                Log.e("SK2D", "request: " + e.getMessage());
            }
        });

        mRequestQueue.add(request);
    }

    private void parseSK2D(final String content) {
        if(content.startsWith("<")) {
            Log.v("SK2D", "web address error!");
            return;
        }

        int valueBegin = content.indexOf('=');
        try {
            JSONObject reader = new JSONObject(content.substring(valueBegin + 1));

            String city = reader.getString("city");
            String temp = reader.getString("temp");
            String weather = reader.getString("weather");
            String cityname = reader.getString("cityname");

            //Toast.makeText(EdgeConfigure.this, cityname + ", " + weather + ", " + temp + "°C", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            Log.e("SK2D", "json: " + e.getMessage());
        }
    }


    private void requestWAP40D(final String id) {
        WeatherRequest request = new WeatherRequest(Request.Method.GET,
                "http://d1.weather.com.cn/wap_40d/" + id + ".html", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parseWAP40D(response);
                insertWeatherContent("wap_40d", id, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                Log.e("WAP40D", "request: " + e.getMessage());
            }
        });

        mRequestQueue.add(request);
    }

    private void parseWAP40D(final String content) {
        if(content.startsWith("<")) {
            Log.v("WAP40D", "web address error!");
            return;
        }

        int valueBegin = 0;
        int valueEnd = 0;

        valueBegin = content.indexOf('=', valueEnd + 1) + 1;
        valueEnd = content.indexOf(';', valueBegin);
        String fc40 = content.substring(valueBegin, valueEnd);

        try {
            JSONArray array = new JSONArray(fc40);
            JSONObject reader = array.getJSONObject(0);

            String date = reader.getString("009");
            String lunar = reader.getString("010");
            String week = reader.getString("016");

            //Toast.makeText(EdgeConfigure.this, date + ", " + lunar + ", " + week, Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            Log.e("WAP40D", "fc40: " + e.getMessage());
        }

        valueBegin = content.indexOf('=', valueEnd + 1) + 1;
        valueEnd = content.indexOf(';', valueBegin);
        String index3d = content.substring(valueBegin, valueEnd);

        try {
            JSONObject reader = new JSONObject(index3d);

            String date = reader.getString("i0");

            //Toast.makeText(EdgeConfigure.this, date, Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            Log.e("WAP40D", "index3d: " + e.getMessage());
        }

        valueBegin = content.indexOf('=', valueEnd + 1) + 1;
        //valueEnd = content.indexOf(';', valueBegin);
        String fc1h_24 = content.substring(valueBegin);

        try {
            JSONObject reader = new JSONObject(fc1h_24);
            JSONArray array = reader.getJSONArray("jh");
            JSONObject item = array.getJSONObject(0);

            String date = item.getString("jf");

            //Toast.makeText(EdgeConfigure.this, date, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            Log.e("WAP40D", "fc1h_24: " + e.getMessage());
        }

    }

    private void requestSearch(final String name) {
        String query = name;
        try {
            query = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("URLEncoder", "encode: " + e.getMessage());
        }

        Log.v("Search", "name = " + name);

        WeatherRequest request = new WeatherRequest(Request.Method.GET,
                "http://toy1.weather.com.cn/search?cityname=" + query, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parseSearch(response);
                insertWeatherContent("search", name, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                Log.e("Search", "request: " + e.getMessage());
                e.printStackTrace();
            }
        });

        mRequestQueue.add(request);
    }

    private void parseSearch(final String content) {
        if(content.startsWith("<")) {
            Log.v("Search", "web address error!");
            return;
        }

        try {
            JSONArray array = new JSONArray(content.substring(1, content.length()-1));
            JSONObject reader = array.getJSONObject(0);

            String data = reader.getString("ref");
            String[] items = data.split("~");

            //Toast.makeText(EdgeConfigure.this, items[0] + ", " + items[2] + ", " + items[9], Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            Log.e("Search", "json: " + e.getMessage());
        }
    }


    private void requestAround(final String id) {
        WeatherRequest request = new WeatherRequest(Request.Method.GET,
                "http://d1.weather.com.cn/index_around/" + id + ".html", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parseAround(response);
                insertWeatherContent("index_around", id, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                Log.e("WAP40D", "request: " + e.getMessage());
            }
        });

        mRequestQueue.add(request);
    }

    private void parseAround(String content) {
        if(content.startsWith("<")) {
            Log.v("Around", "web address error!");
            return;
        }

        int valueBegin = content.indexOf('=') + 1;
        String around = content.substring(valueBegin);

        try {
            JSONObject reader = new JSONObject(around);
            JSONArray array = reader.getJSONArray("jd");

            JSONObject item = array.getJSONObject(0);
            String id = item.getString("ac");
            String cityname = item.getString("an");

            //Toast.makeText(EdgeConfigure.this, id + ", " + cityname, Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            Log.e("WAP40D", "fc40: " + e.getMessage());
        }
    }

    private void insertWeatherContent(String api, String city, String content) {
        if(content.startsWith("<")) {
            Log.v(TAG, "weather content error!");
            return;
        }

        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("api", api);
        values.put("city", city);
        values.put("content", content);
        values.put("timestamp", System.currentTimeMillis());

        long rowId = db.insert(DB_TABLE_WEATHER, null, values);
        if(rowId == -1) {
            Log.e(TAG, "couldn't insert into database");
        }
    }

    private static final void copyInteger(String key, ContentValues from, ContentValues to) {
        Integer i = from.getAsInteger(key);
        if (i != null) {
            to.put(key, i);
        }
    }
    private static final void copyBoolean(String key, ContentValues from, ContentValues to) {
        Boolean b = from.getAsBoolean(key);
        if (b != null) {
            to.put(key, b);
        }
    }
    private static final void copyString(String key, ContentValues from, ContentValues to) {
        String s = from.getAsString(key);
        if (s != null) {
            to.put(key, s);
        }
    }

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
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_WEATHER);
                db.execSQL("CREATE TABLE " + DB_TABLE_WEATHER + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "timestamp" + " INTEGER NOT NULL," +
                        "api" + " TEXT NOT NULL," +
                        "city" + " TEXT NOT NULL" +
                        "content" + " TEXT NOT NULL" +
                        ");");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}