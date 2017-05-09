package com.samsung.android.sdk;

import android.content.Context;

public interface SsdkInterface {
    void initialize(Context paramContext) throws SsdkUnsupportedException;
    boolean isFeatureEnabled(int paramInt);
    int getVersionCode();
    String getVersionName();
}
