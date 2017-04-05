package com.preventium.boxpreventium.gui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import com.github.clans.fab.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.location.CustomMarkerData;

import java.io.IOException;
import java.util.ArrayList;

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";
    public static final String MARKER_DATA_PARAM = "MARKER_DATA";

    private WebView webView;
    private TextView stepsView;
    private CustomMarkerData markerData;
    private int maxPages = 0;
    private int currPageIndex = 0;
    private int pageLoadProgress = 0;
    private int pageReloadAttempts = 0;

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        markerData = getIntent().getParcelableExtra(MARKER_DATA_PARAM);

        ///////////////////////////////////////////////////////////////

        ArrayList<String> urlList = new ArrayList<>();

        urlList.add("http://www.orimi.com/pdf-test.pdf");
        urlList.add("http://www.hubharp.com/web_sound/HarrisLilliburleroShort.mp3");
        urlList.add("https://www.w3schools.com/css/trolltunga.jpg");
        urlList.add("https://allthingsaudio.wikispaces.com/file/view/Out%20on%20the%20road.mp3/139234925/Out%20on%20the%20road.mp3");
        urlList.add("https://www.youtube.com/");

        markerData.alertAttachments = urlList;

        ///////////////////////////////////////////////////////////////

        final AppColor appColor = new AppColor(this);

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(true);

        final ProgressDialog progressBar = new ProgressDialog(this, R.style.InfoDialogStyle);
        // progressBar.setMessage(getString(R.string.progress_loading_string));
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setCancelable(false);
        progressBar.setMax(100);

        stepsView = (TextView) findViewById(R.id.step_num);

        if (markerData.alertAttachments != null) {

            maxPages = markerData.alertAttachments.size();
        }

        updateStepView();

        if (!isFinishing()) {

            progressBar.show();
        }

        webView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged (WebView view, int progress) {

                pageLoadProgress = progress;

                if (progress >= 100) {

                    progressBar.dismiss();
                }
                else {

                    if (!progressBar.isShowing()) {

                        progressBar.show();
                    }

                    progressBar.setProgress(progress);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished (WebView view, String url) {

                if (url.endsWith(".pdf")) {

                    if (pageLoadProgress < 100) {

                        if (pageReloadAttempts < 3) {

                            pageReloadAttempts++;
                            view.clearCache(true);
                            view.reload();
                        }
                    }
                }

                super.onPageFinished(view, url);
            }
        });

        webView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart (String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        openPage(currPageIndex);

        final FloatingActionButton buttonNext = (FloatingActionButton) findViewById(R.id.fab_file_check);
        buttonNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                if (currPageIndex < (maxPages - 1)) {

                    currPageIndex++;
                    pageReloadAttempts = 0;

                    openPage(currPageIndex);
                    updateStepView();
                }
                else {

                    onBackPressed();
                }

                if (currPageIndex >= (maxPages - 1)) {

                    buttonNext.setImageIcon(Icon.createWithResource(WebViewActivity.this, R.drawable.ic_check));
                    buttonNext.setColorNormal(appColor.getColor(AppColor.GREEN));
                }
            }
        });

        final FloatingActionButton buttonRefresh = (FloatingActionButton) findViewById(R.id.fab_page_refresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                pageReloadAttempts = 0;
                webView.clearCache(true);
                webView.reload();
            }
        });
    }

    @Override
    protected void onPause() {

        webView.stopLoading();
        webView.clearCache(true);
        webView.setVisibility(View.GONE);
        webView.destroy();

        super.onPause();
    }

    public void openPage (int pageIndex) {

        String url = markerData.alertAttachments.get(currPageIndex);

        if (url.endsWith(".pdf")) {

            String temp = url;
            url = "https://docs.google.com/gview?embedded=true&url=" + temp;
        }

        try {

            webView.loadUrl(url);
        }
        catch (Exception e) {

            Log.d(TAG, "Page Load Exception: " + e.toString());
        }
    }

    public void updateStepView() {

        String text = (currPageIndex + 1) + "/" + maxPages;
        stepsView.setText(text);
    }
}
