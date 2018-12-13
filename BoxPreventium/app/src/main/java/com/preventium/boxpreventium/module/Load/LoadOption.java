package com.preventium.boxpreventium.module.Load;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.preventium.boxpreventium.gui.MainActivity;
import com.preventium.boxpreventium.module.HandlerBox;
import com.preventium.boxpreventium.server.JSON.ParseJsonData;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.Connectivity;
import com.preventium.boxpreventium.utils.DataLocal;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by tog on 06/11/2018.
 */

public class LoadOption {

    // variable
    private Context context;
    private MainActivity main;

    // constructor
    public LoadOption (Context context) {
        this.context = context;
        main = getMain(context);
    }

    private MainActivity getMain(Context context) {
        return (MainActivity) context;
    }

    public void getOption (String server) {
        new Option(this, context).execute(server);
    }

    @SuppressLint("StaticFieldLeak")
    public static class Option extends AsyncTask<String, String, Boolean> {

        private WeakReference<Context> context;
        private LoadOption opt;
        public Option (LoadOption opt, Context context) {
            this.context = new WeakReference<Context>(context);
            this.opt = opt;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... param) {
            Context context = this.context.get();
            if( context != null )
            {
                DataLocal local = DataLocal.get(context);
                String imei = ComonUtils.getIMEInumber(context);
                ParseJsonData jsonData = new ParseJsonData();
                String server = param[0];

                boolean connected = Connectivity.isConnected(context);
                if( connected ) {
                    String json = jsonData.makeServiceCall(server + "/index.php/get_config/" + imei);

                    if( json != null && json.toString().length() > 0) {
                        try {
                            JSONObject conf = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                            JSONObject config = conf.getJSONObject("config");

                            int opt_qrcode_web = Integer.parseInt(config.optString("qrcode"));
                            int opt_carte_web = Integer.parseInt(config.optString("affiche_carte"));
                            int opt_panneau_web = Integer.parseInt(config.optString("paneau_vitesse_droite"));
                            int opt_note_web = Integer.parseInt(config.optString("note_sur_20"));
                            int opt_VFAM_web = Integer.parseInt(config.optString("VFAM"));
                            int opt_duree_web = Integer.parseInt(config.optString("duree"));
                            int opt_seulforce_web = Integer.parseInt(config.optString("seulforce"));
                            int opt_config_type_web = Integer.parseInt(config.optString("config_type"));
                            int opt_langue_web = Integer.parseInt(config.optString("langue"));
                            int opt_screen_size_web = Integer.parseInt(config.optString("taille_ecran"));
                            int opt_force_mg_web = Integer.parseInt(config.optString("force_mg"));
                            int opt_leurre = Integer.parseInt(config.optString("leurre"));
                            int opt_sonore = Integer.parseInt(config.optString("voix"));
                            int opt_button_parcours = Integer.parseInt(config.optString("btn_menu"));
                            int opt_triangle = Integer.parseInt(config.optString("triangle"));
                            int opt_timer = Integer.parseInt(config.optString("timer"));
                            int opt_relance = Integer.parseInt(config.optString("relance"));

                            local.setValue("options_fonc", true);
                            local.setValue("qrcode", opt_qrcode_web);
                            local.setValue("affiche_carte", opt_carte_web);
                            local.setValue("paneau_vitesse_droite", opt_panneau_web);
                            local.setValue("note_sur_20", opt_note_web);
                            local.setValue("VFAM", opt_VFAM_web);
                            local.setValue("duree", opt_duree_web);
                            local.setValue("seulforce", opt_seulforce_web);
                            local.setValue("config_type", opt_config_type_web);
                            local.setValue("langue", opt_langue_web);
                            local.setValue("taille_ecran", opt_screen_size_web);
                            local.setValue("force_mg", opt_force_mg_web);
                            local.setValue("leurre", opt_leurre);
                            local.setValue("voix", opt_sonore);
                            local.setValue("btn_menu", opt_button_parcours);
                            local.setValue("triangle", opt_triangle);
                            local.setValue("timer", opt_timer == 0 ? 1 : opt_timer);
                            local.setValue("relance", opt_relance);
                            local.setValue("workTime", 8);
                            local.setValue("formateur", ComonUtils.is_tablet(context) ? 1 : 0);
                            local.apply();

                            // leurre active
                            new HandlerBox(context).set_active_from_serveur(opt_leurre);

                            return true;
                        } catch (JSONException e) {
                        }
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            // manage option
            opt.main.manageOption();
        }
    }
}