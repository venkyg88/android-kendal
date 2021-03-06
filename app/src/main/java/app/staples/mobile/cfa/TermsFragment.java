package app.staples.mobile.cfa;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.analytics.Tracker;

import app.staples.R;
import app.staples.mobile.cfa.widget.ActionBar;

public class TermsFragment extends Fragment {
    private static final String TAG = TermsFragment.class.getSimpleName();
    private static final String TERMS_URL = "file:///android_asset/Terms.html";
    private LinearLayout loadingSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("TermsFragment:onCreateView(): Displaying the Privacy & Terms screen.");

        View view = inflater.inflate(R.layout.terms_fragment, container, false);
        view.setTag(this);

        WebView termsWebView = (WebView) view.findViewById(R.id.terms_view);
        termsWebView.getSettings().setJavaScriptEnabled(true);
        termsWebView.getSettings().setLoadWithOverviewMode(true);
        termsWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        termsWebView.setScrollbarFadingEnabled(true);

        termsWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Crittercism.logHandledException(new Exception());
                Log.d(TAG, "Loading WebViewClient Error! Error code:" + errorCode +
                        ", Reason:" + description);
            }
        });

        loadingSpinner = (LinearLayout) view.findViewById(R.id.loading_spinner);

        termsWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                loadingSpinner.setVisibility(View.GONE);
            }
        });

        termsWebView.loadUrl(TERMS_URL);

        return view;
    }

    @Override
     public void onResume() {
        super.onResume();
        Tracker.getInstance().trackStateForPrivacy(); // analytics
        ActionBar.getInstance().setConfig(ActionBar.Config.TERMS);
    }
}
