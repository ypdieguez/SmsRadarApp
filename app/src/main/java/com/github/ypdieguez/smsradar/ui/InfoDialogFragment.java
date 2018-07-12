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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ypdieguez.smsradar.R;

import es.dmoral.toasty.Toasty;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     InfoDialogFragment.newInstance().show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class InfoDialogFragment extends BottomSheetDialogFragment
        implements View.OnLongClickListener {

    View view;

    public static InfoDialogFragment newInstance() {
        return new InfoDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.info_dialog, container, false);

        assert getContext() != null;
        Context c = getContext();

        TextView tvDeveloper = view.findViewById(R.id.developer);
        tvDeveloper.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(c,
                R.drawable.face), null, null, null);
        tvDeveloper.setOnLongClickListener(this);


        TextView tvCertificate = view.findViewById(R.id.certificate);
        tvCertificate.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(c,
                R.drawable.certificate), null, null, null);
        tvCertificate.setOnLongClickListener(this);

        TextView tvEmail = view.findViewById(R.id.email);
        tvEmail.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(c,
                R.drawable.email), null, null, null);
        tvEmail.setOnLongClickListener(this);

        TextView tvCellphone = view.findViewById(R.id.cellphone);
        tvCellphone.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(c,
                R.drawable.cellphone_android), null, null, null);
        tvCellphone.setOnLongClickListener(this);

        TextView tvGithub = view.findViewById(R.id.github);
        tvGithub.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(c,
                R.drawable.github_circle), null, null, null);
        tvGithub.setOnLongClickListener(this);

        TextView tvUser = view.findViewById(R.id.user);
        tvUser.setOnLongClickListener(this);

        return view;
    }

    @Override
    public boolean onLongClick(View v) {
        ClipboardManager cm = (ClipboardManager) v.getContext().getSystemService(
                Context.CLIPBOARD_SERVICE);
        String text = ((TextView) v).getText().toString();
        assert cm != null;
        cm.setPrimaryClip(ClipData.newPlainText("text", text));
        Toasty.info(v.getContext(), getString(R.string.toast_copy, text), Toast.LENGTH_LONG)
                .show();

        return true;
    }
}
