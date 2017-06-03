package com.weihuoya.edgescreen.cocktail;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.weihuoya.edgescreen.R;
import com.weihuoya.edgescreen.weather.WeatherContentProvider;

/**
 * Created by zhangwei1 on 2017/5/10.
 */

public class EdgeContentListService extends RemoteViewsService {

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
        return new ListRemoteViewsFactory(getApplicationContext(), intent);
    }

    // contentview
    private class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private Context mContext;
        private Cursor mCursor;

        public ListRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            if (mCursor != null) {
                mCursor.close();
            }
            String sortOrder = "id DESC";
            Uri CONTENT_URI = Uri.parse("content://" + WeatherContentProvider.AUTHORITY + "/content");
            mCursor = mContext.getContentResolver().query(CONTENT_URI, null, null, null, sortOrder);
        }

        @Override
        public void onDestroy() {
            if (mCursor != null) {
                mCursor.close();
            }
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            int id = 0;
            int type = 0;
            String title = "";
            String content = "";

            if(mCursor.moveToPosition(position)) {
                int idxId = mCursor.getColumnIndex("id");
                int idxType = mCursor.getColumnIndex("type");
                int idxTitle = mCursor.getColumnIndex("title");
                int idxContent = mCursor.getColumnIndex("content");
                id = mCursor.getInt(idxId);
                type = mCursor.getInt(idxType);
                title = mCursor.getString(idxTitle);
                content = mCursor.getString(idxContent);
            }

            RemoteViews itemView = new RemoteViews(getPackageName(), R.layout.edge_list_item);
            itemView.setTextViewText(R.id.action_title, title);
            itemView.setTextViewText(R.id.action_name, content);

            Intent intent = new Intent();
            intent.putExtra("item_id", id);
            intent.putExtra("type", type);
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
