package com.weihuoya.edgescreen.cocktail;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.weihuoya.edgescreen.R;
import com.weihuoya.edgescreen.weather.WeatherContentProvider;

import java.util.ArrayList;


public class EdgeHelpGridService extends RemoteViewsService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags,startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(getApplicationContext(), intent);
    }

    // helpview
    private class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private ArrayList<Bundle> mDataset;
        private Context mContext;

        public GridRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mDataset = new ArrayList<>();
            handleIntent(intent);
        }

        public void handleIntent(Intent intent) {
            int i = 0;
            Bundle extras = intent.getExtras();
            while(true) {
                Bundle model = extras.getBundle(String.valueOf(i++));
                if(model == null) {
                    break;
                }
                mDataset.add(model);
            }
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return mDataset.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            Bundle model = mDataset.get(position);
            RemoteViews itemView = new RemoteViews(getPackageName(), R.layout.edge_grid_item);
            itemView.setImageViewResource(R.id.action_icon, model.getInt("icon"));
            itemView.setTextViewText(R.id.action_name, model.getString("text"));

            Intent intent = new Intent();
            intent.putExtra("type", model.getInt("type"));
            itemView.setOnClickFillInIntent(R.id.action_root, intent);

            return itemView;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
