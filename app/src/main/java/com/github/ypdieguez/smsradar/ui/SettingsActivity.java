/*
 * Copyright 2018 Yordan P. Dieguez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.ypdieguez.smsradar.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManagerFix;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.ypdieguez.smsradar.R;
import com.github.ypdieguez.smsradar.preference.SmsRemainingPreference;
import com.github.ypdieguez.smsradar.util.UIUtil;
import com.takisoft.fix.support.v7.preference.EditTextPreference;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;
import com.takisoft.fix.support.v7.preference.RingtonePreference;

import es.dmoral.toasty.Toasty;

import static com.github.ypdieguez.smsradar.util.IntUtil.parseInt;

/**
 * Activity that presents a set of application settings.
 *
 * @author Yordan P. Dieguez <ypdieguez@tuta.io>
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String FIRST_LAUNCH = "first_launch";
    public static final String KEY_SMS_TO_SEND = "sms_to_send";
    public static final String KEY_RINGTONE = "ringtone";
    public static final String KEY_NOTIFY_SMS_WARNING = "notify_sms_warning";
    public static final String KEY_NOTIFY_SMS_EXHAUSTED = "notify_sms_exhausted";
    public static final String KEY_NOTIFY_SMS_RESET = "notify_sms_reset";
    public static final String KEY_SMS_REMAINING_TO_NOTIFY = "sms_remaining_to_notify";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PreferenceManagerFix.getDefaultSharedPreferences(this)
                .getBoolean(FIRST_LAUNCH, false)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                    == PackageManager.PERMISSION_GRANTED) {

                setContentView(R.layout.activity_main);
                Toolbar toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

                Toasty.Config.getInstance()
                        .setErrorColor(ContextCompat.getColor(this, R.color.primary))
                        .setInfoColor(ContextCompat.getColor(this, R.color.accent))
                        .apply();

            } else {
                startActivity(UIUtil.getIntent(this, PermissionActivity.class));
                finish();
            }
        } else {
            //Init default values
            PreferenceManagerFix.getDefaultSharedPreferences(this).edit()
                    .putInt(KEY_SMS_REMAINING_TO_NOTIFY, Integer.parseInt(
                            getString(R.string.default_value_sms_remaining))).apply();

            startActivity(UIUtil.getIntent(this, IntroActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        InfoDialogFragment.newInstance().show(getSupportFragmentManager(), "dialog");
        return true;
    }

    /**
     * This fragment shows number of sms sent and general preferences.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PreferenceFragment extends PreferenceFragmentCompat {
        SharedPreferences mPreferences;

        EditTextPreference mSmsAmountPref;
        SmsRemainingPreference mSmsRemainingNotPref;
        CheckBoxPreference mSmsExhaustedNotPref;
        CheckBoxPreference mSmsResetNotPref;
        RingtonePreference mRingtonePref;
        /**
         * A preference value change listener that updates the preference to reflect its new value.
         */
        private Preference.OnPreferenceChangeListener bindPreferenceListener =
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object value) {
                        String stringValue;
                        if (value != null) {
                            stringValue = value.toString();
                        } else {
                            stringValue = "";
                        }

                        if (preference instanceof EditTextPreference) {
                            int v = parseInt(stringValue);
                            if (v != -1) {
                                preference.setSummary(String.valueOf(v));
                                if (v == 0) {
                                    updatePreferences(0, false, false, false, false);
                                } else if (v == 1) {
                                    updatePreferences(0, false, true, true, true);
                                } else if (v <= 5) {
                                    updatePreferences(1, true, true, true, true);
                                } else if (v < 15) {
                                    updatePreferences(2, true, true, true, true);
                                } else if (v < 30) {
                                    updatePreferences(5, true, true, true, true);
                                } else {
                                    updatePreferences(10, true, true, true, true);
                                }
                            } else {
                                mSmsAmountPref.getEditText().setError(null);
                                assert getActivity() != null;

                                Toasty.error(preference.getContext(), getString(
                                        R.string.toast_value_not_valid), Toast.LENGTH_LONG).show();

                                return false;
                            }
                        } else if (preference instanceof CheckBoxPreference) {

                            if (value != null) {
                                ((CheckBoxPreference) preference).setChecked((Boolean) value);
                            }

                            if (!mSmsRemainingNotPref.isChecked() && !mSmsExhaustedNotPref.isChecked()
                                    && !mSmsResetNotPref.isChecked()) {
                                mRingtonePref.setEnabled(false);
                            } else {
                                mRingtonePref.setEnabled(true);
                            }
                        } else if (preference instanceof RingtonePreference) {
                            // For ringtone preferences, look up the correct display value
                            // using RingtoneManager.
                            if (TextUtils.isEmpty(stringValue)) {
                                // Empty values correspond to 'silent' (no ringtone).
                                preference.setSummary(R.string.pref_ringtone_silent);

                            } else {
                                Ringtone ringtone = RingtoneManager.getRingtone(
                                        preference.getContext(), Uri.parse(stringValue));

                                if (ringtone == null) {
                                    // Clear the summary if there was a lookup error.
                                    preference.setSummary(null);
                                } else {
                                    // Set the summary to reflect the new ringtone display
                                    // name.
                                    String name = ringtone.getTitle(preference.getContext());
                                    preference.setSummary(name);
                                }
                            }

                        } else {
                            // For all other preferences, set the summary to the value's
                            // simple string representation.
                            preference.setSummary(stringValue);
                        }

                        return true;
                    }

                    /**
                     * Update preferences
                     * @param n Default number of sms remaining to notify.
                     * @param args If preferences will be enable or disable. In order.
                     */
                    private void updatePreferences(int n, Object... args) {
                        if (n != mPreferences.getInt(KEY_SMS_REMAINING_TO_NOTIFY,
                                Integer.parseInt(getString(R.string.default_value_sms_remaining)))
                                ) {
                            mPreferences.edit().putInt(KEY_SMS_REMAINING_TO_NOTIFY, n).apply();
                            assert getContext() != null;
                            Toasty.info(getContext(), getString(R.string.toast_sms_remaining_reset,
                                    n), Toast.LENGTH_LONG).show();
                        }

                        mSmsRemainingNotPref.setSummary(getResources().getQuantityString(
                                R.plurals.pref_summary_notify_sms_remaining, n, n));

                        mSmsRemainingNotPref.setEnabled((Boolean) args[0]);
                        mSmsExhaustedNotPref.setEnabled((Boolean) args[1]);
                        mSmsResetNotPref.setEnabled((Boolean) args[2]);

                        if ((Boolean) args[3] && (mSmsRemainingNotPref.isChecked() ||
                                mSmsExhaustedNotPref.isChecked() || mSmsResetNotPref.isChecked())) {
                            mRingtonePref.setEnabled(true);
                        } else {
                            mRingtonePref.setEnabled(false);
                        }
                    }
                };

        @Override
        public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            mPreferences = getPreferenceManager().getSharedPreferences();

            mSmsAmountPref = (EditTextPreference) findPreference(KEY_SMS_TO_SEND);
            mSmsRemainingNotPref = (SmsRemainingPreference) findPreference(KEY_NOTIFY_SMS_WARNING);
            mSmsExhaustedNotPref = (CheckBoxPreference) findPreference(KEY_NOTIFY_SMS_EXHAUSTED);
            mSmsResetNotPref = (CheckBoxPreference) findPreference(KEY_NOTIFY_SMS_RESET);
            mRingtonePref = (RingtonePreference) findPreference(KEY_RINGTONE);

            bindPreference(mSmsAmountPref);
            bindPreference(mSmsRemainingNotPref);
            bindPreference(mSmsExhaustedNotPref);
            bindPreference(mSmsResetNotPref);
            bindPreference(mRingtonePref);
        }

        /**
         * Binds a preference
         *
         * @see #bindPreferenceListener
         */
        private void bindPreference(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(bindPreferenceListener);

            // Trigger the listener immediately with the preference's
            // current value.
            Object value;
            if (preference instanceof CheckBoxPreference) {
                value = mPreferences.getBoolean(preference.getKey(), true);
            } else {
                value = mPreferences.getString(preference.getKey(), "");
            }

            bindPreferenceListener.onPreferenceChange(preference, value);
        }
    }
}
