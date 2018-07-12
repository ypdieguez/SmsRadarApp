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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.github.ypdieguez.smsradar.receiver.ResetSmsReceiver;

import java.util.Calendar;

public class CalendarUtil {

    public static int getActualMonth() {
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    public static int getMonth(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.get(Calendar.MONTH);
    }

    public static int getMonth(String millis) {
        return getMonth(Long.parseLong(millis));
    }

    public static long getFirstMillisInActualMonth() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.clear(Calendar.MINUTE);
        c.clear(Calendar.SECOND);
        c.clear(Calendar.MILLISECOND);
        return c.getTimeInMillis();
    }

    public static void registerAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(getFirstMillisInActualMonth());
            c.set(Calendar.MONTH, c.get(Calendar.MONTH) + 1);

            alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                    PendingIntent.getBroadcast(context, 0,
                            new Intent(context, ResetSmsReceiver.class), 0));
        }
    }

//    public static void cancelAlarm(Context context) {
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        if (alarmManager != null) {
//            alarmManager.cancel(PendingIntent.getBroadcast(context, 0,
//                    new Intent(context, ResetSmsReceiver.class), 0));
//        }
//    }
}
