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

package com.github.ypdieguez.smsradar.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.ypdieguez.smsradar.AppWidget;
import com.github.ypdieguez.smsradar.R;
import com.github.ypdieguez.smsradar.preference.SmsInfoPreference;
import com.github.ypdieguez.smsradar.ui.SettingsActivity;
import com.github.ypdieguez.smsradar.util.CalendarUtil;
import com.github.ypdieguez.smsradar.util.NotificationUtil;
import com.tuenti.smsradar.SharedPreferencesSmsStorage;

public class ResetSmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                SharedPreferencesSmsStorage.NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(SharedPreferencesSmsStorage.SMS_SENT_COUNT, 0);
        editor.putLong(SharedPreferencesSmsStorage.LAST_SMS_DATE,
                CalendarUtil.getFirstMillisInActualMonth());
        editor.apply();

        // Register alarm
        CalendarUtil.registerAlarm(context);

        // Send notification
        NotificationUtil.notify(context, "channel_sms_restore", context.getString(
                R.string.channel_sms_restore), context
                .getString(R.string.not_sms_restore, Integer.parseInt(PreferenceManager
                        .getDefaultSharedPreferences(context).getString(
                                SettingsActivity.KEY_SMS_TO_SEND,
                                context.getString(R.string.default_value_sms_to_send)))));

        // Update widget
        AppWidget.updateWidget(context, 0);

        // Update count in activity
        context.sendBroadcast(new Intent(SmsInfoPreference.ACTION)
                .putExtra(SharedPreferencesSmsStorage.SMS_SENT_COUNT, 0));
    }
}
