package com.wubydax.geartwswipe;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
@SuppressWarnings("unused")
class PopulateAppListTask extends AsyncTask<Void, Void, Void> {
    private static final String LOG_TAG = PopulateAppListTask.class.getName();
    private OnTaskCompletedListener mOnTaskCompletedListener;
    private List<AppInfo> mAppInfoList;




    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {

        mAppInfoList = createAppList();
        //Sorting the ApplicationInfo list by alphabetical order based on loadLabel method (application name)
        Collections.sort(mAppInfoList, new Comparator<AppInfo>() {


            @Override
            public int compare(AppInfo lhs, AppInfo rhs) {
                return String.CASE_INSENSITIVE_ORDER.compare(lhs.mAppLabel, rhs.mAppLabel);
            }
        });
        return null;
    }

    //We create a list of ApplicationInfo Objects from which we can retrieve the information we need for setView of the List adapter.
    //We use getLaunchIntentForPackage method to only get apps with "good" intent that will launch for sure. In short we only get launcher available apps into this list.
    private List<AppInfo> createAppList() {
        Log.d(LOG_TAG, "createAppList is called in async task");
        PackageManager packageManager = MyApp.getContext().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);
        Log.d(LOG_TAG, "createAppList resolve info list size is " + resolveInfoList.size());
        List<AppInfo> appList = new ArrayList<>();
        for (ResolveInfo resolveInfo : resolveInfoList) {
            if (!isCancelled()) {
                try {
                    AppInfo appInfo = new AppInfo();
                    appInfo.mAppIcon = resolveInfo.activityInfo.loadIcon(packageManager);
                    appInfo.mAppLabel = resolveInfo.loadLabel(packageManager).toString();
                    appInfo.mPackageName = resolveInfo.activityInfo.packageName;
                    appInfo.mActivityName = resolveInfo.activityInfo.name;
                    Intent appIntent = new Intent();
                    intent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
                    appInfo.mIntent = appIntent;
                    appList.add(appInfo);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }

        }
        Log.d(LOG_TAG, "createAppList app list size is " + appList.size());

        return appList;

    }


    @Override
    protected void onPostExecute(Void aVoid) {
        if (mOnTaskCompletedListener != null) {
            mOnTaskCompletedListener.onListCreated(mAppInfoList);
        }
    }



    public void setOnTaskCompletedListener(OnTaskCompletedListener listener) {
        mOnTaskCompletedListener = listener;
    }

    public interface OnTaskCompletedListener {
        void onListCreated(List<AppInfo> list);
    }


}
