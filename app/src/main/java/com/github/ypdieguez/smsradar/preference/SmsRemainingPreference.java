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

package com.github.ypdieguez.smsradar.preference;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceManagerFix;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.github.ypdieguez.smsradar.R;
import com.github.ypdieguez.smsradar.ui.SettingsActivity;

import es.dmoral.toasty.Toasty;

import static com.github.ypdieguez.smsradar.util.IntUtil.parseInt;

public class SmsRemainingPreference extends CheckBoxPreference implements
        DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

    private CharSequence mPositiveButtonText;
    private CharSequence mNegativeButtonText;

    /**
     * The dialog, if it is showing.
     */
    private AlertDialog mDialog;

    /**
     * Which button was clicked.
     */
    private int mWhichButtonClicked;

    /**
     * The edit text shown in the dialog.
     */
    private EditText mEditText;

    @SuppressLint({"RestrictedApi", "PrivateResource"})
    public SmsRemainingPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DialogPreference,
                R.attr.editTextPreferenceStyle, 0);

        mPositiveButtonText = TypedArrayUtils.getString(a,
                R.styleable.DialogPreference_positiveButtonText,
                R.styleable.DialogPreference_android_positiveButtonText);

        mNegativeButtonText = TypedArrayUtils.getString(a,
                R.styleable.DialogPreference_negativeButtonText,
                R.styleable.DialogPreference_android_negativeButtonText);

        a.recycle();

    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null && mDialog.isShowing()) return;
                showDialog();
            }
        });
    }


    /**
     * Shows the dialog associated with this Preference.
     */
    private void showDialog() {
        mWhichButtonClicked = DialogInterface.BUTTON_NEGATIVE;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.pref_dialog_sms_remaining_title))
                .setPositiveButton(mPositiveButtonText, this)
                .setNegativeButton(mNegativeButtonText, this);

        View contentView = onCreateDialogView();
        onBindDialogView(contentView);
        mBuilder.setView(contentView);

        // Create the dialog
        mDialog = mBuilder.create();
        mDialog.setOnDismissListener(this);
        mDialog.show();
        requestInputMethod(mDialog);
    }

    /**
     * Creates the content view for the dialog.
     *
     * @return The content View for the dialog.
     */
    @SuppressLint("InflateParams")
    private View onCreateDialogView() {
        return LayoutInflater.from(getContext()).inflate(R.layout.preference_dialog_edittext,
                null);
    }

    /**
     * Binds views in the content View of the dialog to data.
     *
     * @param view The content View of the dialog, if it is custom.
     */
    private void onBindDialogView(View view) {
        mEditText = view.findViewById(android.R.id.edit);
        mEditText.append(
                String.valueOf(PreferenceManagerFix.getDefaultSharedPreferences(getContext())
                        .getInt(SettingsActivity.KEY_SMS_REMAINING_TO_NOTIFY, 0)));
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int v = parseInt(s.toString());
                if (!isValueValid(v)) {
                    mEditText.setError(getContext().getString(
                            R.string.pref_dialog_sms_remaining_error, getSmsToSend()));
                } else {
                    mEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Sets the required flags on the dialog window to enable input method window to show up.
     */
    private void requestInputMethod(Dialog dialog) {
        Window window = dialog.getWindow();
        assert window != null;
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        mWhichButtonClicked = which;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mDialog = null;
        onDialogClosed(mWhichButtonClicked == DialogInterface.BUTTON_POSITIVE);
    }

    /**
     * Called when the dialog is dismissed and should be used to save data to
     * the {@link android.content.SharedPreferences}.
     *
     * @param positiveResult Whether the positive button was clicked (true), or
     *                       the negative button was clicked or the dialog was canceled (false).
     */
    private void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String text = mEditText.getText().toString();
            int v = parseInt(text);
            if (isValueValid(v)) {
                PreferenceManagerFix.getDefaultSharedPreferences(getContext()).edit().putInt(
                        SettingsActivity.KEY_SMS_REMAINING_TO_NOTIFY, v).apply();
                setSummary(getContext().getResources().getQuantityString(
                        R.plurals.pref_summary_notify_sms_remaining, v, v));
            } else {
                Toasty.error(getContext(), getContext().getString(R.string.toast_value_not_valid),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isValueValid(int v) {
        return (v != -1 && v < getSmsToSend());
    }

    private int getSmsToSend() {
        return parseInt(PreferenceManagerFix
                .getDefaultSharedPreferences(getContext()).getString(
                        SettingsActivity.KEY_SMS_TO_SEND,
                        getContext().getString(R.string.default_value_sms_to_send)));
    }
}
