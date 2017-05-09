package com.samsung.android.sdk.look.cocktailbar;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;

import com.samsung.android.cocktailbar.CocktailBarManager;
import com.samsung.android.cocktailbar.ICocktailBarService;
import com.samsung.android.cocktailbar.SemCocktailBarManager;
import com.samsung.android.sdk.look.Slook;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.WeakHashMap;


public final class SlookCocktailManager {
    private static WeakHashMap<Context, WeakReference<SlookCocktailManager>> sManagerCache = new WeakHashMap();

    private Slook mSlook;
    private Context mContext;
    private CocktailBarManager mCocktailBarManager;
    //private SemCocktailBarManager mSemCocktailBarManager;

    public static SlookCocktailManager getInstance(Context context) {
        synchronized (sManagerCache) {
            if (context == null) {
                throw new IllegalArgumentException("context is null.");
            }

            if (((context instanceof ContextWrapper)) &&
                    (((ContextWrapper) context).getBaseContext() == null)) {
                throw new IllegalArgumentException("Base context is null.");
            }

            WeakReference<SlookCocktailManager> ref = (WeakReference) sManagerCache.get(context);
            SlookCocktailManager result = null;
            if (ref != null) {
                result = (SlookCocktailManager) ref.get();
            }

            if (result == null) {
                result = new SlookCocktailManager(context);
                result.initSemFeature();
                sManagerCache.put(context, new WeakReference(result));
            }

            return result;
        }
    }

    private SlookCocktailManager(Context context) {
        mContext = context;
        mSlook = new Slook();
    }

    private void initSemFeature() {
        if (mSlook.isFeatureEnabled(Slook.COCKTAIL_PANEL)) {
            PackageManager packageManager = mContext.getPackageManager();
            mCocktailBarManager = CocktailBarManager.getInstance(mContext);
            //if (packageManager.hasSystemFeature("com.samsung.feature.samsung_experience_mobile")) {
            //    mSemCocktailBarManager = SemCocktailBarManager.getInstance(mContext);
            //}
            //ICocktailBarService service = getService();
        }
    }

    public void updateCocktailHelpView(int cocktailId, RemoteViews helpView) {
        mCocktailBarManager.updateCocktailHelpView(cocktailId, helpView);
    }

    public void updateCocktailView(int cocktailId, RemoteViews contentView) {
        mCocktailBarManager.updateCocktailView(cocktailId, contentView);
    }

    public void partiallyUpdateCocktail(int cocktailId, RemoteViews view) {
        mCocktailBarManager.partiallyUpdateCocktail(cocktailId, view);
    }

    public void partiallyUpdateHelpView(int cocktailId, RemoteViews view) {
        mCocktailBarManager.partiallyUpdateHelpView(cocktailId, view);
    }


    public void updateCocktail(int cocktailId, RemoteViews contentView, Bundle content) {
        mCocktailBarManager.updateCocktail(cocktailId,
                CocktailBarManager.COCKTAIL_DISPLAY_POLICY_GENERAL,
                CocktailBarManager.COCKTAIL_CATEGORY_GLOBAL,
                contentView,
                content);
    }

    public void updateCocktail(int cocktailId, RemoteViews contentView, RemoteViews helpView) {
        mCocktailBarManager.updateCocktail(cocktailId,
                CocktailBarManager.COCKTAIL_DISPLAY_POLICY_GENERAL,
                CocktailBarManager.COCKTAIL_CATEGORY_GLOBAL,
                contentView,
                helpView);
    }

    public void updateCocktail(int cocktailId, RemoteViews contentView, RemoteViews helpView, Bundle content) {
        mCocktailBarManager.updateCocktail(cocktailId,
                CocktailBarManager.COCKTAIL_DISPLAY_POLICY_GENERAL,
                CocktailBarManager.COCKTAIL_CATEGORY_GLOBAL,
                contentView,
                helpView,
                content);
    }

    public void updateCocktail(int cocktailId, RemoteViews contentView, RemoteViews helpView, Bundle content, ComponentName component) {
        mCocktailBarManager.updateCocktail(cocktailId,
                CocktailBarManager.COCKTAIL_DISPLAY_POLICY_GENERAL,
                CocktailBarManager.COCKTAIL_CATEGORY_GLOBAL,
                contentView,
                helpView,
                content,
                component);
    }

    // force verify samsung signature and package name in
    // com.samsung.android.app.cocktailbarservice.ui.panel.AbstractRemotePanelView.composeCocktailWithLoadableInfo()
    // com.samsung.android.app.cocktailbarservice.util.CocktailWhiteLists.isAllowedToUseClassLoader()
    public void updateCocktail(int cocktailId, Class panelClass, Bundle content, RemoteViews contentView) {
        mCocktailBarManager.updateCocktail(cocktailId,
                CocktailBarManager.COCKTAIL_DISPLAY_POLICY_GENERAL,
                CocktailBarManager.COCKTAIL_CATEGORY_GLOBAL,
                panelClass,
                content,
                contentView);
    }


    public void notifyCocktailViewDataChanged(int cocktailId, int viewId) {
        mCocktailBarManager.notifyCocktailViewDataChanged(cocktailId, viewId);
    }

    public int[] getCocktailIds(ComponentName provider) {
        return mCocktailBarManager.getCocktailIds(provider);
    }

