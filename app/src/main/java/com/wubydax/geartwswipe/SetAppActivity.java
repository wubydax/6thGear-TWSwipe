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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class SetAppActivity extends Activity {
    List<ApplicationInfo> mAppList;
    PackageManager pm;
    TextView mAppName;
    TextView mAppPackage;
    ImageView mImageView;
    ListView mAppListView;
    SharedPreferences mPreferences;
    public static Activity SetApp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SetApp = this;
        mPreferences = getSharedPreferences("swipe_prefs", MODE_PRIVATE);
        pm = getPackageManager();
        String mSetPackage = mPreferences.getString("swipe_app", null);
        String mSetName = mPreferences.getString("name", null);
        String mSetShortcutAction = mPreferences.getString("action", null);
        Intent applicationInfo = pm.getLaunchIntentForPackage(mSetPackage);

        if(mSetPackage!=null){
            //Translucent theme with no title bar is the app is launching shortcut or app and not itself
            setTheme(R.style.Theme_Transparent);
            setContentView(R.layout.activity_set_app);
            startActivity(applicationInfo);
            finish();
        }
        else if (mSetName!=null&&mSetShortcutAction!=null){
            try {
                setTheme(R.style.Theme_Transparent);
                setContentView(R.layout.activity_set_app);
                startActivity(Intent.parseUri(mSetShortcutAction, 0));
                finish();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        else
        {
            //Defaul app theme if launching the list of choices for swipe
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_set_app);
            PopulateAppList mListTask = new PopulateAppList();
            mListTask.execute();
        }
        mAppListView = (ListView) findViewById(R.id.listView);
        mAppListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //This is only relevant to setting single app as Swipe action, since it relates to the ListView attribute
                Intent applicationInfo = pm.getLaunchIntentForPackage(mAppList.get(position).packageName.toString());
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString("swipe_app", mAppList.get(position).packageName.toString());
                editor.commit();
                Intent dialog = new Intent(SetAppActivity.this, ResetDialogActivity.class);
                dialog.putExtra("SetApp", "allset");
                startActivity(dialog);

            }
        });
        mAppListView.setFastScrollEnabled(true);
        mAppListView.setFadingEdgeLength(1);
        mAppListView.setDivider(null);
        mAppListView.setDividerHeight(0);
        mAppListView.setScrollingCacheEnabled(false);

    }
    //We create a list of ApplicationInfo Objects from which we can retrieve the information we need for setView of the List adapter.
    //We use getLaunchIntentForPackage method to only get apps with "good" intent that will launch for sure. In short we only get launcher available apps into this list.
    private List<ApplicationInfo> createAppList() {
        ArrayList<ApplicationInfo> appList = new ArrayList<ApplicationInfo>();
        List<ApplicationInfo> list = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (int i=0; i<list.size(); i++){
            try {
                if (pm.getLaunchIntentForPackage(list.get(i).packageName)==null){
                    continue;
                }
                appList.add(list.get(i));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return appList;

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Based on item id we launch menu items. Can be extanded as if/else or changed to switch based on id.
        //Items may be added in menu/menu_set_app.xml
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            AboutFragment mAbout = new AboutFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(mAbout, "about");
            ft.commitAllowingStateLoss();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //Void method to display the shortcut picker dialog on button click. The reference to this method is in the layout file in widget Button attributes
    //We call an activity picker for all applications that have an intent filter "android.intent.action.CREATE_SHORTCUT" in their manifest
    //On item selected we start the chosen activity for result and it's indexed as requestCode 1
    //Based on requestCode, we will identify the intent we recieve from the launched activity in method onActivityResult
    public void chooseShortcut(View view) throws URISyntaxException {
        Intent intent = new Intent("android.intent.action.PICK_ACTIVITY");
        intent.putExtra("android.intent.extra.INTENT", new Intent("android.intent.action.CREATE_SHORTCUT"));
        intent.putExtra("android.intent.extra.TITLE", getString(R.string.shortcut_chooser_title));
        startActivityForResult(intent, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if requestCode is 1, means we are talking about the intent of initially clicking on shortcut chooser dialog item
        //we start the activity for result and requestCode is now 2, we will retrieve that result to store the shortcut characteristics in our SharedPreferences
        if (requestCode == 1){
            if (resultCode==RESULT_OK) {
                startActivityForResult(data, 2);
            }
        }
        //If the shortcut providing activity returned a valid result, we store that result in SharedPreferences
        //We store the name (which we will later use for a string in AlertDialog) and an intent (action) which we use for custom shortcut swipe action.
        else if(requestCode==2) {
            if(resultCode==RESULT_OK) {
                SharedPreferences.Editor mEditor = mPreferences.edit();
                Bundle extra = data.getExtras();
                Intent mGetIntent = (Intent) extra.get("android.intent.extra.shortcut.INTENT");
                String mShortcutName = extra.getString("android.intent.extra.shortcut.NAME");
                mEditor.putString("action", mGetIntent.toUri(0)).putString("name", mShortcutName).commit();
                Intent dialog = new Intent(SetAppActivity.this, ResetDialogActivity.class);
                dialog.putExtra("SetApp", "Shortcut Set");
                startActivity(dialog);
            }
        }
        else {
            Toast.makeText(this, "Not successful", Toast.LENGTH_SHORT).show();
        }
    }
        //Creating the list of ApplicationInfo objects takes some time. In order to not block the UI thread we use AsyncTask to perform the list formation
    private class PopulateAppList extends AsyncTask<Void, Void, Void>{
        ProgressDialog mProgress;
        AppListAdapter mAdapter;
        @Override
        protected void onPreExecute() {
            mProgress=ProgressDialog.show(SetAppActivity.this, null, getString(R.string.progress_message));
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            mAppList = createAppList();
            //Sorting the ApplicationInfo list by alphabetical order based on loadLabel method (application name)
            Collections.sort(mAppList, new Comparator<ApplicationInfo>()
            {


                @Override
                public int compare(ApplicationInfo lhs, ApplicationInfo rhs) {
                    return String.CASE_INSENSITIVE_ORDER.compare(lhs.loadLabel(pm).toString(), rhs.loadLabel(pm).toString());
                }
            });
            //Creating an instance of the adapter to display the collected data
            mAdapter = new AppListAdapter(SetAppActivity.this, R.layout.app_item, mAppList, mAppName, mAppPackage, mImageView);
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgress.dismiss();
            //Calling the adapter to action
            mAppListView.setAdapter(mAdapter);


        }

    }
    private class AppListAdapter extends ArrayAdapter<ApplicationInfo> implements SectionIndexer {
        TextView mAppNames;
        TextView mAppPackage;
        ImageView mAppIcon;
        List<ApplicationInfo> mAppList;
        Context c;
        private HashMap<String, Integer> alphaIndexer;
        private String[] sections;

        public AppListAdapter(Context context, int resource, List<ApplicationInfo> appList, TextView mAppNames, TextView mAppPackage, ImageView mAppIcon) {
            super(context, resource, appList);
            this.c = context;
            this.mAppNames = mAppNames;
            this.mAppPackage = mAppPackage;
            this.mAppIcon = mAppIcon;
            this.mAppList = appList;

            //adding Indexer to display the first letter of an app while using fast scroll
            alphaIndexer = new HashMap<String, Integer>();
            for (int i = 0; i < mAppList.size(); i++) {
                String s = mAppList.get(i).loadLabel(pm).toString();
                String s1 = s.substring(0, 1).toUpperCase();
                if (!alphaIndexer.containsKey(s1))
                    alphaIndexer.put(s1, i);
            }

            Set<String> sectionLetters = alphaIndexer.keySet();
            ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
            Collections.sort(sectionList);
            sections = new String[sectionList.size()];
            for (int i = 0; i < sectionList.size(); i++)
                sections[i] = sectionList.get(i);

        }

        @Override
        public Object[] getSections() {
            return sections;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return alphaIndexer.get(sections[sectionIndex]);
        }

        @Override
        public int getSectionForPosition(int position)
            {
                for ( int i = sections.length - 1; i >= 0; i-- ) {
                    if ( position >= alphaIndexer.get( sections[ i ] ) ) {
                        return i;
                    }
                }
                return 0;
            }


        private class ViewHolder {
           public TextView mAppNames;
           public TextView mAppPackage;
           public ImageView mAppIcon;
        }




        @Override
        public int getCount() {
            if (mAppList!=null){
                return mAppList.size();
            }
            return 0;
        }

        @Override
        public ApplicationInfo getItem(int position) {
            if (mAppList!=null){
                return mAppList.get(position);
            }
            return null;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) c.getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.app_item, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.mAppNames = (TextView) convertView.findViewById(R.id.appName);
                viewHolder.mAppPackage = (TextView) convertView.findViewById(R.id.appPackage);
                viewHolder.mAppIcon = (ImageView) convertView.findViewById(R.id.appIcon);
                convertView.setTag(viewHolder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            final ApplicationInfo applicationInfo = mAppList.get(position);

            holder.mAppNames.setText(applicationInfo.loadLabel(pm));
            holder.mAppPackage.setText(applicationInfo.packageName);
            holder.mAppIcon.setImageDrawable(applicationInfo.loadIcon(pm));

            return convertView;
        }
    }


        }



