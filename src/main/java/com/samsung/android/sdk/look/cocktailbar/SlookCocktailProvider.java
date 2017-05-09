package com.samsung.android.sdk.look.cocktailbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.view.DragEvent;
import android.content.Intent;
import android.os.Bundle;

import com.samsung.android.cocktailbar.CocktailBarManager;

public class SlookCocktailProvider extends BroadcastReceiver {

    private static final String ACTION_COCKTAIL_UPDATE_FEEDS = "com.samsung.android.cocktail.action.COCKTAIL_UPDATE_FEEDS";

    public SlookCocktailProvider() {
    }

    public void onReceive(Context context, Intent intent) {
        Bundle extras;
        String action = intent.getAction();
        switch (action) {
            case CocktailBarManager.ACTION_COCKTAIL_UPDATE:
            case CocktailBarManager.ACTION_COCKTAIL_UPDATE_V2:
                extras = intent.getExtras();
                if(extras != null && extras.containsKey(CocktailBarManager.EXTRA_COCKTAIL_IDS)) {
                    int[] cocktailIds = extras.getIntArray(CocktailBarManager.EXTRA_COCKTAIL_IDS);
                    onUpdate(context, SlookCocktailManager.getInstance(context), cocktailIds);
                }
                break;
            case CocktailBarManager.ACTION_COCKTAIL_ENABLED:
                onEnabled(context);
                break;
            case CocktailBarManager.ACTION_COCKTAIL_DISABLED:
                onDisabled(context);
                break;
            case CocktailBarManager.ACTION_COCKTAIL_VISIBILITY_CHANGED:
                extras = intent.getExtras();
                if(extras != null && extras.containsKey(CocktailBarManager.EXTRA_COCKTAIL_ID)) {
                    int cocktailId =  extras.getInt(CocktailBarManager.EXTRA_COCKTAIL_ID);
                    if(extras.containsKey(CocktailBarManager.EXTRA_COCKTAIL_VISIBILITY)) {
                        int visibility = extras.getInt(CocktailBarManager.EXTRA_COCKTAIL_VISIBILITY);
                        onVisibilityChanged(context, cocktailId, visibility);
                    }
                }
                break;
            case CocktailBarManager.ACTION_COCKTAIL_DROPED:
                extras = intent.getExtras();
                if(extras != null && extras.containsKey(CocktailBarManager.EXTRA_DRAG_EVENT)) {
                    DragEvent dragEvent = intent.getParcelableExtra(CocktailBarManager.EXTRA_DRAG_EVENT);
                    onDroped(context, SlookCocktailManager.getInstance(context), dragEvent);
                }
                break;
            case ACTION_COCKTAIL_UPDATE_FEEDS:
                extras = intent.getExtras();
                if(extras != null && extras.containsKey(CocktailBarManager.EXTRA_COCKTAIL_ID)) {
                    int cocktailId =  extras.getInt(CocktailBarManager.EXTRA_COCKTAIL_ID);
                    onUpdateFeeds(context, SlookCocktailManager.getInstance(context), cocktailId);
                }
                break;
        }
    }

    public void onUpdate(Context context, SlookCocktailManager manager, int[] cocktailIds) {
    }

    public void onEnabled(Context context) {
    }

    public void onDisabled(Context context) {
    }

    public void onVisibilityChanged(Context context, int cocktailId, int visibility) {
    }

    public void onDroped(Context context, SlookCocktailManager manager, DragEvent dragEvent) {
    }

    public void onUpdateFeeds(Context context, SlookCocktailManager manager, int cocktailId) {
    }
}