    public void showCocktail(int cocktailId) {
        mCocktailBarManager.showCocktail(cocktailId);
    }

    public void closeCocktail(int cocktailId, int category) {
        mCocktailBarManager.closeCocktail(cocktailId, category);
    }

    public void disableCocktail(ComponentName provider) {
        mCocktailBarManager.disableCocktail(provider);
    }

    public boolean isEnabledCocktail(ComponentName provider) {
        return mCocktailBarManager.isEnabledCocktail(provider);
    }

    public int getCocktailBarWindowType() {
        return mCocktailBarManager.getCocktailBarWindowType();
    }

    public void setLongPressEnabled(boolean enabled) {
        mCocktailBarManager.updateLongpressGesture(enabled);
    }

    public void setCocktailBarStatus(boolean shift, boolean transparent) {
        mCocktailBarManager.setCocktailBarStatus(shift, transparent);
    }


    public void setOnPullPendingIntent(int cocktailId, int viewId, PendingIntent pendingIntent) {
        mCocktailBarManager.setOnPullPendingIntent(cocktailId, viewId, pendingIntent);
    }


    //
    public void setOnLongClickPendingIntent(RemoteViews rv, int viewId, PendingIntent longClickPendingIntent) {
        final String className = "android.widget.RemoteViews";
        final String methodName = "semSetOnLongClickPendingIntent";

        if(mRemoteViewsClass == null) {
            try {
                mRemoteViewsClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                System.err.println(className + " Unable to load class " + e);
            }
        }

        if(mRemoteViewsClass != null) {
            Class[] argType = {Integer.TYPE, PendingIntent.class};
            if(semSetOnLongClickPendingIntentMethod == null) {
                try {
                    semSetOnLongClickPendingIntentMethod = mRemoteViewsClass.getMethod(methodName, argType);
                } catch (NoSuchMethodException e) {
                    System.err.println(methodName + " Unable to load method " + e);
                }
            }
            if(semSetOnLongClickPendingIntentMethod != null) {
                try {
                    semSetOnLongClickPendingIntentMethod.invoke(rv, viewId, longClickPendingIntent);
                } catch (IllegalAccessException e) {
                    System.err.println(className + " IllegalAccessException encountered invoking " + methodName + e);
                } catch (InvocationTargetException e) {
                    System.err.println(className + " InvocationTargetException encountered invoking " + methodName + e);
                    e.printStackTrace();
                }
            }
        }
    }

    public void setOnLongClickPendingIntentTemplate(RemoteViews rv, int viewId, PendingIntent pendingIntentTemplate) {
        final String className = "android.widget.RemoteViews";
        final String methodName = "semSetOnLongClickPendingIntentTemplate";

        if(mRemoteViewsClass == null) {
            try {
                mRemoteViewsClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                System.err.println(className + " Unable to load class " + e);
            }
        }

        if(mRemoteViewsClass != null) {
            Class[] argType = {Integer.TYPE, PendingIntent.class};
            if(semSetOnLongClickPendingIntentTemplateMethod == null) {
                try {
                    semSetOnLongClickPendingIntentTemplateMethod = mRemoteViewsClass.getMethod(methodName, argType);
                } catch (NoSuchMethodException e) {
                    System.err.println(methodName + " Unable to load method " + e);
                }
            }
            if(semSetOnLongClickPendingIntentTemplateMethod != null) {
                try {
                    semSetOnLongClickPendingIntentTemplateMethod.invoke(rv, viewId, pendingIntentTemplate);
                } catch (IllegalAccessException e) {
                    System.err.println(className + " IllegalAccessException encountered invoking " + methodName + e);
                } catch (InvocationTargetException e) {
                    System.err.println(className + " InvocationTargetException encountered invoking " + methodName + e);
                    e.printStackTrace();
                }
            }
        }
    }

    private static Class<?> mRemoteViewsClass = null;
    private static Method semSetOnLongClickPendingIntentMethod = null;
    private static Method semSetOnLongClickPendingIntentTemplateMethod = null;


    //
    private ICocktailBarService getService() {
        if(mICocktailBarService == null) {
            final String className = "android.os.ServiceManager";
            final String methodName = "getService";
            final String serviceName = "CocktailBarService";
            if (mServiceManagerClass == null) {
                try {
                    mServiceManagerClass = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    System.err.println(className + " Unable to load class " + e);
                }
            }
            if (mServiceManagerClass != null) {
                Class[] argType = {String.class};
                if (getServiceMethod == null) {
                    try {
                        getServiceMethod = mServiceManagerClass.getMethod(methodName, argType);
                    } catch (NoSuchMethodException e) {
                        System.err.println(methodName + " Unable to load method " + e);
                    }
                }
                if (getServiceMethod != null) {
                    try {
                        Object service = getServiceMethod.invoke(null, serviceName);
                        mICocktailBarService = ICocktailBarService.Stub.asInterface((IBinder) service);
                    } catch (IllegalAccessException e) {
                        System.err.println(className + " IllegalAccessException encountered invoking " + methodName + e);
                    } catch (InvocationTargetException e) {
                        System.err.println(className + " InvocationTargetException encountered invoking " + methodName + e);
                        e.printStackTrace();
                    }
                }
            }
        }
        return mICocktailBarService;
    }

    private static Class<?> mServiceManagerClass = null;
    private static Method getServiceMethod = null;
    private static ICocktailBarService mICocktailBarService = null;
}
