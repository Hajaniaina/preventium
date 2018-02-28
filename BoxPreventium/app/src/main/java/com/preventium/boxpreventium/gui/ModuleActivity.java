package com.preventium.boxpreventium.gui;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.manager.StatsLastDriving;
import com.preventium.boxpreventium.server.CFG.ReaderCFGFile;
import com.preventium.boxpreventium.server.JSON.ParseJsonData;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.Connectivity;

import org.json.JSONObject;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ModuleActivity extends AppCompatActivity {

    private TextView module_avfm;
    private TextView module_vitesse;
    private TextView module_duree;
    private TextView module_note;
    private TextView module_force;
    private TextView module_ecran;
    private TextView module_carte;
    private TextView module_langue;
    private TextView module_epc;
    private TextView module_qr_code;
    private TextView module_leurre;
    private TextView module_force_mg;

    private String serveur;
    private ReaderCFGFile reader = new ReaderCFGFile();
    private String desFileName;
    private String srcFileName;

    private int opt_qrcode_web = 0;
    private int opt_carte_web = 0;
    private int opt_panneau_web = 0;
    private int opt_note_web = 0;
    private int opt_VFAM_web = 0;
    private int opt_duree_web = 0;
    private int opt_seulforce_web = 0;
    private int opt_config_type_web = 0;
    private int opt_langue_web = 0;
    private int opt_screen_size_web = 0;
    private int opt_force_mg_web = 0;
    private int opt_leurre_web = 0;


    private boolean cfgi;
    private Handler handler;
    private Runnable handlerTask;
    private boolean run = false;

    private static Timer timer = new Timer();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // onBackPressed();
                String message = "close";
                Intent intent = new Intent();
                intent.putExtra("MESSAGE", message);
                setResult(0, intent);
                finish();//finishing activity
                break;
        }
        return true;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_activity);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //String module_dr_code = sharedPref.getString(getString(R.string.driver_id_key), "1");

        // module mesure
        module_avfm = (TextView) findViewById(R.id.module_avfm);
        module_vitesse = (TextView) findViewById(R.id.module_vitesse);
        module_duree = (TextView) findViewById(R.id.module_duree);
        module_note = (TextView) findViewById(R.id.module_note);
        module_force = (TextView) findViewById(R.id.module_force);

        // module affichage
        module_ecran = (TextView) findViewById(R.id.module_ecran);
        module_carte = (TextView) findViewById(R.id.module_carte);
        module_langue = (TextView) findViewById(R.id.module_langue);

        // module bouton
        module_epc = (TextView) findViewById(R.id.module_epc);
        module_qr_code = (TextView) findViewById(R.id.module_qr_code);
        module_leurre = (TextView) findViewById(R.id.module_leurre);
        module_force_mg = (TextView) findViewById(R.id.module_force_mg);
    }

    @Override
    protected void onResume () {
        repeatAsync();

        super.onResume();
    }

    private boolean Is = false;
    private void repeatAsync () {
        final Handler handler = new Handler();
        timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                    try {

                        boolean activeopt = Connectivity.isConnected(getApplicationContext());
                        Log.e("connect za : ", String.valueOf(Connectivity.isConnected(getApplicationContext())));

                        if (activeopt) {

                            srcFileName = ComonUtils.getIMEInumber(getApplicationContext()) + ".CFG";
                            desFileName = String.format(Locale.getDefault(), "%s/%s", getApplicationContext().getFilesDir(), srcFileName);
                            cfgi = reader.read(desFileName);

                            if(cfgi){

                                serveur = reader.getServerUrl();
                                if(serveur != ""){
                                    if( !run && !Is ) {
                                        new JsonParse().execute();
                                        Is = true;
                                    }
                                    if(run) {
                                        UpdateUI();
                                        timer.cancel();
                                        timer.purge();
                                        timer = null;
                                        run = false;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
                });
            }
        };

        timer.schedule(task, 0, 1*1000);  // interval of one minute (1 sec)
    }

    private void UpdateUI () {

        AppColor color = new AppColor(ModuleActivity.this);
        int GREEN = color.getColor(4);
        int RED = color.getColor(0);

        if (opt_qrcode_web != 0) {
            module_qr_code.setText(R.string.enabled_string);
            module_qr_code.setTextColor(GREEN);
        } else {
            module_qr_code.setText(R.string.disabled_string);
            module_qr_code.setTextColor(RED);
        }

        if (opt_carte_web != 0) {
            module_carte.setText(R.string.enabled_string);
            module_carte.setTextColor(GREEN);
        } else {
            module_carte.setText(R.string.disabled_string);
            module_carte.setTextColor(RED);
        }
        module_carte.setVisibility(View.VISIBLE);

        Log.e("module_carte", String.valueOf(module_carte.getText()));

        if (opt_panneau_web != 0) {
            module_vitesse.setText(R.string.enabled_string);
            module_vitesse.setTextColor(GREEN);
        } else {
            module_vitesse.setText(R.string.disabled_string);
            module_vitesse.setTextColor(RED);
        }

        if (opt_note_web != 0) {
            module_note.setText(R.string.enabled_string);
            module_note.setTextColor(GREEN);
        } else {
            module_note.setText(R.string.disabled_string);
            module_note.setTextColor(RED);
        }

        if (opt_VFAM_web != 0) {
            module_avfm.setText(R.string.enabled_string);
            module_avfm.setTextColor(GREEN);
        } else {
            module_avfm.setText(R.string.disabled_string);
            module_avfm.setTextColor(RED);
        }

        if (opt_seulforce_web != 0) {
            module_force.setText(R.string.enabled_string);
            module_force.setTextColor(GREEN);
        } else {
            module_force.setText(R.string.disabled_string);
            module_force.setTextColor(RED);
        }

        if (opt_langue_web != 0) {
            module_langue.setText(R.string.enabled_string);
            module_langue.setTextColor(GREEN);
        } else {
            module_langue.setText(R.string.disabled_string);
            module_langue.setTextColor(RED);
        }

        if (opt_duree_web != 0) {
            module_duree.setText(R.string.enabled_string);
            module_duree.setTextColor(GREEN);
        } else {
            module_duree.setText(R.string.disabled_string);
            module_duree.setTextColor(RED);
        }

        if (opt_screen_size_web == 4) {
            module_ecran.setText(R.string.module_simple_string);
            module_ecran.setTextColor(RED);
        } else {
            module_ecran.setText(R.string.module_complete_string);
            module_ecran.setTextColor(GREEN);
        }

        if (opt_seulforce_web != 0) {
            module_epc.setText(R.string.enabled_string);
            module_epc.setTextColor(GREEN);
        } else {
            module_epc.setText(R.string.disabled_string);
            module_epc.setTextColor(RED);
        }

        if (opt_force_mg_web != 0) {
            module_force_mg.setText(R.string.enabled_string);
            module_force_mg.setTextColor(GREEN);
        } else {
            module_force_mg.setText(R.string.disabled_string);
            module_force_mg.setTextColor(RED);
        }

        if (opt_leurre_web != 0) {
            module_leurre.setText(R.string.enabled_string);
            module_leurre.setTextColor(GREEN);
        } else {
            module_leurre.setText(R.string.disabled_string);
            module_leurre.setTextColor(RED);
        }
    }

    class JsonParse extends AsyncTask<Void, Void, Void>
    {
        ProgressDialog pdLoading = new ProgressDialog(ModuleActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage(getString(R.string.loading_string));
            pdLoading.setCancelable(false);
            pdLoading.show();
        }
        @Override
        protected Void doInBackground(Void... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here

            srcFileName = ComonUtils.getIMEInumber(getApplicationContext()) + ".CFG";
            desFileName = String.format(Locale.getDefault(), "%s/%s", getApplicationContext().getFilesDir(), srcFileName);
            cfgi = reader.read(desFileName);

            serveur = reader.getServerUrl();
            String imei = StatsLastDriving.getIMEI(ModuleActivity.this);
            ParseJsonData jsonData = new ParseJsonData();
            String jsonString1 = jsonData.makeServiceCall(serveur + "/index.php/get_config/" + imei);

            if ( jsonString1 != null ) {

                try {
                    JSONObject conf = new JSONObject(jsonString1);
                    JSONObject config = conf.getJSONObject("config");

                    opt_qrcode_web = Integer.parseInt(config.optString("qrcode"));
                    opt_carte_web = Integer.parseInt(config.optString("affiche_carte"));
                    opt_panneau_web = Integer.parseInt(config.optString("paneau_vitesse_droite"));
                    opt_note_web = Integer.parseInt(config.optString("note_sur_20"));
                    opt_VFAM_web = Integer.parseInt(config.optString("VFAM"));
                    opt_duree_web = Integer.parseInt(config.optString("duree"));
                    opt_seulforce_web = Integer.parseInt(config.optString("seulforce"));
                    opt_config_type_web = Integer.parseInt(config.optString("config_type"));
                    opt_langue_web = Integer.parseInt(config.optString("langue"));
                    opt_screen_size_web = Integer.parseInt(config.optString("taille_ecran"));
                    opt_force_mg_web = Integer.parseInt(config.optString("force_mg"));
                    opt_leurre_web = Integer.parseInt(config.optString("leurre"));

                    run = true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pdLoading.dismiss();
        }
    }
}
