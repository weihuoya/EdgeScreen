package com.weihuoya.edgescreen.cocktail;

import com.android.volley.NoConnectionError;
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
import com.weihuoya.edgescreen.weather.WeatherRequest;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.view.DragEvent;
import android.view.View;
import android.widget.RemoteViews;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.CLIPBOARD_SERVICE;


public class SinglePlusProvider extends SlookCocktailProvider {

    private static final String TAG = SinglePlusProvider.class.getSimpleName();

    private static final String ACTION_PULL_TO_REFRESH = "com.weihuoya.edgescreen.cocktail.action.ACTION_PULL_TO_REFRESH";
    private static final String ACTION_REMOTE_LONG_CLICK = "com.weihuoya.edgescreen.cocktail.action.ACTION_REMOTE_LONGCLICK";
    private static final String ACTION_REMOTE_CLICK = "com.weihuoya.edgescreen.cocktail.action.ACTION_REMOTE_CLICK";

    private static int mCocktailId = 0;
    private static RemoteViews mEdgeView = null;
    private static RemoteViews mContentView = null;

    private static RequestQueue mRequestQueue = null;
    private static String mCityId = null;
    private static boolean isRunningRequest = false;

    private Context mContext = null;

    public Context getContext() {
        if(mContext == null) {
            Log.e(TAG, "getContext to null");
        }
        return mContext;
    }

    public void setContext(Context c) {
        if(c != null) {
            Log.v(TAG, "setContext: " + c.getPackageName());
        } else {
            Log.v(TAG, "setContext to null");
        }
        mContext = c;
    }

    @Override
    public void onDisabled(Context context) {
        Log.v(TAG, "onDisabled: " + context.getPackageName());
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        Log.v(TAG, "onEnabled: " + context.getPackageName());
        super.onEnabled(context);
    }

    @Override
    public void onVisibilityChanged(Context context, int cocktailId, int visibility) {
        Log.v(TAG, "onVisibilityChanged: " + visibility + ", cocktailId: " + cocktailId);
        super.onVisibilityChanged(context, cocktailId, visibility);
    }

