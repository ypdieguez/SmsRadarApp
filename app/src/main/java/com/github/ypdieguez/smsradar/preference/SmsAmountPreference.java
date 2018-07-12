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

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.github.ypdieguez.smsradar.R;
import com.takisoft.fix.support.v7.preference.EditTextPreference;

import static com.github.ypdieguez.smsradar.util.IntUtil.parseInt;

public class SmsAmountPreference extends EditTextPreference {

    public SmsAmountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (parseInt(getEditText().getText().toString()) == -1) {
                    getEditText().setError(getContext().getString(
                            R.string.pref_dialog_sms_amount_error, Integer.MAX_VALUE));
                } else {
                    getEditText().setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void setText(String text) {
        super.setText(String.valueOf(parseInt(text)));
    }
}
