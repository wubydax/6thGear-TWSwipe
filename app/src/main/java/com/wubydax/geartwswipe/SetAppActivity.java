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
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.net.URISyntaxException;
import java.util.List;


public class SetAppActivity extends Activity implements PopulateAppListTask.OnTaskCompletedListener {
    public static final String OPEN_APP_KEY = "swipe_app";
    public static final String SHORTCUT_ACTION_KEY = "action";
    public static final String SHORTCUT_NAME_KEY = "name";
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
        String components = mPreferences.getString(OPEN_APP_KEY, null);
        String setName = mPreferences.getString(SHORTCUT_NAME_KEY, null);
        String setShortcutAction = mPreferences.getString(SHORTCUT_ACTION_KEY, null);

        if (components != null) {
            //Translucent theme with no title bar is the app is launching shortcut or app and not itself
            Intent openApp = new Intent();
            String[] componentItems = components.split("/");
            openApp.setComponent(new ComponentName(componentItems[0], componentItems[1]));
            if (getPackageManager().resolveActivity(openApp, 0) != null) {
                setTheme(R.style.Theme_Transparent);
                startActivity(openApp);
                finish();
            } else {
                setUpList();
            }

        } else if (setName != null && setShortcutAction != null) {
            try {
                setTheme(R.style.Theme_Transparent);
                Intent intent = Intent.parseUri(setShortcutAction, 0);
                if (getPackageManager().resolveActivity(intent, 0) != null) {
                    startActivity(intent);
                    finish();
                } else {
                    setUpList();
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
                setUpList();
            }
        } else {
            //Default app theme if launching the list of choices for swipe
            setUpList();
        }


    }

    private void setUpList() {
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_set_app);
        PopulateAppListTask listTask = new PopulateAppListTask();
        listTask.setOnTaskCompletedListener(this);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        listTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(new AboutFragment(), "about");
            ft.commitAllowingStateLoss();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Void method to display the shortcut picker dialog on button click. The reference to this method is in the layout file in widget Button attributes
    //We call an activity picker for all applications that have an intent filter "android.intent.action.CREATE_SHORTCUT" in their manifest
    //On item selected we start the chosen activity for result and it's indexed as requestCode 1
    //Based on requestCode, we will identify the intent we receive from the launched activity in method onActivityResult
    public void chooseShortcut(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        intent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.shortcut_chooser_title));
        startActivityForResult(intent, 46);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if requestCode is 1, means we are talking about the intent of initially clicking on shortcut chooser dialog item
        //we start the activity for result and requestCode is now 2, we will retrieve that result to store the shortcut characteristics in our SharedPreferences
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 46:
                startActivityForResult(data, 58);
                break;
            case 58:
                Bundle extra = data.getExtras();
                Intent intent = (Intent) extra.get(Intent.EXTRA_SHORTCUT_INTENT);
                String shortcutName = extra.getString(Intent.EXTRA_SHORTCUT_NAME);
                if (intent != null && shortcutName != null) {
                    mPreferences.edit().putString(SHORTCUT_ACTION_KEY, intent.toUri(0))
                            .putString(SHORTCUT_NAME_KEY, shortcutName)
                            .putString(OPEN_APP_KEY, null)
                            .apply();
                    getFragmentManager().beginTransaction().add(MyDialogFragment.newInstance(2), "my_dialog").commit();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);

        }

    }

    @Override
    public void onListCreated(List<AppInfo> list) {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //This is only relevant to setting single app as Swipe action, since it relates to the ListView attribute
                AppListAdapter appListAdapter = (AppListAdapter) parent.getAdapter();
                AppInfo appInfo = appListAdapter.getItem(position);
                String packageName = appInfo.mPackageName;
                String activityName = appInfo.mActivityName;
                PreferenceManager.getDefaultSharedPreferences(MyApp.getContext()).edit()
                        .putString(OPEN_APP_KEY, packageName + "/" + activityName)
                        .putString(SHORTCUT_NAME_KEY, null)
                        .putString(SHORTCUT_ACTION_KEY, null)
                        .apply();
                getFragmentManager().beginTransaction().add(MyDialogFragment.newInstance(1), "my_dialog").commit();

            }
        });
        listView.setFastScrollEnabled(true);
        listView.setFadingEdgeLength(1);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setScrollingCacheEnabled(false);
        AppListAdapter appListAdapter = new AppListAdapter(list);
        listView.setAdapter(appListAdapter);
    }


}



