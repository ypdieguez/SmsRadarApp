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
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import com.github.ypdieguez.smsradar.AppWidget;
import com.github.ypdieguez.smsradar.R;
import com.github.ypdieguez.smsradar.util.CalendarUtil;
import com.tuenti.smsradar.SharedPreferencesSmsStorage;
import com.tuenti.smsradar.Sms;
import com.tuenti.smsradar.SmsRadar;
import com.tuenti.smsradar.SmsRadarService;
import com.tuenti.smsradar.SmsType;

import java.util.List;

/**
 * This Preference shows number of sms sent, general preferences and let active the [SmsRadarService].
 *
 * @author Yordan P. Dieguez <ypdieguez@tuta.io>
 */
public class SmsInfoPreference extends Preference {

    public static final String ACTION = SmsInfoPreference.class.getName();

    private TextView mSmsSentCount;

    /**
     * BroadcastReceiver that update the UI.
     */
    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSmsSentCount.setText(String.valueOf(intent.getIntExtra(
                    SharedPreferencesSmsStorage.SMS_SENT_COUNT, 0)));
        }
    };

    public SmsInfoPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mSmsSentCount = holder.itemView.findViewById(R.id.sms_sent_count);
        updateSmsSentCount();
        checkService();
    }

    @Override
    public void onAttached() {
        super.onAttached();
        getContext().registerReceiver(br, new IntentFilter(ACTION));
    }


    @Override
    public void onDetached() {
        super.onDetached();
        getContext().unregisterReceiver(br);
    }


    private void checkService() {
        if (!isSmsRadarServiceRunning()) {
            SmsRadar.initializeSmsRadarService(getContext());
        }
    }

    /**
     * If [SmsRadarService] is running
     *
     * @return boolean
     */
    private boolean isSmsRadarServiceRunning() {
        ActivityManager manager =
                (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);

        if (manager != null) {
            List<ActivityManager.RunningServiceInfo> services =
                    manager.getRunningServices(Integer.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo serviceInfo : services) {
                if (serviceInfo.service.getClassName().equals(SmsRadarService.class.getName())
                        && serviceInfo.pid != 0) {
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressLint("NewApi")
    private void updateSmsSentCount() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Uri uri = Telephony.Sms.Sent.CONTENT_URI;
                String[] projection = new String[]{Telephony.Sms._ID, Telephony.Sms.ADDRESS,
                        Telephony.Sms.DATE, Telephony.Sms.BODY, Telephony.Sms.TYPE};
                String selection = Telephony.Sms.DATE + " >= ?";
                String[] selectionArgs = {String.valueOf(CalendarUtil.getFirstMillisInActualMonth())};

                Cursor cursor = getContext().getContentResolver().query(uri, projection, selection,
                        selectionArgs, null);

                if (cursor != null) {
                    int count = 0;
                    while (cursor.moveToNext()) {
                        String address = cursor.getString(1);
                        String date = cursor.getString(2);
                        String body = cursor.getString(3);
                        String type = cursor.getString(4);

                        Sms sms = new Sms(address, date, body, SmsType.fromValue(
                                Integer.parseInt(type)));

                        count += sms.getPages();
                    }

                    cursor.close();

                    SharedPreferences p = getContext().getSharedPreferences(
                            SharedPreferencesSmsStorage.NAME, Context.MODE_PRIVATE);
                    int smsSentCount = p.getInt(SharedPreferencesSmsStorage.SMS_SENT_COUNT,
                            0);
                    long lastSmsDate = p.getLong(SharedPreferencesSmsStorage.LAST_SMS_DATE,
                            CalendarUtil.getFirstMillisInActualMonth());

                    if ((CalendarUtil.getActualMonth() == CalendarUtil.getMonth(lastSmsDate)
                            && count > smsSentCount) || (CalendarUtil.getActualMonth()
                            != CalendarUtil.getMonth(lastSmsDate))) {
                        p.edit().putInt(SharedPreferencesSmsStorage.SMS_SENT_COUNT, count).apply();
                        smsSentCount = count;
                    }

                    mSmsSentCount.setText(String.valueOf(smsSentCount));

                    AppWidget.updateWidget(getContext(), smsSentCount);
                }
            }
        });
    }
}
