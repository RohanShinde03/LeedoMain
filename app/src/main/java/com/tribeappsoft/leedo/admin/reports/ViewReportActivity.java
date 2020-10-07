package com.tribeappsoft.leedo.admin.reports;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.tribeappsoft.leedo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewReportActivity extends AppCompatActivity {

    @BindView(R.id.wv_leado_report_webview)
    WebView webView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);
        ButterKnife.bind(this);

      //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        loadLocalPage();
    }

    public void loadLocalPage() {
        WebSettings webSetting = webView.getSettings();
        webSetting.setBuiltInZoomControls(false);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://upstox.com/open-demat-account/?utm_expid=.D0HWlQuTRcWjQzMM2f2bcQ.0&utm_referrer=https%3A%2F%2Fupstox.com%2F");
    }

    public class WebViewClient extends android.webkit.WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub

            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}