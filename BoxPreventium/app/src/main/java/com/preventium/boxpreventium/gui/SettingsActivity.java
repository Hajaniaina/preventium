package com.preventium.boxpreventium.gui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.pavelsikun.seekbarpreference.SeekBarPreference;
import com.preventium.boxpreventium.R;

import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String APPPREFERENCES = "AppPrefs" ;
    private static final String TAG = "SettingsActivity";
    private static ProgressDialog progress;
    private static int forceEnabled;
    private static int mapEnabled;


    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            String stringValue = value.toString();

            if (preference instanceof ListPreference) {

                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            }
            else {

                preference.setSummary(stringValue);
            }

            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {

        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        try {

            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
        }
        catch (Exception ex) {}
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {

           actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onIsMultiPane() {

        return false;
    }

    @Override
    public void onBuildHeaders (List<Header> target) {

        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment (String fragmentName) {

        return true;
    }

    private static void updatePhoneNumbersList (PreferenceFragment fragment, int key) {

        SharedPreferences sharedPref = fragment.getPreferenceScreen().getSharedPreferences();
        String[] realNumbers = new String[5];

        realNumbers[0] = sharedPref.getString(fragment.getString(R.string.phone_number_1_key), "");
        realNumbers[1] = sharedPref.getString(fragment.getString(R.string.phone_number_2_key), "");
        realNumbers[2] = sharedPref.getString(fragment.getString(R.string.phone_number_3_key), "");
        realNumbers[3] = sharedPref.getString(fragment.getString(R.string.phone_number_4_key), "");
        realNumbers[4] = sharedPref.getString(fragment.getString(R.string.phone_number_5_key), "");

        ListPreference listPref = (ListPreference) fragment.findPreference(fragment.getString(key));
        CharSequence[] numbers = listPref.getEntries();

        for (int i = 0; i <= 5; i++) {

            if (i > 0) {

                if (realNumbers[i - 1].isEmpty()) {

                    String str = numbers[i].toString();
                    str += " [" + fragment.getString(R.string.empty_string) + "]";
                    numbers[i] = str;
                }
                else {

                    String str = numbers[i].toString();
                    str += " [" + realNumbers[i - 1] + "]";
                    numbers[i] = str;
                }
            }
        }

        listPref.setEntries(numbers);
        listPref.setSummary(listPref.getEntry());
    }

    /**********************************************************************************************/

    public static class RoutePreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate (Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_route);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_pause_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_pause_timeout_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pause_trigger_time_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.stop_trigger_time_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.recommended_speed_time_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.parcours_type_name_key)));

            updatePhoneNumbersList(this, R.string.phone_select_sms_pause_key);

            final SeekBarPreference pauseTimeoutPref = (SeekBarPreference) findPreference(getString(R.string.pause_trigger_time_key));
            final SeekBarPreference stopTimeoutPref = (SeekBarPreference) findPreference(getString(R.string.stop_trigger_time_key));

            int stopTimeoutCurrent = stopTimeoutPref.getCurrentValue();
            int stopTimeoutMin = (pauseTimeoutPref.getCurrentValue() + 1);

            stopTimeoutPref.setMinValue(stopTimeoutMin);

            if (stopTimeoutCurrent < stopTimeoutMin) {

                stopTimeoutPref.setCurrentValue(stopTimeoutMin);
            }
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item) {

            int id = item.getItemId();

            Log.d(TAG, item.getTitle().toString());

            if (id == android.R.id.home) {

                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }

    /**********************************************************************************************/

    public static class SosPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate (Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_sos);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_sos_key)));
            updatePhoneNumbersList(this, R.string.phone_select_sms_sos_key);
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item) {

            int id = item.getItemId();

            if (id == android.R.id.home) {

                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }

    /**********************************************************************************************/

    public static class ShocksPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate (Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_shocks);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_shock_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.shock_trigger_mG_key)));

            updatePhoneNumbersList(this, R.string.phone_select_sms_shock_key);
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item) {

            int id = item.getItemId();

            if (id == android.R.id.home) {

                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }

    /**********************************************************************************************/

    public static class TrackingPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate (Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_tracking);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_tracking_key)));
            updatePhoneNumbersList(this, R.string.phone_select_sms_tracking_key);
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item) {

            int id = item.getItemId();

            if (id == android.R.id.home) {

                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }

    /**********************************************************************************************/

    public static class QrScanPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate (Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_qr_scan);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.qr_select_start_mode_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.qr_select_stop_mode_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.qr_select_ic_mode_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_qr_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_qr_timeout_key)));

            updatePhoneNumbersList(this, R.string.phone_select_sms_qr_key);
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item) {

            int id = item.getItemId();

            if (id == android.R.id.home) {

                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }

    /**********************************************************************************************/

    public static class PhonePreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate (Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_phone);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_voice_key)));
            updatePhoneNumbersList(this, R.string.phone_select_voice_key);
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item) {

            int id = item.getItemId();

            if (id == android.R.id.home) {

                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }

    /**********************************************************************************************/

    public static class PinCodePreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate (Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_pin_code);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pin_code_key)));

            EditTextPreference pinCodeEdit = (EditTextPreference) findPreference(getString(R.string.pin_code_key));
            pinCodeEdit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange (Preference preference, Object newValue) {

                    if (newValue.toString().length() == 4) {

                        preference.setSummary(newValue.toString());
                        return true;
                    }
                    else {

                        Toast.makeText(getActivity(), getString(R.string.pin_short_string), Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item) {

            int id = item.getItemId();

            if (id == android.R.id.home) {

                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }

    /**********************************for language************************************************************/

    public static class LocalizationPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate (Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_localization);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.select_language_key)));

            ListPreference listPreference = (ListPreference) findPreference(getString(R.string.select_language_key));
            final PreferenceFragment fragment = this;

            Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences preferences = fragment.getActivity().getSharedPreferences(APPPREFERENCES, Context.MODE_PRIVATE);
                    String languageToLoad = newValue.toString(); // your language
                    Locale locale = new Locale(languageToLoad);
                    Locale.setDefault(locale);

                    Resources resources = fragment.getActivity().getBaseContext().getResources();

                    Configuration configuration = resources.getConfiguration();
                    configuration.locale = locale;

                    resources.updateConfiguration(configuration, resources.getDisplayMetrics());

                    SharedPreferences.Editor editor = preferences.edit();
                    //editor.putString("language", languageToLoad);
                    //editor.commit();
                    editor.putString(getString(R.string.select_language_key), languageToLoad);

                    editor.apply();

                    fragment.getActivity().startActivity(new Intent(fragment.getActivity(), MainActivity.class));
                    fragment.getActivity().finish();
                    return true;
                }
            };

            listPreference.setOnPreferenceChangeListener(listener);

        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item) {

            int id = item.getItemId();

            if (id == android.R.id.home) {

                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }


    /**********************************for status list ***********************************************************/

    public static class ModulePreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate (Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            // addPreferencesFromResource(R.xml.pref_module);
            // setHasOptionsMenu(true);

            Intent intent = new Intent(getActivity(), ModuleActivity.class);
            startActivityForResult(intent, 0);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);
            // check if the request code is same as what is passed  here it is 0
            if(requestCode == 0)
            {
                try {
                    String message = data.getStringExtra("MESSAGE");
                    if (message.equals("close")) {
                        getActivity().onBackPressed();
                    }
                }catch(NullPointerException e) {
                    getActivity().onBackPressed();
                }
            }
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item) {

            int id = item.getItemId();

            if (id == android.R.id.home) {

                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }

    /**********************************for help screen ***********************************************************/
    public static class HelpPreferenceFragment extends PreferenceFragment {
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_help);
            setHasOptionsMenu(true);
        }

        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            Intent intent;
            if (preference.getKey().equalsIgnoreCase("setting_key")) {
                intent = new Intent(getActivity(), SettingHelp.class);
                intent.putExtra("help", 1);
                startActivity(intent);
            } else if (preference.getKey().equalsIgnoreCase("buttons_key")) {
                intent = new Intent(getActivity(), SettingHelp.class);
                intent.putExtra("help", 2);
                startActivity(intent);
            } else {
                intent = new Intent(getActivity(), SettingHelp.class);
                intent.putExtra("help", 3);
                startActivity(intent);
            }
            return true;
        }
    }


    /**********************************for Force wiew***********************************************************/
  /*
    public static class ForcePreferenceFragment extends PreferenceFragment {

        class C00761 implements Preference.OnPreferenceClickListener {

            class C00741 implements DialogInterface.OnClickListener {

                class C00731 implements Runnable {
                    C00731() {
                    }

                    public void run() {
                        SettingsActivity.progress.cancel();
                    }
                }

                C00741() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    if (SettingsActivity.forceEnabled == 1) {
                        ComonUtils.SavePreferences("force", "force", 0, ForcePreferenceFragment.this.getActivity());
                    } else {
                        ComonUtils.SavePreferences("force", "force", 1, ForcePreferenceFragment.this.getActivity());
                    }
                    SettingsActivity.progress = new ProgressDialog(ForcePreferenceFragment.this.getActivity(), R.style.InfoDialogStyle);
                    SettingsActivity.progress.setMessage(ForcePreferenceFragment.this.getString(R.string.reboot_alert_string));
                    SettingsActivity.progress.setCancelable(false);
                    SettingsActivity.progress.show();
                    new Handler().postDelayed(new C00731(), 4000);
                }
            }

            class C00752 implements DialogInterface.OnClickListener {
                C00752() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }

            C00761() {
            }

            public boolean onPreferenceClick(Preference arg0) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ForcePreferenceFragment.this.getActivity());
                alertDialog.setCancelable(false);
                String actionStr = "";
                if (SettingsActivity.forceEnabled == 1) {
                    actionStr = ForcePreferenceFragment.this.getString(R.string.disable_string);
                    alertDialog.setMessage(ForcePreferenceFragment.this.getString(R.string.disable_force_message_string) + "?");
                } else {
                    actionStr = ForcePreferenceFragment.this.getString(R.string.enable_string);
                    alertDialog.setMessage(ForcePreferenceFragment.this.getString(R.string.enable_force_message_string) + "?");
                }
                alertDialog.setPositiveButton(actionStr, new C00741());
                alertDialog.setNegativeButton(R.string.cancel_string, new C00752());
                alertDialog.show();
                return false;
            }
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            addPreferencesFromResource(R.xml.pref_force);
            SettingsActivity.forceEnabled = ComonUtils.LoadPreferences("force", "force", getActivity());
            findPreference(getString(R.string.force_key)).setOnPreferenceClickListener(new C00761());
        }
    }

*/

    /**********************************for map wiew***********************************************************/
  /*  public static class MapPreferenceFragment extends PreferenceFragment {

        MainActivity mainActivity;
        class C00851 implements Preference.OnPreferenceClickListener {

            class C00801 implements DialogInterface.OnClickListener {
                C00801() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }

            class C00812 implements DialogInterface.OnClickListener {
                C00812() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }

            class C00833 implements DialogInterface.OnClickListener {

                class C00821 implements Runnable {
                    C00821() {
                    }

                    public void run() {
                        SettingsActivity.progress.cancel();
                    }
                }

                C00833() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    if (SettingsActivity.mapEnabled == 1) {
                        if (ComonUtils.LoadPreferences("map_local", "mapLocal", MapPreferenceFragment.this.getActivity()) == 0) {
                            ComonUtils.SavePreferences("map_local", "mapLocal", 1, MapPreferenceFragment.this.getActivity());
                        } else {
                            ComonUtils.SavePreferences("map_local", "mapLocal", 0, MapPreferenceFragment.this.getActivity());
                        }
                    }
                    SettingsActivity.progress = new ProgressDialog(MapPreferenceFragment.this.getActivity(), R.style.InfoDialogStyle);
                    SettingsActivity.progress.setMessage(MapPreferenceFragment.this.getString(R.string.reboot_alert_string));
                    SettingsActivity.progress.setCancelable(false);
                    SettingsActivity.progress.show();
                    new Handler().postDelayed(new C00821(), 4000);
                }
            }

            class C00844 implements DialogInterface.OnClickListener {
                C00844() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }

            C00851() {
            }

            public boolean onPreferenceClick(Preference arg0) {
                AlertDialog.Builder builder;
                if (SettingsActivity.mapEnabled == 0) {
                    builder = new AlertDialog.Builder(MapPreferenceFragment.this.getActivity(), R.style.InfoDialogStyle);
                    builder.setMessage(R.string.subscriber_string);
                    builder.setCancelable(false);
                    builder.setPositiveButton("Ok", new C00801());
                    builder.show();
                } else if (new DeviceScreen().getDeviceScreen(MapPreferenceFragment.this.getActivity()) <= 4.0d) {
                    builder = new AlertDialog.Builder(MapPreferenceFragment.this.getActivity(), R.style.InfoDialogStyle);
                    builder.setCancelable(false);
                    builder.setMessage(R.string.screen_alert_string);
                    builder.setPositiveButton("Ok", new C00812());
                    builder.show();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapPreferenceFragment.this.getActivity());
                    alertDialog.setCancelable(false);
                    String actionStr = "";
                    if (SettingsActivity.mapEnabled == 1) {
                        if (ComonUtils.LoadPreferences("map_local", "mapLocal", MapPreferenceFragment.this.getActivity()) == 0) {
                            actionStr = MapPreferenceFragment.this.getString(R.string.disable_string);
                            alertDialog.setMessage(MapPreferenceFragment.this.getString(R.string.disable_map_displaying_string) + "?");
                        } else {
                            actionStr = MapPreferenceFragment.this.getString(R.string.enable_string);
                            alertDialog.setMessage(MapPreferenceFragment.this.getString(R.string.enable_map_displaying_string) + "?");
                        }
                    }
                    alertDialog.setPositiveButton(actionStr, new C00833());
                    alertDialog.setNegativeButton(R.string.cancel_string, new C00844());
                    alertDialog.show();
                }
                return false;
            }
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_map);
            setHasOptionsMenu(true);
            //JSONManager jSONManager = new JSONManager(getActivity());
            SettingsActivity.mapEnabled = this.mainActivity.getMap();

            findPreference(getString(R.string.map_key)).setOnPreferenceClickListener(new C00851());
        }
    }
*/

}
