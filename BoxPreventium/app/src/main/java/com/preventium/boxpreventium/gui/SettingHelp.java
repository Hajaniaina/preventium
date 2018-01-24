package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.preventium.boxpreventium.R;

public class SettingHelp extends Activity {
    private OnClickListener help_on_line = new C00672();
    private String htmlText1;
    private String htmlText2;
    private String htmlText3;
    private String link = "www.preventium.fr";
    private int ref;

    class C00661 implements OnClickListener {
        C00661() {
        }

        public void onClick(View view) {
            SettingHelp.this.onBackPressed();
        }
    }

    class C00672 implements OnClickListener {
        C00672() {
        }

        public void onClick(View v) {
            SettingHelp.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://www.preventium.fr")));
        }
    }

    private class ImageGetter implements android.text.Html.ImageGetter {
        private ImageGetter() {
        }

        public Drawable getDrawable(String source) {
            int id;
            if (source.equals("home.jpg")) {
                id = R.drawable.ic_accueil;
            } else if (source.equals("buttons1.jpg")) {
                id = R.drawable.ic_buttons1;
            } else if (!source.equals("buttons2.jpg")) {
                return null;
            } else {
                id = R.drawable.ic_buttons2;
            }
            Drawable d = SettingHelp.this.getResources().getDrawable(id);
            d.setBounds(0, 0, 600, 337);
            return d;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_help);
        this.ref = getIntent().getIntExtra("help", 0);
        this.htmlText1 = "<body><h2>" + getResources().getString(R.string.setting_string) + "</h2>" + "<img src=\"home.jpg\"><br><br>" + "1. " + getResources().getString(R.string.corners_string) + "<br>" + "2. " + getResources().getString(R.string.brakes_string) + "<br>" + "3. " + getResources().getString(R.string.acc_string) + "<br>" + "4. " + getResources().getString(R.string.avg_string) + "<br>" + "5. " + getResources().getString(R.string.time_elapsed_string) + "<br>" + "6. " + getResources().getString(R.string.speed_line_string) + "<br>" + "7. " + getResources().getString(R.string.speed_corner_string) + "<br>" + "8. " + getResources().getString(R.string.app_lang_string) + "<br>" + "9. " + getResources().getString(R.string.avg_score_string) + "<br>" + "10. " + getResources().getString(R.string.display_force_string) + "<br>" + "11. " + getResources().getString(R.string.message_string) + "<br>" + "</body>";
        this.htmlText2 = "<body><h2>" + getResources().getString(R.string.buttons_string) + "</h2>" + "<img src=\"buttons1.jpg\"><br><br>" + "1. <br>" + "2. <br>" + "3. <br>" + "4. <br><br>" + "<img src=\"buttons2.jpg\"><br><br>" + "5. <br>" + "6. <br>" + "7. <br>" + "8. <br>" + "</body>";
        this.htmlText3 = "<body><h2>" + getResources().getString(R.string.on_line_string) + "</h2>" + "</body>";
        TextView htmlTextView = (TextView) findViewById(R.id.html_text);
        if (this.ref == 1) {
            htmlTextView.setText(Html.fromHtml(this.htmlText1, new ImageGetter(), null));
        } else if (this.ref == 2) {
            htmlTextView.setText(Html.fromHtml(this.htmlText2, new ImageGetter(), null));
        } else {
            htmlTextView.setText(Html.fromHtml(this.htmlText3, new ImageGetter(), null));
            TextView helpOnLine = (TextView) findViewById(R.id.help_on_line);
            SpannableString spanString = new SpannableString(this.link);
            spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
            helpOnLine.setText(spanString);
            helpOnLine.setOnClickListener(this.help_on_line);
        }
        ((FloatingActionButton) findViewById(R.id.back)).setOnClickListener(new C00661());
    }

    protected void onResume() {
        //ComonUtils.changeLanguage(this);
        super.onResume();
    }

    protected void onPause() {
        //ComonUtils.changeLanguage(this);
        super.onPause();
    }
}
