package com.samsung.android.cocktailbar;

import android.widget.RemoteViews;
import android.view.DragEvent;
import java.lang.String;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.ComponentName;

import com.samsung.android.cocktailbar.ICocktailHost;
//import com.samsung.android.cocktailbar.CocktailInfo;
//import com.samsung.android.cocktailbar.Cocktail;
//import com.samsung.android.cocktailbar.CocktailBarStateInfo;

interface ICocktailBarService {
    
    void setCocktailHostCallbacks(ICocktailHost p1, String p2, int p3);
    
    void startListening(ICocktailHost p1, String p2, int p3);
    
    void stopListening(String p1);
    
    void setEnabledCocktailIds(in int[] p1);
    
    int[] getEnabledCocktailIds();
    
    int[] getAllCocktailIds();
    
    //Cocktail getCocktail(int p1);
    
    //void updateCocktail(String p1, CocktailInfo p2, int p3);
    
    void partiallyUpdateCocktail(String p1, in RemoteViews p2, int p3);
    
    void partiallyUpdateHelpView(String p1, in RemoteViews p2, int p3);
    
    void showCocktail(String p1, int p2);
    
    void closeCocktail(String p1, int p2, int p3);
    
    int getCocktailId(String p1, in ComponentName p2);
    
    void disableCocktail(String p1, in ComponentName p2);
    
    int[] getCocktailIds(String p1, in ComponentName p2);
    
    boolean isBoundCocktailPackage(String p1, int p2);
    
    boolean isEnabledCocktail(String p1, in ComponentName p2);
    
    void notifyCocktailViewDataChanged(String p1, int p2, int p3);
    
    void setOnPullPendingIntent(String p1, int p2, int p3, in PendingIntent p4);
    
    void bindRemoteViewsService(String p1, int p2, in Intent p3, IBinder p4);
    
    void unbindRemoteViewsService(String p1, int p2, in Intent p3);
    
    boolean requestToUpdateCocktail(int p1);
    
    boolean requestToDisableCocktail(int p1);
    
    boolean requestToUpdateCocktailByCategory(int p1);
    
    boolean requestToDisableCocktailByCategory(int p1);
    
    void notifyKeyguardState(boolean p1);
    
    void notifyCocktailVisibiltyChanged(int p1, int p2);
    
    void sendDragEvent(int p1, in DragEvent p2);
    
    void showAndLockCocktailBar();
    
    void unlockCocktailBar(int p1);
    
    void updateCocktailBarVisibility(int p1);
    
    void updateCocktailBarPosition(int p1);
    
    void setCocktailBarStatus(boolean p1, boolean p2);
    
    void registerCocktailBarStateListenerCallback(IBinder p1, in ComponentName p2);
    
    void unregisterCocktailBarStateListenerCallback(IBinder p1);
    
    int getCocktailBarVisibility();
    
    //CocktailBarStateInfo getCocktailBarStateInfo();
    
    void updateCocktailBarWindowType(String p1, int p2);
    
    int getWindowType();
    
    void activateCocktailBar();
    
    void deactivateCocktailBar();
    
    void setDisableTickerView(int p1);
    
    void updateWakeupGesture(int p1, boolean p2);
    
    void updateWakeupArea(int p1);
    
    void updateLongpressGesture(boolean p1);
    
    void updateSysfsDeadZone(int p1);
    
    void updateSysfsBarLength(int p1);
    
    void setCocktailBarWakeUpState(boolean p1);
    
    boolean getCocktaiBarWakeUpState();
    
    void switchDefaultCocktail();
    
    boolean isAllowTransientBarCocktailBar();
    
    void sendExtraDataToCocktailBar(in Bundle p1);
    
    void removeCocktailUIService();
    
    void cocktailBarshutdown(String p1);
    
    void cocktailBarreboot(String p1);
    
    int getConfigVersion();
    
    int getPreferWidth();
    
    String getCategoryFilterStr();
    
    String getHideEdgeListStr();
}
