package com.samsung.android.sdk.look;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.samsung.android.sdk.SsdkInterface;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.SsdkVendorCheck;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public final class Slook implements SsdkInterface {
    public static final int AIRBUTTON = 1;
    public static final int SMARTCLIP = 2;
    public static final int WRITINGBUDDY = 3;
    public static final int SPEN_HOVER_ICON = 4;
    public static final int SPEN_BEZEL_INTERACTION = 5;
    public static final int COCKTAIL_BAR = 6;
    public static final int COCKTAIL_PANEL = 7;
    private static final String VERSION_NAME = "1.3.0";
    private static final int VERSION_CODE = 6;

    public Slook() {
    }

    public int getVersionCode() {
        return VERSION_CODE;
    }

    public String getVersionName() {
        return VERSION_NAME;
    }

    public void initialize(Context arg0) throws SsdkUnsupportedException {
        if (!SsdkVendorCheck.isSamsungDevice()) {
            throw new SsdkUnsupportedException("This device is not samsung product.",
                    SsdkUnsupportedException.VENDOR_NOT_SUPPORTED);
        }
        if (!isSupportedDevice()) {
            throw new SsdkUnsupportedException("This device is not supported.",
                    SsdkUnsupportedException.DEVICE_NOT_SUPPORTED);
        }
        try {
            insertLog(arg0);
        } catch (SecurityException e) {
            throw new SecurityException("com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY permission is required.");
        }
    }

    private boolean isSupportedDevice() {
        int firstFeature = 1;
        int lastFeature = 7;
        for (int type = firstFeature; type <= lastFeature; type++) {
            switch (type) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    if ((Build.VERSION.SDK_INT >= 17) &&
                            (SlookImpl_isFeatureEnabled(type))) {
                        return true;
                    }

                    break;
                case 6:
                    if ((Build.VERSION.SDK_INT >= 19) &&
                            (SlookImpl_isFeatureEnabled(type))) {
                        return true;
                    }

                    break;
                case 7:
                    if (Build.VERSION.SDK_INT >= 22) {
                        if (SlookImpl_isFeatureEnabled(type)) {
                            return true;
                        }
                    } else if ((Build.VERSION.SDK_INT >= 19) &&
                            (SlookImpl_isFeatureEnabled(COCKTAIL_BAR))) {
                        return true;
                    }
                    break;
            }

        }
        return false;
    }

    public boolean isFeatureEnabled(int type) {
        switch (type) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                if (Build.VERSION.SDK_INT < 17) {
                    return false;
                }
                return SlookImpl_isFeatureEnabled(type);
            case 6:
                if (Build.VERSION.SDK_INT < 19) {
                    return false;
                }
                return SlookImpl_isFeatureEnabled(type);
            case 7:
                if (Build.VERSION.SDK_INT < 19)
                    return false;
                if (Build.VERSION.SDK_INT < 22) {
                    return SlookImpl_isFeatureEnabled(COCKTAIL_BAR);
                }
                return SlookImpl_isFeatureEnabled(type);
        }
        throw new IllegalArgumentException("The type(" + type + ") is not supported.");
    }

    private void insertLog(Context context) {
        int version = -1;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    "com.samsung.android.providers.context", PackageManager.GET_META_DATA);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("weihuoya", "Could not find ContextProvider");
        }
        Log.d("weihuoya", "versionCode: " + version);

        if (version > 1) {
            if (context.checkCallingOrSelfPermission("com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY")
                    != PackageManager.PERMISSION_GRANTED) {
                throw new SecurityException();
            }
        } else {
            Log.d("weihuoya", "Add com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY permission");
            return;
        }

        ContentValues cv = new ContentValues();
        String appId = getClass().getPackage().getName();
        String feature = context.getPackageName() + "#" + getVersionCode();
        cv.put("app_id", appId);
        cv.put("feature", feature);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.samsung.android.providers.context.log.action.USE_APP_FEATURE_SURVEY");
        broadcastIntent.putExtra("data", cv);
        broadcastIntent.setPackage("com.samsung.android.providers.context");
        context.sendBroadcast(broadcastIntent);
    }


    private boolean SlookImpl_isFeatureEnabled(int type) {
        final String className = "com.samsung.android.sdk.look.SlookImpl";
        final String methodName = "isFeatureEnabled";

        if(mSlookImplClass == null) {
            try {
                mSlookImplClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                System.err.println(className + " Unable to load class " + e);
            }
        }

        if(mSlookImplClass != null) {
            Class[] argType = {Integer.TYPE};
            if(isFeatureEnabledMethod == null) {
                try {
                    isFeatureEnabledMethod = mSlookImplClass.getMethod(methodName, argType);
                } catch (NoSuchMethodException e) {
                    System.err.println(methodName + " Unable to load method " + e);
                }
            }
            if(isFeatureEnabledMethod != null) {
                try {
                    return (boolean)isFeatureEnabledMethod.invoke(null, type);
                } catch (IllegalAccessException e) {
                    System.err.println(className + " IllegalAccessException encountered invoking " + methodName + e);
                } catch (InvocationTargetException e) {
                    System.err.println(className + " InvocationTargetException encountered invoking " + methodName + e);
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    private static Class<?> mSlookImplClass = null;
    private static Method isFeatureEnabledMethod = null;
}
