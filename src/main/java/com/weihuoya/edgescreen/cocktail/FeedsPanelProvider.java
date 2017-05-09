package com.weihuoya.edgescreen.cocktail;

import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager;
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider;
import com.weihuoya.edgescreen.R;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;


public class FeedsPanelProvider extends SlookCocktailProvider {

    private static final String TAG = FeedsPanelProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, SlookCocktailManager cocktailManager, int[] cocktailIds) {
        RemoteViews panel = new RemoteViews(context.getPackageName(), R.layout.feeds_content_view);
        String str = context.getResources().getString(R.string.feeds_panel_name);
        panel.setTextViewText(R.id.feeds_text, str);

        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.edge_content_view);
        view.setTextViewText(R.id.content_text, "content text");

        cocktailManager.updateCocktail(cocktailIds[0], panel, view);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        Log.d(TAG, "onReceive: " + action);
    }

}
