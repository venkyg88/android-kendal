package app.staples.mobile.cfa;

import android.os.StrictMode;

public class DebugUtil {
    public static void setStrictMode() {
        StrictMode.enableDefaults();

        // Thread Policy

        StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder();
        threadPolicyBuilder.detectAll();
        threadPolicyBuilder.penaltyLog();
        StrictMode.ThreadPolicy threadPolicy = threadPolicyBuilder.build();

        StrictMode.setThreadPolicy(threadPolicy);

        // VM Policy

        StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder();
        vmPolicyBuilder.detectAll();
        vmPolicyBuilder.detectActivityLeaks();
        vmPolicyBuilder.detectLeakedClosableObjects();
        vmPolicyBuilder.detectLeakedSqlLiteObjects();
        vmPolicyBuilder.penaltyLog();
        StrictMode.VmPolicy vmPolicy = vmPolicyBuilder.build();

        StrictMode.setVmPolicy(vmPolicy);
    }
}
