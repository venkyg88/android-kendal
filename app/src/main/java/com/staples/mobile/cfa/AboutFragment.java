package com.staples.mobile.cfa;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Geocoder;
import android.location.Location;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.staples.mobile.cfa.location.LocationFinder;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AboutFragment extends BaseFragment {
    private static final String TAG = "AboutFragment";

    private SimpleDateFormat dateFormat;
    private DecimalFormat coordFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.about_fragment, container, false);
        TableLayout table = (TableLayout) view.findViewById(R.id.about_table);
        dateFormat = new SimpleDateFormat(("yyyy-MM-dd HH:mm"));
        coordFormat = new DecimalFormat("0.0000");

        addRow(inflater, table, "Android API version", Integer.toString(Build.VERSION.SDK_INT));
        addDisplayRows(inflater, table);
        addPackageRows(inflater, table);
        addGoogleRows(inflater, table);
        addLocationRows(inflater, table);

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

    private String formatGooglePlayVersion() {
        Resources res = getActivity().getResources();
        int x = res.getInteger(R.integer.google_play_services_version);
        int a = x / 1000000; // 2 digits
        x -= 1000000 * a;
        int b = x / 100000; // 1 digit
        x -= 100000 * b;
        int c = x / 1000; // 2 digits
        x -= 1000 * c; // optional 3 digits
        String version = Integer.toString(a) + "." + Integer.toString(b) + "." + Integer.toString(c);
        if (x > 0) version = version + "." + Integer.toString(x);
        return(version);
    }

    private void addGoogleRows(LayoutInflater inflater, TableLayout table) {
        String play;
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        switch(status) {
            case ConnectionResult.SUCCESS:
                play = formatGooglePlayVersion();
                break;
            case ConnectionResult.SERVICE_MISSING:
                play = "Missing";
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                play = "Update required";
                break;
            case ConnectionResult.SERVICE_DISABLED:
                play = "Disabled";
                break;
            default:
                play = "Error "+status;
                break;
        }
        addRow(inflater, table, "Google Play", play);

        String geo;
        if (Geocoder.isPresent()) geo = "Yes";
        else geo = "No";
        addRow(inflater, table, "Geocoder", geo);
    }

    private String formatElapsedTime(long time) {
        if (time==0) return("Never");

        // Explode duration
        int seconds = (int) ((System.currentTimeMillis()-time+500)/1000);
        int minutes = seconds/60;
        seconds -= 60*minutes;
        int hours = minutes/60;
        minutes -= 60*hours;
        int days = hours/24;
        hours -= 24*hours;

        // Format fields
        StringBuilder sb = new StringBuilder();
        if (days>0) {
            sb.append(Integer.toString(days));
            sb.append("d ");
        }
        if (hours>0 || sb.length()>0) {
            sb.append(Integer.toString(hours));
            sb.append("h ");
        }
        if (minutes>0 || sb.length()>0) {
            sb.append(Integer.toString(minutes));
            sb.append("m ");
        }
        sb.append(Integer.toString(seconds));
        sb.append("s");
        return(sb.toString());
    }

    private void addLocationRows(LayoutInflater inflater, TableLayout table) {
        LocationFinder finder = LocationFinder.getInstance(getActivity());
        Location location = finder.getLocation();
        if (location!=null) {
            String coords = "[" + coordFormat.format(location.getLatitude()) + ", " +
                    coordFormat.format(location.getLongitude()) + "]";
            addRow(inflater, table, "Location", coords);
            addRow(inflater, table, "Last fix", formatElapsedTime(location.getTime()));
        } else {
            addRow(inflater, table, "Location", "Not available");
            addRow(inflater, table, "Last fix", "Never");
        }

        String postalCode = finder.getPostalCode();
        if (postalCode==null) postalCode = "Not available";
        addRow(inflater, table, "Postal code", postalCode);
    }

    private void addRow(LayoutInflater inflater, TableLayout table, String key, String value) {
        TableRow row = (TableRow) inflater.inflate(R.layout.about_item, table, false);
        table.addView(row);
        ((TextView) row.findViewById(R.id.about_key)).setText(key);
        ((TextView) row.findViewById(R.id.about_value)).setText(value);
    }
}