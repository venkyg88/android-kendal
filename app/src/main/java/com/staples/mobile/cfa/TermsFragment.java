package com.staples.mobile.cfa;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.crittercism.app.Crittercism;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.analytics.Tracker;

public class TermsFragment extends Fragment {
    private static final String TAG = TermsFragment.class.getSimpleName();
    private static final String TERMS_URL = "http://m.staples.com/skmobwidget/sbd/content/help-center/policies-and-legal.html";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("TermsFragment:onCreateView(): Displaying the Privacy & Terms screen.");

        View view = inflater.inflate(R.layout.terms_fragment, container, false);
        WebView termsWebView = (WebView) view.findViewById(R.id.terms_view);

        termsWebView.getSettings().setJavaScriptEnabled(true);
        termsWebView.getSettings().setLoadWithOverviewMode(true);
        termsWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        termsWebView.setScrollbarFadingEnabled(true);

        termsWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Crittercism.logHandledException(new Exception());
                Log.d(TAG, "Loading WebViewClient Error!");
            }
        });

        termsWebView.loadUrl(TERMS_URL);

        termsWebView.requestFocus();

        return view;
    }

    @Override
     public void onResume() {
        super.onResume();
        Tracker.getInstance().trackStateForAbout(); // analytics
        ActionBar.getInstance().setConfig(ActionBar.Config.TERMS);
    }
}
