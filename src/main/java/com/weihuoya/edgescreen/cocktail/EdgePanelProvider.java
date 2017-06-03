package com.weihuoya.edgescreen.cocktail;

import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager;
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider;

import com.weihuoya.edgescreen.R;
import com.weihuoya.edgescreen.weather.WeatherContentProvider;
import com.weihuoya.edgescreen.weather.WeatherRequest;

import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.os.RemoteException;
import android.text.TextUtils;
import android.view.DragEvent;
import android.widget.RemoteViews;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class EdgePanelProvider extends SlookCocktailProvider {

    private static final String TAG = EdgePanelProvider.class.getSimpleName();

    private static final String ACTION_PULL_TO_REFRESH = "com.weihuoya.edgescreen.cocktail.action.ACTION_PULL_TO_REFRESH";
    private static final String ACTION_REMOTE_LONG_CLICK = "com.weihuoya.edgescreen.cocktail.action.ACTION_REMOTE_LONGCLICK";
    private static final String ACTION_REMOTE_CLICK = "com.weihuoya.edgescreen.cocktail.action.ACTION_REMOTE_CLICK";

    private static final int ACTION_TYPE_NONE = 0;
    private static final int ACTION_TYPE_WGEO = 1;
    private static final int ACTION_TYPE_YOUDAO = 2;
    private static final int ACTION_TYPE_SYSTEM = 3;
    private static final int ACTION_TYPE_SMS = 4;
    private static final int ACTION_TYPE_CALLLOG = 5;
    private static final int ACTION_TYPE_SK2D = 6;
    private static final int ACTION_TYPE_WAP40D = 7;

    private static int mCocktailId = 0;
    private static RemoteViews mEdgeView = null;
    private static RemoteViews mContentView = null;

    private static RequestQueue mRequestQueue = null;
    private static boolean isRunningRequest = false;

    private static long mTimeElapsed = 7 * 24 * 60 * 60 * 1000;

    private Context mContext = null;

    public Context getContext() {
        if(mContext == null) {
            Log.e(TAG, "zhangwei getContext to null");
        }
        return mContext;
    }

    public void setContext(Context c) {
        if(c != null) {
            Log.v(TAG, "zhangwei setContext: " + c.getPackageName());
        } else {
            Log.v(TAG, "zhangwei setContext to null");
        }
        mContext = c;
    }

    @Override
    public void onDisabled(Context context) {
        Log.v(TAG, "zhangwei onDisabled: " + context.getPackageName());
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        Log.v(TAG, "zhangwei onEnabled: " + context.getPackageName());
        super.onEnabled(context);
    }

    @Override
    public void onVisibilityChanged(Context context, int cocktailId, int visibility) {
        Log.v(TAG, "zhangwei onVisibilityChanged: " + visibility + ", cocktailId: " + cocktailId);
        super.onVisibilityChanged(context, cocktailId, visibility);
    }

    @Override
    public void onUpdate(Context context, SlookCocktailManager cocktailManager, int[] cocktailIds) {
        Log.v(TAG, "zhangwei onUpdate: " + context.getPackageName() + ", cocktailId: " + cocktailIds[0]);
        mCocktailId = cocktailIds[0];
        cocktailManager.updateCocktail(mCocktailId, getContentView(), getHelpView());

        // pull to refresh
        Intent refreshIntent = new Intent(ACTION_PULL_TO_REFRESH);
        refreshIntent.putExtra("id", R.id.edge_content_list);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                R.id.edge_content_list,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        cocktailManager.setOnPullPendingIntent(mCocktailId, R.id.edge_content_list, pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        setContext(context);

        String action = intent.getAction();
        Log.d(TAG, "zhangwei onReceive: " + action);
        switch(action) {
            case ACTION_REMOTE_CLICK:
                onReceiveRemoteClick(context, intent);
                break;
            case ACTION_REMOTE_LONG_CLICK:
                onReceiveRemoteLongClick(context, intent);
                break;
            case ACTION_PULL_TO_REFRESH:
                onReceivePullToRefresh(context, intent);
                break;
            default:
                super.onReceive(context, intent);
                break;
        }
    }

    public void onDroped(Context context, SlookCocktailManager cocktailManager, DragEvent event) {
        Log.d(TAG, "zhangwei onDroped");
    }

    public void onUpdateFeeds(Context context, SlookCocktailManager cocktailManager) {
        Log.d(TAG, "zhangwei onUpdateFeeds");
    }

    private void onReceiveRemoteClick(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -1);
        if(id == R.id.edge_help_grid) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case ACTION_TYPE_WGEO:
                    requestWeather("wgeo", "");
                    break;
                case ACTION_TYPE_YOUDAO:
                    String word = SystemIntoUtils.getClipboardText(context);
                    if(!word.isEmpty()) {
                        requestYoudao(word);
                    }
                    break;
                case ACTION_TYPE_SYSTEM:
                    String info = SystemIntoUtils.getSystemInfo(context);
                    insertContent(type, info);
                    break;
                case ACTION_TYPE_SMS:
                    List<String> smslist = SystemIntoUtils.getSMS(context, mTimeElapsed, 6);
                    insertContents(type, smslist);
                    break;
                case ACTION_TYPE_CALLLOG:
                    List<String> loglist = SystemIntoUtils.getCallLog(context, mTimeElapsed, 6);
                    insertContents(type, loglist);
                    break;
                case ACTION_TYPE_SK2D:
                    handleWeatherAction("sk_2d");
                    break;
                case ACTION_TYPE_WAP40D:
                    handleWeatherAction("wap_40d");
                    break;
            }
        } else if(id == R.id.edge_content_list) {
            int itemId = intent.getIntExtra("item_id", -1);
            if(itemId > 0) {
                clearContent(itemId);
            }
        }
    }

    private void onReceiveRemoteLongClick(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -1);
        if(id == R.id.edge_content_list) {
            int itemId = intent.getIntExtra("itemId", -1);
            if(itemId > 0) {
                Toast.makeText(context, "long click item: " + itemId, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void onReceivePullToRefresh(Context context, Intent intent) {
        Log.d(TAG, "zhangwei onReceivePullToRefresh");
        clearContent(-1);
        SlookCocktailManager cocktailManager = SlookCocktailManager.getInstance(context);
        cocktailManager.notifyCocktailViewDataChanged(mCocktailId, R.id.edge_content_list);
    }

    private RemoteViews getContentView() {
        if(mContentView == null) {
            Context context = getContext();
            mContentView = new RemoteViews(context.getPackageName(), R.layout.edge_content_view);

            // list service
            Intent remoteIntent = new Intent(context, EdgeContentListService.class);
            remoteIntent.putExtra("type", R.id.edge_content_list);
            mContentView.setRemoteAdapter(R.id.edge_content_list, remoteIntent);

            // click intent
            Intent clickIntent = new Intent(ACTION_REMOTE_CLICK);
            clickIntent.putExtra("id", R.id.edge_content_list);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    R.id.edge_content_list,
                    clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mContentView.setPendingIntentTemplate(R.id.edge_content_list, pendingIntent);

            // long click intent
            Intent longClickIntent = new Intent(ACTION_REMOTE_LONG_CLICK);
            longClickIntent.putExtra("id", R.id.edge_content_list);
            pendingIntent = PendingIntent.getBroadcast(
                    context,
                    R.id.edge_content_list,
                    longClickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            SlookCocktailManager.getInstance(context).setOnLongClickPendingIntentTemplate(
                    mContentView,
                    R.id.edge_content_list,
                    pendingIntent);

        }
        return mContentView;
    }

    private void handleWeatherAction(final String api) {
        String query = "";
        String response = queryResponse("wgeo", query);
        if(response == null) {
            return;
        }
        List<String> list = parseWGEO(query, response);
        if(list.size() == 0) {
            return;
        }
        requestWeather(api, list.get(0));
    }

    private String getActionName(final int type) {
        int textId;
        switch (type) {
            case ACTION_TYPE_NONE:
                textId = R.string.action_name_none;
                break;
            case ACTION_TYPE_WGEO:
                textId = R.string.action_name_wgeo;
                break;
            case ACTION_TYPE_YOUDAO:
                textId = R.string.action_name_youdao;
                break;
            case ACTION_TYPE_SYSTEM:
                textId = R.string.action_name_system;
                break;
            case ACTION_TYPE_SMS:
                textId = R.string.action_name_sms;
                break;
            case ACTION_TYPE_CALLLOG:
                textId = R.string.action_name_calllog;
                break;
            case ACTION_TYPE_SK2D:
                textId = R.string.action_name_sk2d;
                break;
            case ACTION_TYPE_WAP40D:
                textId = R.string.action_name_wap40d;
                break;
            default:
                return null;
        }
        return getContext().getString(textId);
    }

    private List<Bundle> getActionList() {
        Context context = getContext();
        List<Bundle> list = new ArrayList<>();

        Bundle wgeo = new Bundle();
        wgeo.putInt("type", ACTION_TYPE_WGEO);
        wgeo.putString("text", getActionName(ACTION_TYPE_WGEO));
        wgeo.putInt("icon", android.R.drawable.ic_menu_compass);
        list.add(wgeo);

        Bundle weather = new Bundle();
        weather.putInt("type", ACTION_TYPE_SK2D);
        weather.putString("text", getActionName(ACTION_TYPE_SK2D));
        weather.putInt("icon", android.R.drawable.ic_menu_compass);
        list.add(weather);

        Bundle wap40d = new Bundle();
        wap40d.putInt("type", ACTION_TYPE_WAP40D);
        wap40d.putString("text", getActionName(ACTION_TYPE_WAP40D));
        wap40d.putInt("icon", android.R.drawable.ic_menu_compass);
        list.add(wap40d);

        Bundle youdao = new Bundle();
        youdao.putInt("type", ACTION_TYPE_YOUDAO);
        youdao.putString("text", getActionName(ACTION_TYPE_YOUDAO));
        youdao.putInt("icon", android.R.drawable.ic_menu_compass);
        list.add(youdao);

        if(context.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            Bundle system = new Bundle();
            system.putInt("type", ACTION_TYPE_SYSTEM);
            system.putString("text", getActionName(ACTION_TYPE_SYSTEM));
            system.putInt("icon", android.R.drawable.ic_menu_compass);
            list.add(system);
        }

        if(context.checkSelfPermission(android.Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            Bundle sms = new Bundle();
            sms.putInt("type", ACTION_TYPE_SMS);
            sms.putString("text", getActionName(ACTION_TYPE_SMS));
            sms.putInt("icon", android.R.drawable.ic_menu_compass);
            list.add(sms);
        }

        if(context.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {
            Bundle calllog = new Bundle();
            calllog.putInt("type", ACTION_TYPE_CALLLOG);
            calllog.putString("text", getActionName(ACTION_TYPE_CALLLOG));
            calllog.putInt("icon", android.R.drawable.ic_menu_compass);
            list.add(calllog);
        }

        return list;
    }

    private RemoteViews getHelpView() {
        if(mEdgeView == null) {
            Context context = getContext();
            mEdgeView = new RemoteViews(context.getPackageName(), R.layout.edge_help_view);

            Intent remoteIntent = new Intent(context, EdgeHelpGridService.class);
            remoteIntent.putExtra("type", R.id.edge_help_grid);

            List<Bundle> actionList = getActionList();
            for(int i = 0; i < actionList.size(); ++i) {
                remoteIntent.putExtra(String.valueOf(i), actionList.get(i));
            }
            mEdgeView.setRemoteAdapter(R.id.edge_help_grid, remoteIntent);

            //
            Intent clickIntent = new Intent(ACTION_REMOTE_CLICK);
            clickIntent.putExtra("id", R.id.edge_help_grid);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    R.id.edge_help_grid,
                    clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mEdgeView.setPendingIntentTemplate(R.id.edge_help_grid, pendingIntent);
        }
        return mEdgeView;
    }

    private void requestYoudao(final String word) {
        String content = queryResponse("youdao", word);
        if(content != null) {
            JSONObject response;
            try {
                response = new JSONObject(content);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            parseYoudao(response);
        } else {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("fanyi.youdao.com").
                    appendPath("openapi.do")
                    .appendQueryParameter("keyfrom", "longcwang")
                    .appendQueryParameter("key", "131895274")
                    .appendQueryParameter("type", "data")
                    .appendQueryParameter("doctype", "json")
                    .appendQueryParameter("version", "1.1")
                    .appendQueryParameter("q", word);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    builder.build().toString(),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            isRunningRequest = false;
                            insertResponse("youdao", word, response.toString());
                            parseYoudao(response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError e) {
                            isRunningRequest = false;
                            handleResponseError(e);
                        }
                    }
            );
            executeRequest(request);
        }
    }

    private void parseYoudao(final JSONObject response) {
        try {
            int errorCode = response.getInt("errorCode");
            if(errorCode == 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(response.getString("query"));

                JSONObject object = response.getJSONObject("basic");
                String phonetic = object.optString("phonetic");
                if(phonetic != null && !phonetic.isEmpty()) {
                    sb.append("[");
                    sb.append(phonetic);
                    sb.append("]");
                }

                JSONArray array = object.getJSONArray("explains");
                if(array.length() == 0) {
                    array = response.getJSONArray("translation");
                }

                if(array.length() > 0) {
                    sb.append("\n");
                    sb.append(array.getString(0));
                }

                insertContent(ACTION_TYPE_YOUDAO, sb.toString());
            }
        } catch (JSONException e) {
            Log.v(TAG, response.toString());
            handleResponseError(e);
        }
    }

    private void requestWeather(final String api, final String query) {
        String content = queryResponse(api, query);

        if(content != null) {
            parseWeather(api, query, content);
        } else {
            String id;
            String url;
            switch (api) {
                case "wgeo":
                    url = "http://wgeo.weather.com.cn/ip/";
                    break;
                case "sk_2d":
                    id = query.replaceAll("\\D+", "");
                    // http://d1.weather.com.cn/sk_2d/hours?lat=30.html
                    // http://d1.weather.com.cn/sk_2d/future?lat=30.html
                    url = "http://d1.weather.com.cn/sk_2d/" + id + ".html";
                    break;
                case "wap_40d":
                    id = query.replaceAll("\\D+", "");
                    // http://d1.weather.com.cn/calendar_new/2017/101270101_201705.html
                    url = "http://d1.weather.com.cn/wap_40d/" + id + ".html";
                    break;
                case "search":
                    id = query;
                    try {
                        id = URLEncoder.encode(query, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.e("URLEncoder", "encode: " + e.getMessage());
                    }
                    url = "http://toy1.weather.com.cn/search?cityname=" + id;
                    break;
                case "index_around":
                    id = query.replaceAll("\\D+", "");
                    url = "http://d1.weather.com.cn/index_around/" + id + ".html";
                    break;
                default:
                    return;
            }

            WeatherRequest request = new WeatherRequest(
                    Request.Method.GET,
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            isRunningRequest = false;
                            insertResponse(api, query, response);
                            parseWeather(api, query, response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError e) {
                            isRunningRequest = false;
                            handleResponseError(e);
                        }
                    }
            );
            executeRequest(request);
        }
    }

    private void parseWeather(final String api, final String query, final String response) {
        int type = ACTION_TYPE_NONE;
        List<String> list;
        switch (api) {
            case "wgeo":
                type = ACTION_TYPE_WGEO;
                list = parseWGEO(query, response);
                break;
            case "sk_2d":
                type = ACTION_TYPE_SK2D;
                list = parseSK2D(query, response);
                break;
            case "wap_40d":
                type = ACTION_TYPE_WAP40D;
                list = parseWAP40D(query, response);
                break;
            case "search":
                list = parseSearch(query, response);
                break;
            case "index_around":
                list = parseAround(query, response);
                break;
            default:
                return;
        }
        insertContent(type, TextUtils.join("\n", list));
    }

    private List<String> parseWGEO(final String query, final String content) {
        int valueBegin = 0;
        int valueEnd = 0;

        List<String> list = new ArrayList<>();

        valueBegin = content.indexOf('"', valueEnd + 1) + 1;
        valueEnd = content.indexOf('"', valueBegin);
        String ip = content.substring(valueBegin, valueEnd);

        valueBegin = content.indexOf('"', valueEnd + 1) + 1;
        valueEnd = content.indexOf('"', valueBegin);
        String id = content.substring(valueBegin, valueEnd);

        valueBegin = content.indexOf('"', valueEnd + 1) + 1;
        valueEnd = content.indexOf('"', valueBegin);
        String addr = content.substring(valueBegin, valueEnd);

        list.add(id);
        list.add(ip);
        list.add(addr);
        return list;
    }

    private List<String> parseSK2D(final String query, final String content) {
        List<String> list = new ArrayList<>();

        int valueBegin = content.indexOf('=');
        try {
            JSONObject reader = new JSONObject(content.substring(valueBegin + 1));
            String city = reader.getString("city");
            String temp = reader.getString("temp");
            String weather = reader.getString("weather");
            String cityname = reader.getString("cityname");
            list.add(city);
            list.add(cityname);
            list.add(weather);
            list.add(temp + "°C");
        } catch (JSONException e) {
            Log.e("SK2D", "json: " + e.getMessage());
        }
        return list;
    }

    private List<String> parseWAP40D(final String query, final String content) {
        int valueBegin = 0;
        int valueEnd = 0;

        List<String> list = new ArrayList<>();

        valueBegin = content.indexOf('=', valueEnd + 1) + 1;
        valueEnd = content.indexOf(';', valueBegin);
        String fc40 = content.substring(valueBegin, valueEnd);

        try {
            JSONArray array = new JSONArray(fc40);
            JSONObject reader = array.getJSONObject(0);
            String date = reader.getString("009");
            String lunar = reader.getString("010");
            String week = reader.getString("016");
            list.add(date);
            list.add(lunar);
            list.add(week);
        } catch (JSONException e) {
            handleResponseError(e);
        }

        valueBegin = content.indexOf('=', valueEnd + 1) + 1;
        valueEnd = content.indexOf(';', valueBegin);
        String index3d = content.substring(valueBegin, valueEnd);

        try {
            JSONObject reader = new JSONObject(index3d);
            String date = reader.getString("i0");
            list.add(date);
        } catch (JSONException e) {
            handleResponseError(e);
        }

        valueBegin = content.indexOf('=', valueEnd + 1) + 1;
        //valueEnd = content.indexOf(';', valueBegin);
        String fc1h_24 = content.substring(valueBegin);

        try {
            JSONObject reader = new JSONObject(fc1h_24);
            JSONArray array = reader.getJSONArray("jh");
            for(int i = 0; i < array.length(); ++i) {
                JSONObject item = array.getJSONObject(i);
                String date = item.getString("jf");

                list.add(date);
            }
        } catch (JSONException e) {
            handleResponseError(e);
        }
        return list;
    }

    private List<String> parseSearch(final String query, final String content) {
        List<String> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(content.substring(1, content.length()-1));
            JSONObject reader = array.getJSONObject(0);
            String data = reader.getString("ref");
            String[] items = data.split("~");
            Collections.addAll(list, items);
        } catch (JSONException e) {
            handleResponseError(e);
        }
        return list;
    }

    private List<String> parseAround(final String query, final String content) {
        List<String> list = new ArrayList<>();

        int valueBegin = content.indexOf('=') + 1;
        String around = content.substring(valueBegin);

        try {
            JSONObject reader = new JSONObject(around);
            JSONArray array = reader.getJSONArray("jd");
            for(int i = 0; i < array.length(); ++i) {
                JSONObject item = array.getJSONObject(0);
                String id = item.getString("ac");
                String cityname = item.getString("an");
                list.add(id);
                list.add(cityname);
            }
        } catch (JSONException e) {
            handleResponseError(e);
        }
        return list;
    }

    private <T> void executeRequest(Request<T> request) {
        if(isRunningRequest) {
            Log.e(TAG, "zhangwei isRunningRequest is true");
            return;
        }

        if(!SystemIntoUtils.isNetworkAvailable(getContext())) {
            Log.e(TAG, "zhangwei isNetworkAvailable is false");
            return;
        }

        if(mRequestQueue == null) {
            Context context = getContext();
            if(context == null) {
                Log.e(TAG, "zhangwei executeRequest context is null");
                return;
            }
            Log.v(TAG, "zhangwei executeRequest newRequestQueue");
            mRequestQueue = Volley.newRequestQueue(context);
        }

        isRunningRequest = true;
        mRequestQueue.add(request);
    }

    private String queryResponse(final String api, final String query) {
        String content = null;
        Context context = getContext();
        Uri CONTENT_URI = Uri.parse("content://" + WeatherContentProvider.AUTHORITY + "/response");
        long timestamp = System.currentTimeMillis() - mTimeElapsed;

        String[] projection = {"content"};
        String selection = "api=? and query=? and timestamp>?";
        String[] selectionArgs = {api, query, String.valueOf(timestamp)};
        String sortOrder = "timestamp desc limit 1";

        Cursor cursor = context.getContentResolver().query(CONTENT_URI, projection, selection, selectionArgs, sortOrder);
        if(cursor.moveToNext()) {
            int idxContent = cursor.getColumnIndex("content");
            content = cursor.getString(idxContent);
        }
        cursor.close();
        return content;
    }

    private void insertResponse(final String api, final String query, final String content) {
        Uri CONTENT_URI = Uri.parse("content://" + WeatherContentProvider.AUTHORITY + "/response");
        ContentValues values = new ContentValues();
        values.put("api", api);
        values.put("timestamp", System.currentTimeMillis());
        values.put("query", query);
        values.put("content", content);
        getContext().getContentResolver().insert(CONTENT_URI, values);
    }

    private void insertContents(final int type, final List<String> contents) {
        if(contents.size() == 0) {
            return;
        }

        Context context = getContext();
        String title = getActionName(type);
        Uri CONTENT_URI = Uri.parse("content://" + WeatherContentProvider.AUTHORITY + "/content");
        ArrayList<ContentProviderOperation> opslist = new ArrayList<>();

        // clear all
        String selection = "id > ?";
        String[] selectionArgs = {String.valueOf(-1)};
        opslist.add(ContentProviderOperation.newDelete(CONTENT_URI).withSelection(selection, selectionArgs).build());

        // add all
        for(String c : contents) {
            ContentValues values = new ContentValues();
            values.put("type", type);
            values.put("title", title);
            values.put("content", c);
            opslist.add(ContentProviderOperation.newInsert(CONTENT_URI).withValues(values).build());
        }

        try {
            context.getContentResolver().applyBatch(WeatherContentProvider.AUTHORITY, opslist);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
            return;
        }

        SlookCocktailManager cocktailManager = SlookCocktailManager.getInstance(context);
        cocktailManager.notifyCocktailViewDataChanged(mCocktailId, R.id.edge_content_list);
    }

    private void insertContent(final int type, final String content) {
        Context context = getContext();
        Uri CONTENT_URI = Uri.parse("content://" + WeatherContentProvider.AUTHORITY + "/content");
        ContentValues values = new ContentValues();
        values.put("type", type);
        values.put("title", getActionName(type));
        values.put("content", content);
        context.getContentResolver().insert(CONTENT_URI, values);

        SlookCocktailManager cocktailManager = SlookCocktailManager.getInstance(context);
        cocktailManager.notifyCocktailViewDataChanged(mCocktailId, R.id.edge_content_list);
    }

    private int clearContent(final int id) {
        Context context = getContext();
        Uri CONTENT_URI = Uri.parse("content://" + WeatherContentProvider.AUTHORITY + "/content");
        String selection = id == -1 ? "id > ?" : "id == ?";
        String[] selectionArgs = {String.valueOf(id)};
        int count = context.getContentResolver().delete(CONTENT_URI, selection, selectionArgs);
        if(count > 0) {
            SlookCocktailManager cocktailManager = SlookCocktailManager.getInstance(context);
            cocktailManager.notifyCocktailViewDataChanged(mCocktailId, R.id.edge_content_list);
        }
        return count;
    }

    private void handleResponseError(Exception e) {
        String content;
        if(e instanceof TimeoutError) {
            content = "Timeout";
        } else if(e instanceof NoConnectionError) {
            content = "NoConnection";
        } else if(e instanceof ParseError) {
            content = "ParseError";
        } else if(e instanceof JSONException) {
            content = "JSONException";
        } else {
            content = "Error";
            e.printStackTrace();
        }
        insertContent(ACTION_TYPE_NONE, content);
    }
}
