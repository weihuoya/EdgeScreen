package com.weihuoya.edgescreen.cocktail;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;

import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;

import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SystemIntoUtils {

    private static ConnectivityManager sConnectivityManager = null;
    private static TelephonyManager sTelephonyManager = null;
    private static ActivityManager sActivityManager = null;
    private static ClipboardManager sClipboardManager = null;

    private static String[] sCallLogProjection = {
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.NEW
    };

    private static String[] sSmsProjection = {
            Telephony.TextBasedSmsColumns.ADDRESS,
            Telephony.TextBasedSmsColumns.PERSON,
            Telephony.TextBasedSmsColumns.BODY,
            Telephony.TextBasedSmsColumns.DATE,
            Telephony.TextBasedSmsColumns.TYPE
    };


    private static String[] sContactProjection = {
            //ContactsContract.Contacts._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            //ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
    };

    public static boolean isNetworkAvailable(Context context) {
        if(sConnectivityManager == null) {
            sConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        ConnectivityManager connectivityManager = sConnectivityManager;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo.isAvailable() && networkInfo.isConnected()) {
            return true;
        } else {
            if(sTelephonyManager == null) {
                sTelephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            }
            TelephonyManager telephonyManager = sTelephonyManager;
            if(telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY &&
                    telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
                return true;
            }
        }
        return false;
    }

    public static String getSystemInfo(Context context) {
        String availableSize;
        String totalSize;

        StringBuilder sb = new StringBuilder();

        sb.append("Board: ");
        sb.append(Build.BOARD);
        sb.append('\n');

        sb.append("Device: ");
        sb.append(Build.DEVICE);
        sb.append('\n');

        try {
            String macAddress = "00:00:00:00:00:00";
            NetworkInterface networkInterface = NetworkInterface.getByName("eth1");

            if(networkInterface == null) {
                networkInterface = NetworkInterface.getByName("wlan0");
            }

            if(networkInterface != null) {
                StringBuilder sbMac = new StringBuilder();
                byte[] addr = networkInterface.getHardwareAddress();
                for(byte b : addr) {
                    sbMac.append(String.format("%02X:", b));
                }
                if(sbMac.length() > 0) {
                    sbMac.deleteCharAt(sbMac.length() - 1);
                }
                macAddress = sbMac.toString();
            }

            sb.append("Mac: ");
            sb.append('\n');
            sb.append(macAddress);
            sb.append('\n');
        } catch (SocketException e) {
            e.printStackTrace();
        }

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
            sb.append('\n');
            sb.append(availableSize + " / " + totalSize);
            sb.append('\n');
        }

        // memory
        if(sActivityManager == null) {
            sActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        ActivityManager am = sActivityManager;
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);

        availableSize = Formatter.formatFileSize(context, memoryInfo.availMem);
        totalSize = Formatter.formatFileSize(context, memoryInfo.totalMem);
        sb.append("Memory: ");
        sb.append('\n');
        sb.append(availableSize + " / " + totalSize);

        return sb.toString();
    }

    public static String getPhoneContact(Context context, final int person, final String number) {
        String displayName = null;

        if(context.checkSelfPermission(Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            return displayName;
        }

        ContentResolver contentResolver = context.getContentResolver();
        String selection = ContactsContract.Contacts._ID + " = ?";
        String[] selectionArgs = {String.valueOf(person)};
        if(person == 0 && number != null) {
            selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";
            selectionArgs[0] = number;
        }

        Cursor cursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, sContactProjection, selection, selectionArgs, null);
        if(cursor == null) {
            return displayName;
        }

        if(cursor.moveToNext()) {
            int idxDisplayName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            displayName = cursor.getString(idxDisplayName);
        }

        return displayName;
    }

    public static List<String> getCallLog(Context context, long timeElapsed, int limit) {
        List<String> loglist = new ArrayList<>();

        if(context.checkSelfPermission(Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            return loglist;
        }

        ContentResolver contentResolver = context.getContentResolver();

        long timestamp = System.currentTimeMillis() - timeElapsed;
        String selection = CallLog.Calls.DATE + " > ?";
        String[] selectionArgs = {String.valueOf(timestamp)};
        String sortOrder = CallLog.Calls.DEFAULT_SORT_ORDER + " limit " + limit;
        Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, sCallLogProjection, selection, selectionArgs, sortOrder);
        if(cursor == null) {
            return loglist;
        }

        int idxCachedName = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int idxNumber = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int idxType = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int idxDate = cursor.getColumnIndex(CallLog.Calls.DATE);
        int idxDuration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        int idxNew = cursor.getColumnIndex(CallLog.Calls.NEW);

        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();

        while(cursor.moveToNext()) {
            String displayName = cursor.getString(idxCachedName);
            String number = cursor.getString(idxNumber);
            int type = cursor.getInt(idxType);
            long date = cursor.getLong(idxDate);
            long duration = cursor.getLong(idxDuration);
            int newFlag = cursor.getInt(idxNew);

            String strType;
            switch (type) {
                case CallLog.Calls.INCOMING_TYPE :
                    strType = "[来电]";
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    strType = "[已拨]";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    strType = "[未接]";
                    break;
                case CallLog.Calls.VOICEMAIL_TYPE:
                    strType = "[语音]";
                    break;
                case CallLog.Calls.REJECTED_TYPE:
                    strType = "[拒接]";
                    break;
                case CallLog.Calls.BLOCKED_TYPE:
                    strType = "[屏蔽]";
                    break;
                default:
                    strType = "[-" + type + "-]";
                    break;
            }

            StringBuilder sb = new StringBuilder();

            if(displayName == null) {
                displayName = getPhoneContact(context, 0, number);
                if(displayName == null) {
                    displayName = number;
                    Log.v("weihuoya", "getCallLog: " + date + " > " + timestamp);
                }
            }
            sb.append(displayName);
            sb.append('\n');
            sb.append(dateFormat.format(date));
            sb.append('\n');
            sb.append(strType);
            //sb.append('\n');
            //sb.append(duration);
            loglist.add(sb.toString());
        }
        cursor.close();

        return loglist;
    }

    public static List<String> getSMS(Context context, long timeElapsed, int limit) {
        List<String> smslist = new ArrayList<>();

        if(context.checkSelfPermission(Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return smslist;
        }

        //Uri URI_SMS = Uri.parse("content://sms/");
        ContentResolver contentResolver = context.getContentResolver();

        long timestamp = System.currentTimeMillis() - timeElapsed;
        String selection = Telephony.TextBasedSmsColumns.DATE + " > ?";
        String[] selectionArgs = {String.valueOf(timestamp)};
        String sortOrder = Telephony.Sms.DEFAULT_SORT_ORDER + " limit " + limit;
        Cursor cursor = contentResolver.query(Telephony.Sms.CONTENT_URI, sSmsProjection, selection, selectionArgs, sortOrder);
        if(cursor == null) {
            return smslist;
        }

        int idxAddress = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.ADDRESS);
        int idxPerson = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.PERSON);
        int idxBody = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.BODY);
        int idxDate = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.DATE);
        int idxType = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.TYPE);
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();

        while(cursor.moveToNext()) {
            String address = cursor.getString(idxAddress);
            int person = cursor.getInt(idxPerson);
            String body = cursor.getString(idxBody);
            long date = cursor.getLong(idxDate);
            int type = cursor.getInt(idxType);
            String displayName = getPhoneContact(context, person, address);

            String strType;
            switch (type) {
                case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_ALL:
                    strType = "[所有]";
                    break;
                case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX:
                    strType = "[接收]";
                    break;
                case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
                    strType = "[发送]";
                    break;
                case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT:
                    strType = "[草稿]";
                    break;
                case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX:
                    strType = "[发件箱]";
                    break;
                case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED:
                    strType = "[发送失败]";
                    break;
                case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_QUEUED:
                    strType = "[待发送]";
                    break;
                default:
                    strType = "[---]";
                    break;
            }

            StringBuilder sb = new StringBuilder();
            if(displayName == null){
                displayName = address;
                Log.v("weihuoya", "getSMS: " + date + " > " + timestamp);
            }
            sb.append(displayName);
            sb.append(' ');
            sb.append(strType);
            sb.append('\n');
            sb.append(dateFormat.format(date));
            sb.append('\n');
            sb.append(body);

            smslist.add(sb.toString());
        }
        cursor.close();

        return smslist;
    }

    public static String getClipboardText(Context context) {
        if(sClipboardManager == null) {
            sClipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        }
        String text = "";
        ClipboardManager clipboard = sClipboardManager;

        if(!clipboard.hasPrimaryClip()) {
            return text;
        }

        if(!clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            return text;
        }

        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
        CharSequence cs = item.getText();
        if(cs == null) {
            return text;
        }

        Pattern pattern = Pattern.compile("\\w+");
        Matcher matcher = pattern.matcher(cs);
        if(!matcher.find()) {
            return text;
        }

        return matcher.group(0);
    }
}
