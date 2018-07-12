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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManagerFix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.ypdieguez.smsradar.R;
import com.github.ypdieguez.smsradar.util.UIUtil;

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(Slide.newInstance(R.layout.intro_1));
        addSlide(Slide.newInstance(R.layout.intro_2));

        showSkipButton(false);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        PreferenceManagerFix.getDefaultSharedPreferences(this).edit()
                .putBoolean(SettingsActivity.FIRST_LAUNCH, true).apply();

        startActivity(UIUtil.getIntent(IntroActivity.this, SettingsActivity.class));
        finish();
    }

    public static class Slide extends Fragment {

        private static final String ARG_LAYOUT_RES_ID = "layoutResId";
        private int layoutResId;

        public static Slide newInstance(int layoutResId) {
            Slide slide = new Slide();

            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
            slide.setArguments(args);

            return slide;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
                layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(layoutResId, container, false);
        }
    }
}
