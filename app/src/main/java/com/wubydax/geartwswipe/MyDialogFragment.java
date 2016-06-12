package com.wubydax.geartwswipe;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;

/*      Created by Roberto Mariani and Anna Berkovitch, 2015
        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

public class MyDialogFragment extends DialogFragment {


    private static final String REQUEST_CODE = "dialog_request_code";
    private int mRequestCode;

    public MyDialogFragment() {
        // Required empty public constructor
    }

    public static MyDialogFragment newInstance(int requestCode) {
        MyDialogFragment myDialogFragment = new MyDialogFragment();
        Bundle args = new Bundle();
        args.putInt(REQUEST_CODE, requestCode);
        myDialogFragment.setArguments(args);
        return myDialogFragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mRequestCode = savedInstanceState != null ? savedInstanceState.getInt(REQUEST_CODE) : getArguments().getInt(REQUEST_CODE);
        String namePlaceHolder = null;
        String dialogMessage = getString(R.string.swipe_reset_message);
        switch (mRequestCode) {
            case 0:
                dialogMessage = getString(R.string.swipe_reset_message);
                break;
            case 1:
                dialogMessage = getString(R.string.prefs_saved_app_dialog_message);
                break;
            case 2:
                dialogMessage = getString(R.string.prefs_saved_shortcut_dialog_message);
                break;

        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String appComponents = sharedPreferences.getString(SetAppActivity.OPEN_APP_KEY, null);
        String shortcutName = sharedPreferences.getString(SetAppActivity.SHORTCUT_NAME_KEY, null);
        if (appComponents != null) {
            String[] components = appComponents.split("/");
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(components[0], components[1]));
            ResolveInfo resolveInfo = MyApp.getContext().getPackageManager().resolveActivity(intent, 0);
            if (resolveInfo != null) {
                namePlaceHolder = resolveInfo.loadLabel(MyApp.getContext().getPackageManager()).toString();
            }
        } else if (shortcutName != null) {
            namePlaceHolder = shortcutName;
        }
        boolean isActionSet = namePlaceHolder != null;
        dialogMessage = isActionSet ? String.format(dialogMessage, namePlaceHolder) : getString(R.string.swipe_reset_no_prefs);
        return isActionSet ? getResetDialog(dialogMessage) : getSetUpDialog(dialogMessage);


    }

    private AlertDialog getSetUpDialog(String dialogMessage) {
        return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme))
                .setTitle(R.string.set_up_twswipe)
                .setMessage(dialogMessage)
                .setIcon(R.mipmap.ic_launcher)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getActivity(), SetAppActivity.class));
                        getActivity().finish();
                    }
                })
                .create();
    }

    private AlertDialog getResetDialog(String dialogMessage) {
        return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme))
                .setTitle(mRequestCode == 0 ? R.string.swipe_reset_title : R.string.prefs_saved_dialog_title)
                .setMessage(dialogMessage)
                .setIcon(R.mipmap.ic_launcher)
                .setNegativeButton(R.string.reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                                .putString(SetAppActivity.OPEN_APP_KEY, null)
                                .putString(SetAppActivity.SHORTCUT_ACTION_KEY, null)
                                .putString(SetAppActivity.SHORTCUT_NAME_KEY, null)
                                .apply();
                        if (mRequestCode == 0) {
                            startActivity(new Intent(getActivity(), SetAppActivity.class));
                            getActivity().finish();
                        }
                    }
                })
                .setPositiveButton(mRequestCode != 0 ? android.R.string.ok : android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .create();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(REQUEST_CODE, mRequestCode);
        super.onSaveInstanceState(outState);
    }


}
