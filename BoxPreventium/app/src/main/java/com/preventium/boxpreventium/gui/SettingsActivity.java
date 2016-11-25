package com.preventium.boxpreventium.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.pavelsikun.seekbarpreference.SeekBarPreference;
import com.preventium.boxpreventium.R;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = "SettingsActivity";

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

        realNumbers[0] = sharedPref.getString(fragment.getString(R.string.phone_number_1), "");
        realNumbers[1] = sharedPref.getString(fragment.getString(R.string.phone_number_2), "");
        realNumbers[2] = sharedPref.getString(fragment.getString(R.string.phone_number_3), "");
        realNumbers[3] = sharedPref.getString(fragment.getString(R.string.phone_number_4), "");
        realNumbers[4] = sharedPref.getString(fragment.getString(R.string.phone_number_5), "");

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

            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_pause)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_pause_timeout)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pause_trigger_time)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.stop_trigger_time)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.recommended_speed_time)));

            updatePhoneNumbersList(this, R.string.phone_select_sms_pause);

            final SeekBarPreference pauseTimeoutPref = (SeekBarPreference) findPreference(getString(R.string.pause_trigger_time));
            final SeekBarPreference stopTimeoutPref = (SeekBarPreference) findPreference(getString(R.string.stop_trigger_time));

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

            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_sos)));
            updatePhoneNumbersList(this, R.string.phone_select_sms_sos);
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

            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_shock)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.shock_trigger_mG)));

            updatePhoneNumbersList(this, R.string.phone_select_sms_shock);
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

            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_tracking)));
            updatePhoneNumbersList(this, R.string.phone_select_sms_tracking);
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

            bindPreferenceSummaryToValue(findPreference(getString(R.string.qr_select_start_mode)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.qr_select_stop_mode)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_qr)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_qr_timeout)));

            updatePhoneNumbersList(this, R.string.phone_select_sms_qr);
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

            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_voice)));
            updatePhoneNumbersList(this, R.string.phone_select_voice);
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
}
