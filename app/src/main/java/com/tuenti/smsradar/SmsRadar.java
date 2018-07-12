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

import android.content.Context;
import android.content.Intent;

import com.github.ypdieguez.smsradar.util.CalendarUtil;

/**
 * Main library class. This class has to be used to initialize or stop the sms interceptor service.
 *
 * @author Pedro Vcente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 * @author Yordan P. Dieguez <ypdieguez@tuta.io>
 */
public class SmsRadar {

    /**
     * Starts the service to be notified when a new incoming or outgoing sms be processed
     * inside the SMS content provider
     *
     * @param context used to start the service
     */
    public static void initializeSmsRadarService(Context context) {
        // Start service
        context.startService(new Intent(context, SmsRadarService.class));
        // Register alarm
        CalendarUtil.registerAlarm(context);
//        // Enable RECEIVE_BOOT_COMPLETED
//        context.getPackageManager().setComponentEnabledSetting(
//                new ComponentName(context, BootReceiver.class),
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

//    /**
//     * Stop the service
//     *
//     * @param context used to stop the service
//     */
//    public static void stopSmsRadarService(Context context) {
//        // Stop service
//        context.stopService(new Intent(context, SmsRadarService.class));
//        // Cancel alarm
//        CalendarUtil.cancelAlarm(context);
//        // Disable RECEIVE_BOOT_COMPLETED
//        context.getPackageManager().setComponentEnabledSetting(
//                new ComponentName(context, BootReceiver.class),
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
//    }
}
