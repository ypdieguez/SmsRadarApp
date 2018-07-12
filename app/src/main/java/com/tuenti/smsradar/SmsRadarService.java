/*
 * Copyright (c) Tuenti Technologies S.L. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tuenti.smsradar;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.preference.PreferenceManagerFix;

import com.github.ypdieguez.smsradar.AppWidget;
import com.github.ypdieguez.smsradar.R;
import com.github.ypdieguez.smsradar.preference.SmsInfoPreference;
import com.github.ypdieguez.smsradar.ui.SettingsActivity;
import com.github.ypdieguez.smsradar.util.NotificationUtil;

/**
 * Service created to handle the SmsContentObserver registration. This service has the responsibility of register and
 * unregister the content observer in sms content provider when it's created and destroyed.
 * <p/>
 * The SmsContentObserver will be registered over the CONTENT_SMS_URI to be notified each time the system update the
 * sms content provider.
 *
 * @author Pedro Vcente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 * @author Yordan P. Dieguez <ypdieguez@tuta.io>
 */
public class SmsRadarService extends Service {

    private static final String CONTENT_SMS_URI = "content://sms";
    private static final int ONE_SECOND = 1000;

    private ContentResolver contentResolver;
    private SmsObserver smsObserver;
    private AlarmManager alarmManager = null;
    private TimeProvider timeProvider = null;
    private boolean initialized;

    public SmsRadarService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!initialized) {
            initializeService();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finishService();
        restartService();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        finishService();
        restartService();
    }

    private void initializeService() {
        initialized = true;
        initializeDependencies();
        registerSmsContentObserver();
    }

    private void initializeDependencies() {
        if (!areDependenciesInitialized()) {
            initializeContentResolver();
            initializeSmsObserver();
        }
    }

    private boolean areDependenciesInitialized() {
        return contentResolver != null && smsObserver != null;
    }

    private void initializeSmsObserver() {
        Handler handler = new Handler();
        SmsCursorParser smsCursorParser = initializeSmsCursorParser();
        this.smsObserver = new SmsObserver(contentResolver, handler, smsCursorParser, this);
    }

    private SmsCursorParser initializeSmsCursorParser() {
        SharedPreferences preferences =
                getSharedPreferences(SharedPreferencesSmsStorage.NAME, MODE_PRIVATE);
        SmsStorage smsStorage = new SharedPreferencesSmsStorage(preferences);
        return new SmsCursorParser(smsStorage, getTimeProvider());
    }

    private void initializeContentResolver() {
        this.contentResolver = getContentResolver();
    }

    private void finishService() {
        initialized = false;
        unregisterSmsContentObserver();
    }


    private void registerSmsContentObserver() {
        Uri smsUri = Uri.parse(CONTENT_SMS_URI);
        contentResolver.registerContentObserver(smsUri, true, smsObserver);
    }

    private void unregisterSmsContentObserver() {
        contentResolver.unregisterContentObserver(smsObserver);
    }

    private void restartService() {
        Intent intent = new Intent(this, SmsRadarService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        long now = getTimeProvider().getDate().getTime();
        getAlarmManager().set(AlarmManager.RTC_WAKEUP, now + ONE_SECOND, pendingIntent);
    }

    private TimeProvider getTimeProvider() {
        return timeProvider != null ? timeProvider : new TimeProvider();
    }

    private AlarmManager getAlarmManager() {
        return alarmManager != null ? alarmManager : (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    void sendBroadcastAndNotify(int count) {
        // Send Broadcast
        sendBroadcast(new Intent(SmsInfoPreference.ACTION)
                .putExtra(SharedPreferencesSmsStorage.SMS_SENT_COUNT, count));

        // Update widget
        AppWidget.updateWidget(this, count);

        // Send Notification
        SharedPreferences pref =
                PreferenceManagerFix.getDefaultSharedPreferences(this);

        boolean notSmsExhausted =
                pref.getBoolean(SettingsActivity.KEY_NOTIFY_SMS_EXHAUSTED, true);
        boolean notSmsWarning =
                pref.getBoolean(SettingsActivity.KEY_NOTIFY_SMS_WARNING, true);
        int smsRemainingToNot = pref.getInt(SettingsActivity.KEY_SMS_REMAINING_TO_NOTIFY, 0);
        int smsToSend =
                Integer.parseInt(pref.getString(SettingsActivity.KEY_SMS_TO_SEND,
                        getString(R.string.default_value_sms_to_send)));

        String msj, channelId;
        CharSequence channelName;
        if (smsToSend > 0 && notSmsExhausted && smsToSend <= count) {
            msj = getResources().getString(R.string.not_sms_exhausted, smsToSend);
            channelId = "channel_sms_exhausted";
            channelName = getString(R.string.channel_sms_exhausted);
        } else if (smsToSend > 1 && notSmsWarning && smsToSend <= count + smsRemainingToNot) {
            msj = getResources().getString(R.string.not_sms_remaining, smsToSend - count);
            channelId = "channel_sms_remaining";
            channelName = getString(R.string.channel_sms_remaining);
        } else {
            return;
        }

        NotificationUtil.notify(this, channelId, channelName, msj);
    }
}