    @Override
    public void onUpdate(Context context, SlookCocktailManager cocktailManager, int[] cocktailIds) {
        Log.v(TAG, "onUpdate: " + context.getPackageName() + ", cocktailId: " + cocktailIds[0]);
        mCocktailId = cocktailIds[0];
        cocktailManager.updateCocktail(mCocktailId, getContentView(), getHelpView());

        // set pull to refresh
        cocktailManager.setOnPullPendingIntent(mCocktailId, R.id.single_list_view, getRefreshIntent(context, R.id.single_list_view, 0));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        setContext(context);
        super.onReceive(context, intent);

        String action = intent.getAction();
        Log.d(TAG, "onReceive: " + action);
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
        }
    }

    public void onDroped(Context context, SlookCocktailManager cocktailManager, DragEvent event) {
        Log.d(TAG, "onDroped");
    }

    public void onUpdateFeeds(Context context, SlookCocktailManager cocktailManager) {
        Log.d(TAG, "onUpdateFeeds");
    }

    private void onReceiveRemoteClick(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -1);
        switch (id) {
            case R.id.single_btn1:
                requestWGEO();
                break;
            case R.id.single_btn2:
                requestSK2D(mCityId);
                break;
            case R.id.single_btn3:
                requestYoudao(getClipboardText());
                break;
            case R.id.single_btn4:
                showSystemInfo();
                break;
            case R.id.single_btn5:
                showSMS();
                break;
        }
    }

    private void onReceiveRemoteLongClick(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -1);
        switch (id) {
            case R.id.single_btn1:
                break;
            case R.id.single_btn2:
                break;
            case R.id.single_btn3:
                break;
        }
    }

    private void onReceivePullToRefresh(Context context, Intent intent) {
        Log.d(TAG, "onReceivePullToRefresh");
        SlookCocktailManager cocktailManager = SlookCocktailManager.getInstance(context);
        cocktailManager.notifyCocktailViewDataChanged(mCocktailId, R.id.single_list_view);
    }

    private boolean isNetworkAvailable() {
        Context context = getContext();
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo.isAvailable() && networkInfo.isConnected()) {
            return true;
        } else {
            TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            if(telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY &&
                    telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
                return true;
            }
        }
        return false;
    }

    private PendingIntent getRefreshIntent(Context context, int id, int key) {
        Intent refreshIntent = new Intent(ACTION_PULL_TO_REFRESH);
        refreshIntent.putExtra("id", id);
        refreshIntent.putExtra("key", key);
        return PendingIntent.getBroadcast(context, id, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getLongClickIntent(Context context, int id, int key) {
        Intent longClickIntent = new Intent(ACTION_REMOTE_LONG_CLICK);
        longClickIntent.putExtra("id", id);
        longClickIntent.putExtra("key", key);
        return PendingIntent.getBroadcast(context, id, longClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getClickIntent(Context context, int id, int key) {
        Intent clickIntent = new Intent(ACTION_REMOTE_CLICK);
        clickIntent.putExtra("id", id);
        clickIntent.putExtra("key", key);
        return PendingIntent.getBroadcast(context, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void showSystemInfo() {
        String availableSize;
        String totalSize;

        Context context = getContext();
        StringBuilder sb = new StringBuilder();

        sb.append("Board: ");
        sb.append(Build.BOARD);
        sb.append('\n');

        sb.append("Device: ");
        sb.append(Build.DEVICE);
        sb.append('\n');


        try {
            String macAddress = "00:00:00:00:00:00";
            StringBuffer buffer = new StringBuffer();

            NetworkInterface networkInterface = NetworkInterface.getByName("eth1");

            if(networkInterface == null) {
                networkInterface = NetworkInterface.getByName("wlan0");
            }

            if(networkInterface != null) {
                byte[] addr = networkInterface.getHardwareAddress();
                for(byte b : addr) {
                    buffer.append(String.format("%02X:", b));
                }
                if(buffer.length() > 0) {
                    buffer.deleteCharAt(buffer.length() - 1);
                }
                macAddress = buffer.toString();
            }

            sb.append("Mac: ");
            sb.append(macAddress);
            sb.append('\n');
        } catch (SocketException e) {
            e.printStackTrace();
        }


        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        sb.append("IMEI: ");
        sb.append(telephonyManager.getDeviceId());
        sb.append('\n');


        File[] files = context.getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS);
        for(File f : files) {
            String path = f.getPath();
            path = path.substring(0, path.indexOf("Android"));

            StatFs stat = new StatFs(path);
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            long totalBlocks = stat.getBlockCountLong();

            if(path.contains("emulated")) {
                sb.append("Storage: ");
            } else {
                sb.append("SDCard: ");
            }

            availableSize = Formatter.formatFileSize(context, blockSize * availableBlocks);
            totalSize = Formatter.formatFileSize(context, blockSize * totalBlocks);
            sb.append(availableSize + " / " + totalSize);
            sb.append('\n');
        }

        // memory
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);

        availableSize = Formatter.formatFileSize(context, memoryInfo.availMem);
        totalSize = Formatter.formatFileSize(context, memoryInfo.totalMem);
        sb.append("Memory: ");
        sb.append(availableSize + " / " + totalSize);

        updateContentView(sb.toString(), false);
    }


    private void showSMS() {
        Context context = getContext();

        Uri URI_SMS = Uri.parse("content://sms/");
        ContentResolver contentResolver = context.getContentResolver();

        long timestamp = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        String[] projection = {"_id", "address", "person", "body", "date", "type"};
        String selection = "date > ?";
        String[] selectionArgs = {String.valueOf(timestamp)};
        String sortOrder = "date desc";
        Cursor cursor = contentResolver.query(URI_SMS, projection, selection, selectionArgs, sortOrder);
        if(cursor == null) {
            return;
        }

        int idxAddress = cursor.getColumnIndex("address");
        int idxPerson = cursor.getColumnIndex("person");
        int idxBody = cursor.getColumnIndex("body");
        int idxDate = cursor.getColumnIndex("date");
        int idxType = cursor.getColumnIndex("type");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        StringBuilder sb = new StringBuilder();

        while(cursor.moveToNext()) {
            String address = cursor.getString(idxAddress);
            int person = cursor.getInt(idxPerson);
            String body = cursor.getString(idxBody);
            long date = cursor.getLong(idxDate);
            int type = cursor.getInt(idxType);

            String strType;
            switch (type) {
                case 0:
                    strType = "[所有]";
                    break;
                case 1:
                    strType = "[接收]";
                    break;
                case 2:
                    strType = "[发送]";
                    break;
                case 3:
                    strType = "[草稿]";
                    break;
                case 4:
                    strType = "[发件箱]";
                    break;
                case 5:
                    strType = "[发送失败]";
                    break;
                case 6:
                    strType = "[待发送]";
                    break;
                default:
                    strType = "[---]";
                    break;
            }

            sb.append(address + "(" + person + ")");
            sb.append(' ');
            sb.append(dateFormat.format(date));
            sb.append(' ');
            sb.append(strType);
            sb.append('\n');
            sb.append(body);
            sb.append('\n');
            sb.append('\n');
        }

        updateContentView(sb.toString(), false);
    }


    private String getClipboardText() {
        Context context = getContext();
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        if(clipboard.hasPrimaryClip()) {
            if(clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                CharSequence cs = item.getText();
                if(cs != null) {
                    Pattern pattern = Pattern.compile("\\w+");
                    Matcher matcher = pattern.matcher(cs);
                    if(matcher.find()) {
                        String word = matcher.group(0);
                        Log.v("", word);
                        requestYoudao(word);
                        return word;
                    }
                }
            }
        }

        return "";
    }

    private void requestYoudao(String word) {
        if(word == null || word.isEmpty()) {
            Log.e(TAG, "requestYoudao word is null");
            return;
        }

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
                        parseYoudao(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        handleResponseError(e);
                    }
                }
        );
        executeRequest(request);
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

                updateContentView(sb.toString(), false);
            }
        } catch (JSONException e) {
            Log.v(TAG, response.toString());
            handleResponseError(e);
        }
    }

    private void requestWGEO() {
        WeatherRequest request = new WeatherRequest(Request.Method.GET,
                "http://wgeo.weather.com.cn/ip/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseWGEO(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                handleResponseError(e);
            }
        });
        executeRequest(request);
    }

    private void parseWGEO(final String content) {
        if(content.startsWith("<")) {
            Log.e("WGEO", "web address error!");
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

        mCityId = id;
        Log.v(TAG, "parseWGEO mCityId: ");
        Log.v(TAG, mCityId);

        updateContentView(ip + "\n" + addr, false);
    }

    private void requestSK2D(final String id) {
        if(id == null || id.isEmpty()) {
            Log.e(TAG, "requestSK2D id is null");
            return;
        }

        String query = id.replaceAll("\\D+", "");
        WeatherRequest request = new WeatherRequest(Request.Method.GET,
                "http://d1.weather.com.cn/sk_2d/" + query + ".html", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseSK2D(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                handleResponseError(e);
            }
        });

        executeRequest(request);
    }

    private void parseSK2D(final String content) {
        if(content.startsWith("<")) {
            Log.e("SK2D", "web address error!");
            return;
        }

        int valueBegin = content.indexOf('=');
        try {
            JSONObject reader = new JSONObject(content.substring(valueBegin + 1));

            String city = reader.getString("city");
            String temp = reader.getString("temp");
            String weather = reader.getString("weather");
            String cityname = reader.getString("cityname");

            updateContentView(cityname + ", " + weather + ", " + temp + "°C", false);
        } catch (JSONException e) {
            Log.e("SK2D", "json: " + e.getMessage());
        }
    }

    private void handleResponseError(Exception e) {
        String content = "";
        if(e instanceof TimeoutError) {
            content = "Timeout";
        } else if(e instanceof NoConnectionError) {
            content = "NoConnection";
        } else if(e instanceof JSONException) {
            content = "JSONException";
        } else {
            content = "";
            e.printStackTrace();
        }
        updateContentView(content, false);
        mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    private <T> void executeRequest(Request<T> request) {
        if(isRunningRequest) {
            Log.e(TAG, "isRunningRequest is true");
            return;
        }

        if(!isNetworkAvailable()) {
            Log.e(TAG, "isNetworkAvailable is false");
            return;
        }

        if(mRequestQueue == null) {
            Context context = getContext();
            if(context == null) {
                Log.e(TAG, "executeRequest context is null");
                return;
            }
            Log.v(TAG, "executeRequest newRequestQueue");
            mRequestQueue = Volley.newRequestQueue(context);
        }

        isRunningRequest = true;
        mRequestQueue.add(request);

        updateContentView(null, true);
    }

    private void updateContentView(String content, boolean progressbar) {
        RemoteViews contentView = getContentView();
        contentView.setViewVisibility(R.id.update_progress, progressbar ? View.VISIBLE : View.INVISIBLE);
        if(content != null) {
            contentView.setTextViewText(R.id.content_text, content);
        }
        isRunningRequest = progressbar;

        Context context = getContext();
        SlookCocktailManager cocktailManager = SlookCocktailManager.getInstance(context);
        cocktailManager.partiallyUpdateCocktail(mCocktailId, getContentView());
    }

    private RemoteViews getHelpView() {
        if(mEdgeView == null) {
            Context context = getContext();
            mEdgeView = new RemoteViews(context.getPackageName(), R.layout.edge_help_view);

            //cocktailManager.setOnLongClickPendingIntent(mEdgeView, R.id.single_btn1, getLongClickIntent(context, R.id.single_btn1, 0));
            mEdgeView.setTextViewText(R.id.single_btn1, "WGEO");
            mEdgeView.setTextViewText(R.id.single_btn2, "WEATHER");
            mEdgeView.setTextViewText(R.id.single_btn3, "YOUDAO");
            mEdgeView.setTextViewText(R.id.single_btn4, "SYSTEM");
            mEdgeView.setTextViewText(R.id.single_btn5, "SMS");

            // click
            mEdgeView.setOnClickPendingIntent(R.id.single_btn1, getClickIntent(context, R.id.single_btn1, 0));
            mEdgeView.setOnClickPendingIntent(R.id.single_btn2, getClickIntent(context, R.id.single_btn2, 0));
            mEdgeView.setOnClickPendingIntent(R.id.single_btn3, getClickIntent(context, R.id.single_btn3, 0));
            mEdgeView.setOnClickPendingIntent(R.id.single_btn4, getClickIntent(context, R.id.single_btn4, 0));
            mEdgeView.setOnClickPendingIntent(R.id.single_btn5, getClickIntent(context, R.id.single_btn5, 0));
        }
        return mEdgeView;
    }

    private RemoteViews getContentView() {
        if(mContentView == null) {
            Context context = getContext();
            mContentView = new RemoteViews(context.getPackageName(), R.layout.edge_content_view);
        }
        return mContentView;
    }

    private void updateCocktail() {
        Context context = getContext();
        SlookCocktailManager cocktailManager = SlookCocktailManager.getInstance(context);
        cocktailManager.updateCocktail(mCocktailId, getContentView(), getHelpView());
    }

}
