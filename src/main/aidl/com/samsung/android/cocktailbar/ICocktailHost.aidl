package com.samsung.android.cocktailbar;

import android.os.Bundle;
import android.widget.RemoteViews;
import android.app.PendingIntent;
//import com.samsung.android.cocktailbar.Cocktail;

interface ICocktailHost {
    
    //void updateCocktail(int p1, Cocktail p2, int p3);
    
    void partiallyUpdateCocktail(int p1, in RemoteViews p2, int p3);
    
    void removeCocktail(int p1, int p2);
    
    void showCocktail(int p1, int p2);
    
    void closeContextualCocktail(int p1, int p2, int p3);
    
    void viewDataChanged(int p1, int p2, int p3);
    
    void setPullToRefresh(int p1, int p2, in PendingIntent p3, int p4);
    
    void updateToolLauncher(int p1);
    
    void notifyKeyguardState(boolean p1, int p2);
    
    void notifyWakeUpState(boolean p1, int p2, int p3);
    
    void switchDefaultCocktail(int p1);
    
    void sendExtraData(int p1, in Bundle p2);
    
    void setDisableTickerView(int p1, int p2);
    
    void changeVisibleEdgeService(boolean p1, int p2);
}
