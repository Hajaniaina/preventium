package com.preventium.boxpreventium.gui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import com.github.clans.fab.FloatingActionButton;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        markerData = getIntent().getParcelableExtra(MARKER_DATA_PARAM);

        ///////////////////////////////////////////////////////////////

        ArrayList<String> urlList = new ArrayList<>();

        urlList.add("http://www.orimi.com/pdf-test.pdf");
        urlList.add("http://techslides.com/demos/sample-videos/small.mp4");
        urlList.add("https://allthingsaudio.wikispaces.com/file/view/Out%20on%20the%20road.mp3/139234925/Out%20on%20the%20road.mp3");
        urlList.add("https://www.w3schools.com/css/trolltunga.jpg");
        urlList.add("https://www.ikalogic.com/");

        markerData.alertAttachments = urlList;

        ///////////////////////////////////////////////////////////////

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        final AppColor appColor = new AppColor(getApplicationContext());

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(true);

        final ProgressDialog progressBar = new ProgressDialog(this, R.style.InfoDialogStyle);
        progressBar.setMessage(getString(R.string.progress_loading_string));
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setCancelable(false);
        progressBar.setMax(100);

        stepsView = (TextView) findViewById(R.id.step_num);

        if (markerData.alertAttachments != null) {

            maxPages = markerData.alertAttachments.size();
        }

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

                        if (pageReloadAttempts < 5) {

                            pageReloadAttempts++;
                            webView.clearCache(true);
                            openPage(currPageIndex);
                        }
                    }
                }

                super.onPageFinished(view, url);
            }
        });

        openPage(currPageIndex);
        updateStepView();

        final FloatingActionButton buttonNext = (FloatingActionButton) findViewById(R.id.fab_page_next);
        final FloatingActionButton buttonPrev = (FloatingActionButton) findViewById(R.id.fab_page_prev);
        buttonPrev.setEnabled(false);

        buttonNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                if (currPageIndex < (maxPages - 1)) {

                    buttonPrev.setEnabled(true);

                    currPageIndex++;
                    pageReloadAttempts = 0;
                    openPage(currPageIndex);
                    updateStepView();
                }
                else {

                    quit();
                }

                if (currPageIndex >= (maxPages - 1)) {

                    buttonNext.setImageIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_check));
                    buttonNext.setColorNormal(appColor.getColor(AppColor.GREEN));
                }
            }
        });

        buttonPrev.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                if (currPageIndex > 0) {

                    currPageIndex--;
                    pageReloadAttempts = 0;
                    openPage(currPageIndex);
                    updateStepView();

                    if (buttonNext.getColorNormal() == appColor.getColor(AppColor.GREEN)) {

                        buttonNext.setImageIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_chevron_right));
                        buttonNext.setColorNormal(appColor.getColor(AppColor.BLUE));
                    }

                    if (currPageIndex == 0) {

                        buttonPrev.setEnabled(false);
                    }
                }
            }
        });

        final FloatingActionButton buttonRefresh = (FloatingActionButton) findViewById(R.id.fab_page_refresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                pageReloadAttempts = 0;
                webView.clearCache(true);
                openPage(currPageIndex);
                updateStepView();
            }
        });
    }

    @Override
    public void onBackPressed() {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setCancelable(false);
        alertDialog.setTitle("");
        alertDialog.setMessage("Vous n'avez pas fini la lecture des documents. Êtes-vous sûr de vouloir quitter?");

        alertDialog.setPositiveButton(getString(R.string.quit_string), new DialogInterface.OnClickListener() {

            public void onClick (DialogInterface dialog, int which) {

                quit();
                dialog.dismiss();
            }
        });

        alertDialog.setNegativeButton(getString(R.string.cancel_string), new DialogInterface.OnClickListener() {

            public void onClick (DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    @Override
    protected void onPause() {

        webView.stopLoading();
        webView.clearCache(true);
        webView.setVisibility(View.GONE);
        webView.destroy();

        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;

        super.onPause();
    }

    public void openPage (int pageIndex) {

        String url = markerData.alertAttachments.get(pageIndex);

        mediaPlayer.stop();
        mediaPlayer.reset();

        if (url.endsWith(".pdf")) {

            String temp = url;
            url = "https://docs.google.com/gview?embedded=true&url=" + temp;
        }

        if (isAudioStream(url)) {

            try {

                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare();
            }
            catch (IOException e) {

                Log.d(TAG, "Media Player Exception: " + e.toString());
            }

            String page = "<html><body><center><img src=\"http://gurmeet.net/Images/index_page/happy-songs.png\" alt=\"Message Audio\"></center></body></html>";
            webView.loadData(page, "text/html", "UTF-8");

            mediaPlayer.start();
            return;
        }

        try {

            webView.loadUrl("about:blank");
            webView.loadUrl(url);
        }
        catch (Exception e) {

            Log.d(TAG, "Page Load Exception: " + e.toString());
        }
    }

    private void updateStepView() {

        String text = (currPageIndex + 1) + "/" + maxPages;
        stepsView.setText(text);
    }

    private boolean isAudioStream (String url) {

        if (url.endsWith(".mp3") ||
            url.endsWith(".wav") ||
            url.endsWith(".3gp") ||
            url.endsWith(".ogg") ||
            url.endsWith(".flac")) {

            return true;
        }

        return false;
    }

    private void quit() {

        super.onBackPressed();
    }
}
