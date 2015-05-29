package app.staples.mobile.cfa.analytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GPBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        com.adobe.mobile.Analytics.processReferrer(c, i);
    }
}

