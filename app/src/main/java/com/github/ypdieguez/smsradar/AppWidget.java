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

package com.github.ypdieguez.smsradar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.github.ypdieguez.smsradar.ui.SettingsActivity;
import com.github.ypdieguez.smsradar.util.UIUtil;
import com.tuenti.smsradar.SharedPreferencesSmsStorage;

/**
 * App Widget.
 */
public class AppWidget extends AppWidgetProvider {

    /**
     * Update widget
     *
     * @param context Context
     * @param count   SMS count
     */
    public static void updateWidget(Context context, int count) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setTextViewText(R.id.appwidget_text, String.valueOf(count));
        appWidgetManager.updateAppWidget(new ComponentName(context, AppWidget.class), views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        String count = String.valueOf(context.getSharedPreferences(SharedPreferencesSmsStorage.NAME,
                Context.MODE_PRIVATE).getInt(SharedPreferencesSmsStorage.SMS_SENT_COUNT,
                0));

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            // Create an Intent to launch SettingsActivity
            Intent intent = UIUtil.getIntent(context, SettingsActivity.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pendIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
            views.setTextViewText(R.id.appwidget_text, count);

            // Get the layout for the App Widget and attach an on-click listener
            views.setOnClickPendingIntent(R.id.appwidget, pendIntent);
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}

