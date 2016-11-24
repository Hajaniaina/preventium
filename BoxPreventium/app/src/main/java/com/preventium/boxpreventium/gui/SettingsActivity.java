package com.preventium.boxpreventium.gui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.preventium.boxpreventium.R;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            String stringValue = value.toString();

            if (preference instanceof ListPreference) {

                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            }
            else {

                // For all other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);
            }

            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {

        // Set the listener to watch for value changes
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        try {

            // Trigger the listener immediately with the preference's current value
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

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment (String fragmentName) {

        return true;
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

    public static class SosPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate (Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_sos);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.phone_select_sms_sos)));
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
