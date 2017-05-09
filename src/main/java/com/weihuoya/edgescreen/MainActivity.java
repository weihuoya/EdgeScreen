package com.weihuoya.edgescreen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(getString(R.string.app_name));

        handleIntent(getIntent());
    }

    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        if(!Intent.ACTION_MAIN.equals(action)) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if(itemId == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }

}
