package com.staples.mobile.cfa;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AboutFragment extends BaseFragment {
    private static final String TAG = "AboutFragment";

    private SimpleDateFormat dateFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.about_fragment, container, false);
        TableLayout table = (TableLayout) view.findViewById(R.id.about_table);

        dateFormat = new SimpleDateFormat(("yyyy-MM-dd HH:mm"));
        addRow(inflater, table, "Android API version", Integer.toString(Build.VERSION.SDK_INT));
        addDisplayRows(inflater, table);
        addPackageRows(inflater, table);
        addGoogleRows(inflater, table);

        return(view);
    }

    private void addDisplayRows(LayoutInflater inflater, TableLayout table) {
        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        addRow(inflater, table, "Display width", Integer.toString(metrics.widthPixels));
        addRow(inflater, table, "Display height", Integer.toString(metrics.heightPixels));
        addRow(inflater, table, "Pixels per inch", Integer.toString(Math.round(metrics.densityDpi)));
    }

    private void addPackageRows(LayoutInflater inflater, TableLayout table) {
        Activity activity = getActivity();
        String name = activity.getPackageName();
        PackageManager manager = activity.getPackageManager();

        try {
            PackageInfo packInfo = manager.getPackageInfo(name, 0);
            ApplicationInfo appInfo = manager.getApplicationInfo(name, 0);

            ZipFile zf = new ZipFile(appInfo.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long stamp = ze.getTime();
            zf.close();

            addRow(inflater, table, "App version code", Integer.toString(packInfo.versionCode));
            addRow(inflater, table, "App version name", packInfo.versionName);
            addRow(inflater, table, "First install", dateFormat.format(packInfo.firstInstallTime));
            addRow(inflater, table, "Last build", dateFormat.format(stamp));
            addRow(inflater, table, "Last install", dateFormat.format(packInfo.lastUpdateTime));
        } catch(Exception e) {}
    }

    private void addGoogleRows(LayoutInflater inflater, TableLayout table) {
        Resources res = getActivity().getResources();
        int x = res.getInteger(R.integer.google_play_services_version);
        int a = x/1000000; // 2 digits
        x -= 1000000*a;
        int b = x/100000; // 1 digit
        x -= 100000*b;
        int c = x/1000; // 2 digits
        x -= 1000*c; // optional 3 digits
        String version = Integer.toString(a)+"."+Integer.toString(b)+"."+Integer.toString(c);
        if (x>0) version = version+"."+Integer.toString(x);
        addRow(inflater, table, "Google Play", version);
    }

    private void addRow(LayoutInflater inflater, TableLayout table, String key, String value) {
        TableRow row = (TableRow) inflater.inflate(R.layout.about_item, table, false);
        table.addView(row);
        ((TextView) row.findViewById(R.id.about_key)).setText(key);
        ((TextView) row.findViewById(R.id.about_value)).setText(value);
    }
}
