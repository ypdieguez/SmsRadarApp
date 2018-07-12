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

package com.github.ypdieguez.smsradar.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.github.ypdieguez.smsradar.R;
import com.github.ypdieguez.smsradar.ui.SettingsActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationUtil {

    public static void notify(Context context, String channelId, CharSequence channelName,
                              String msj) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                nm.createNotificationChannel(
                        new NotificationChannel(channelId, channelName,
                                NotificationManager.IMPORTANCE_HIGH));
            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(msj)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(SettingsActivity.KEY_RINGTONE, context.getString(
                                    R.string.default_value_ringtone))))
                    .setContentIntent(PendingIntent.getActivity(context, 0,
                            UIUtil.getIntent(context, SettingsActivity.class),
                            PendingIntent.FLAG_CANCEL_CURRENT))
                    .setAutoCancel(true);

            nm.notify(0, mBuilder.build());
        }
    }
}
