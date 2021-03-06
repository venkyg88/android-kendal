package app.staples.mobile.cfa;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Geocoder;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.staples.mobile.common.analytics.Tracker;

import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import app.staples.R;
import app.staples.mobile.cfa.location.LocationFinder;
import app.staples.mobile.cfa.widget.ActionBar;

public class AboutFragment extends Fragment {
    private static final String TAG = AboutFragment.class.getSimpleName();

    private static final String[] orientations = {"unspecified", "landscape", "portrait", "user",
                                                  "behind", "sensor", "nosensor", "sensorLandscape",
                                                  "sensorPortrait", "reverseLandscape", "reversePortrait", "fullSensor",
                                                  "userLandscape", "userPortrait", "fullUser", "locked"};

    private SimpleDateFormat dateFormat;
    private DecimalFormat coordFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.about_fragment, container, false);
        view.setTag(this);

        TableLayout table = (TableLayout) view.findViewById(R.id.about_table);
        dateFormat = new SimpleDateFormat(("yyyy-MM-dd HH:mm"));
        coordFormat = new DecimalFormat("0.0000");

        addIdRows(inflater, table);
        addRow(inflater, table, "Android API version", Integer.toString(Build.VERSION.SDK_INT));
        addDisplayRows(inflater, table);
        addPackageRows(inflater, table);
        addGoogleRows(inflater, table);
        addLocationRows(inflater, table);

        return(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        Tracker.getInstance().trackStateForAbout(); // analytics
        ActionBar.getInstance().setConfig(ActionBar.Config.ABOUT);
    }

    private TableRow addRow(LayoutInflater inflater, TableLayout table, String key, String value) {
        TableRow row = (TableRow) inflater.inflate(R.layout.about_item, table, false);
        table.addView(row);
        ((TextView) row.findViewById(R.id.about_key)).setText(key);
        ((TextView) row.findViewById(R.id.about_value)).setText(value);
        return(row);
    }

    private String formatDensity(int density) {
        switch(density) {
            case DisplayMetrics.DENSITY_LOW:
                return("ldpi");
            case DisplayMetrics.DENSITY_MEDIUM:
                return("mdpi");
            case DisplayMetrics.DENSITY_HIGH:
                return("hdpi");
            case DisplayMetrics.DENSITY_XHIGH:
                return("xhdpi");
            case DisplayMetrics.DENSITY_XXHIGH:
                return("xxhdpi");
            case DisplayMetrics.DENSITY_XXXHIGH:
                return ("xxxhdpi");
            case DisplayMetrics.DENSITY_TV:
                return ("tvdpi");
            default:
                return(null);
        }
    }

    private String getAirWatchId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch(Exception e) {
            return (null);
        }

        digest.update(androidId.getBytes());
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for(byte x : bytes) {
            sb.append(String.format("%02X", x));
        }
        return (sb.toString());
    }

    private void addIdRows(LayoutInflater inflater, TableLayout table) {
        // Android id
        ContentResolver cr = getActivity().getContentResolver();
        String id = Settings.Secure.getString(cr, Settings.Secure.ANDROID_ID);
        addRow(inflater, table, "Android Id", id);

        // AirWatch id
        addRow(inflater, table, "AirWatch Id", getAirWatchId(getActivity()));

        // WiFi MAC
        WifiManager wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        if (wifi==null) return;
        WifiInfo info = wifi.getConnectionInfo();
        if (info==null) return;
        String mac = info.getMacAddress();
        if (mac==null || mac.length()==0) return;
        addRow(inflater, table, "MAC address", mac);

        // Hashed MAC
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(mac.getBytes());
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for(byte x : bytes) {
                sb.append(Integer.toHexString(x&0xff));
            }
            addRow(inflater, table, "Hashed MAC", sb.toString());

        } catch(Exception e) {}
    }

    private String formatOrientation(int value) {
        value++;
        if (value<0 || value>=orientations.length) return(null);
        return(orientations[value]);
    }

    private void addDisplayRows(LayoutInflater inflater, TableLayout table) {
        Activity activity = getActivity();
        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        addRow(inflater, table, "Display width", Integer.toString(metrics.widthPixels));
        addRow(inflater, table, "Display height", Integer.toString(metrics.heightPixels));
        String density = formatDensity(metrics.densityDpi);
        if (density!=null) density = Integer.toString(metrics.densityDpi)+" ("+density+")";
        else density = Integer.toString(metrics.densityDpi);
        addRow(inflater, table, "Pixels per inch", density);
        addRow(inflater, table, "Refresh rate", Math.round(display.getRefreshRate())+" Hz");
        String orientation = formatOrientation(getActivity().getRequestedOrientation());
        addRow(inflater, table, "Orientation", orientation);
    }

    private void addPackageRows(LayoutInflater inflater, TableLayout table) {
        Activity activity = getActivity();
        PackageManager manager = activity.getPackageManager();
        String name = activity.getPackageName();

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
        } catch(Exception exception) {
            Crittercism.logHandledException(exception);
        }
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
        hours -= 24*days;

        // Format fields
        StringBuilder sb = new StringBuilder();
        if (days>0) {
            sb.append(days);
            sb.append("d ");
        }
        if (hours>0 || sb.length()>0) {
            sb.append(hours);
            sb.append("h ");
        }
        if (minutes>0 || sb.length()>0) {
            sb.append(minutes);
            sb.append("m ");
        }
        sb.append(seconds);
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
}
