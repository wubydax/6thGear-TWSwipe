package com.wubydax.geartwswipe;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class ResetDialogActivity extends Activity {
    SharedPreferences mPreferences;
    PackageManager pm;
    AlertDialog.Builder mBuilder;
    String appName, shortcutName, mExtra, mSetPackage;
    Intent whoCalled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pm = getPackageManager();
        mPreferences = getSharedPreferences("swipe_prefs", MODE_PRIVATE);
        mSetPackage = mPreferences.getString("swipe_app", null);
        whoCalled = getIntent();
        mExtra = whoCalled.getStringExtra("SetApp");
        shortcutName = mPreferences.getString("name", "No shortcut was assigned");
        try {
            appName = getAppName(mSetPackage);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //if the intent extra passed from the main activity was "allset", meaning an app was chosen and not shortcut
        //we show a dialog with chosen app name and option to reset app preferences for app
        if ("allset".equals(mExtra)){
            appSetDialog();
        }
        //if the intent extra passed from the main activity was "Shortcut Set", meaning a shortcut was chosen and not app
        //we show a dialog with chosen shortcut name and option to reset app preferences for shortcut
        else if ("Shortcut Set".equals(mExtra)){
            shortcutSetDialog();
        }
        else{
        //if no intent was passed to this activity, we know that it was opened from elswhere (launcher, other app intent... etc)
        //so we open an option for reset dialog
            resetDialog();
        }
        setContentView(R.layout.activity_reset_dialog);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reset_dialog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private String getAppName (String packageName) throws PackageManager.NameNotFoundException {
        ApplicationInfo appInfo;
        String appName;
        try {
            appInfo = pm.getApplicationInfo(packageName, 0);

            appName = appInfo.loadLabel(pm).toString();
        }
        catch (PackageManager.NameNotFoundException e){
            appName = null;
        }



        return appName;
    }
    private void resetDialog(){
        mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));

        mBuilder.setIcon(R.mipmap.ic_launcher);
        mBuilder.setTitle(getString(R.string.swipe_reset_title));
        if (mSetPackage == null && shortcutName.equals("No shortcut was assigned")){
            mBuilder.setMessage(getString(R.string.swipe_reset_no_prefs));
        }
        else if (mSetPackage!=null && shortcutName.equals("No shortcut was assigned")) {
            mBuilder.setMessage(String.format(getString(R.string.swipe_reset_message), appName));
        }
        else {
            mBuilder.setMessage(String.format(getString(R.string.swipe_reset_message), shortcutName));
        }
        mBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        mBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString("swipe_app", null).putString("name", null).putString("action", null).commit();
                Intent intent = new Intent(getApplicationContext(), SetAppActivity.class);
                startActivity(intent);
                finish();
            }

        });
        mBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();

                }
                return false;
            }
        });

        Dialog dialog = mBuilder.create();
        dialog.show();
    }
    private void appSetDialog(){
        mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));
        mBuilder.setIcon(R.mipmap.ic_launcher);
        mBuilder.setTitle(getString(R.string.prefs_saved_dialog_title));
        mBuilder.setMessage(String.format(getString(R.string.prefs_saved_app_dialog_message), appName));
        mBuilder.setNegativeButton(getString(R.string.reset), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString("swipe_app", null).commit();
                finish();
            }
        });
        mBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
             SetAppActivity.SetApp.finish();
                finish();
            }
        });
        mBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();

                }
                return false;
            }
        });
        Dialog mSetSuccessfully = mBuilder.create();
        mSetSuccessfully.show();
    }
    private void shortcutSetDialog(){
        mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));
        mBuilder.setIcon(R.mipmap.ic_launcher);
        mBuilder.setTitle(getString(R.string.prefs_saved_dialog_title));
        mBuilder.setMessage(String.format(getString(R.string.prefs_saved_shortcut_dialog_message), shortcutName));

        mBuilder.setNegativeButton(getString(R.string.reset), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString("name", null).putString("action", null).commit();
                finish();
            }
        });
        mBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SetAppActivity.SetApp.finish();
                finish();
            }
        });
        mBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();

                }
                return false;
            }
        });
        Dialog mSetSuccessfully = mBuilder.create();
        mSetSuccessfully.show();
    }

}
