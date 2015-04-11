package com.staples.mobile.cfa;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.crittercism.app.Crittercism;

import com.staples.mobile.common.access.config.StaplesAppContext;
import com.staples.mobile.common.access.configurator.model.Force;
import com.staples.mobile.common.access.configurator.model.Suggest;
import com.staples.mobile.common.access.configurator.model.Update;

public class UpgradeManager {

    private static final String TAG = "UpgradeManager";

    private static final boolean LOGGING = false;

    public enum UPGRADE_STATUS {
        RAW,
        NO_UPGRADE_INFO,
        NOT_NECESSARY,
        SUGGEST_UPGRADE,
        FORCE_UPGRADE,
        EXCEPTION,
    };

    private Context context;
    private UPGRADE_STATUS upgradeStatus;
    private Update update;
    private Force force;
    private Suggest suggest;
    private String upgradeMsg;
    private String upgradeUrl;

    public UpgradeManager(Context context) {
        if (LOGGING) {
            Log.v(TAG, "UpgradeManager:UpgradeManager():");
        }
        this.context = context;
    }

    public String getUpgradeMsg() {
        if (LOGGING) {
            Log.v(TAG, "UpgradeManager:getUpgradeMsg():"
                    + " upgradeMsg[" + upgradeMsg + "]");
        }
        return (upgradeMsg);
    }

    public String getUpgradeUrl() {
        if (LOGGING) {
            Log.v(TAG, "UpgradeManager:getUpgradeUrl():"
                    + " upgradeUrl[" + upgradeUrl + "]");
        }
        return (upgradeUrl);
    }

    private void launchUpgrade(Context context, String upgradeUrl) {

        if (LOGGING) {
            Log.v(TAG, "UpgradeManager:launchUpgrade(): Entry.");
        }
        Uri uriUrl = Uri.parse(upgradeUrl);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        context.startActivity(launchBrowser);
    }

    public UPGRADE_STATUS getUpgradeStatus() {

        if (LOGGING) {
            Log.v(TAG, "UpgradeManager:getUpgradeStatus(): Entry.");
        }

        upgradeStatus = UPGRADE_STATUS.NOT_NECESSARY;

        StaplesAppContext staplesAppContext = StaplesAppContext.getInstance();
        update = staplesAppContext.getUpdate();

        int appVersionCode = 0;
        String thresholdForceStr = "";
        String thresholdSuggestStr = "";

        while (true) {

            if (update == null) {
                upgradeStatus = UPGRADE_STATUS.NO_UPGRADE_INFO;
                break; // while (true)
            }

            // App Version

            PackageInfo packageInfo = null;

            try {

                PackageManager packageManager = context.getPackageManager();
                String packageName = context.getPackageName();
                packageInfo = packageManager.getPackageInfo(packageName, 0);
                appVersionCode = packageInfo.versionCode;

            } catch (PackageManager.NameNotFoundException nameNotFoundException) {

                Crittercism.logHandledException(nameNotFoundException);
                upgradeStatus = UPGRADE_STATUS.EXCEPTION;
                break; // while (true)
            }

            // Check Force Upgrade

            force = update.getForce();
            thresholdForceStr = force.getThreshold();
            // @TODO Remove line below once MCS dev and prod Update section changed.
            // thresholdForceStr = "9";
            int thresholdForceInt = 0;

            try {

                thresholdForceInt = Integer.parseInt(thresholdForceStr);

            } catch (NumberFormatException numberFormatException) {

                Crittercism.logHandledException(numberFormatException);
                upgradeStatus = UPGRADE_STATUS.EXCEPTION;
                break; // while (true)
            }

            if (appVersionCode < thresholdForceInt) {
                upgradeStatus = UPGRADE_STATUS.FORCE_UPGRADE;
                upgradeMsg = force.getMessage();
                upgradeUrl = update.getUrl();
                upgradeUrl = "https://play.google.com/store/apps/details?id=app.staples&hl=en";
                break; // while (true)
            }

            // Check Suggest Upgrade

            suggest = update.getSuggest();
            thresholdSuggestStr = suggest.getThreshold();
            // @TODO Remove line below once MCS dev and prod Update section changed.
            // thresholdSuggestStr = "8";
            int thresholdSuggestInt = 0;

            try {

                thresholdSuggestInt = Integer.parseInt(thresholdSuggestStr);

            } catch (NumberFormatException numberFormatException) {

                Crittercism.logHandledException(numberFormatException);
                upgradeStatus = UPGRADE_STATUS.EXCEPTION;
                break; // while (true)
            }

            if (appVersionCode < thresholdSuggestInt) {
                upgradeStatus = UPGRADE_STATUS.SUGGEST_UPGRADE;
                upgradeMsg = suggest.getMessage();
                upgradeUrl = update.getUrl();
                upgradeUrl = "https://play.google.com/store/apps/details?id=app.staples&hl=en";
            }

            break; // while (true)

        } // while (true)

        if (LOGGING) {
            Log.v(TAG, "UpgradeManager:getUpgradeStatus():"
                    + " appVersionCode[" + appVersionCode + "]"
                    + " thresholdForceStr[" + thresholdForceStr + "]"
                    + " thresholdSuggestStr[" + thresholdSuggestStr + "]"
                    + " upgradeStatus[" + upgradeStatus + "]"
                    + " upgradeMsg[" + upgradeMsg + "]"
                    + " upgradeUrl[" + upgradeUrl + "]");
        }

        return (upgradeStatus);
    }
}
