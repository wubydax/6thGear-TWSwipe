package com.wubydax.geartwswipe;

import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
public class AppListAdapter extends BaseAdapter implements SectionIndexer {

    List<AppInfo> mAppList;
    private HashMap<String, Integer> alphaIndexer;
    private String[] sections;

    public AppListAdapter(List<AppInfo> list) {

        this.mAppList = list;

        //adding Indexer to display the first letter of an app while using fast scroll
        alphaIndexer = new HashMap<>();
        for (int i = 0; i < mAppList.size(); i++) {
            String s = mAppList.get(i).mAppLabel;
            String s1 = s.substring(0, 1).toUpperCase();
            if (!alphaIndexer.containsKey(s1))
                alphaIndexer.put(s1, i);
        }

        Set<String> sectionLetters = alphaIndexer.keySet();
        ArrayList<String> sectionList = new ArrayList<>(sectionLetters);
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
    public int getSectionForPosition(int position) {
        for (int i = sections.length - 1; i >= 0; i--) {
            if (position >= alphaIndexer.get(sections[i])) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public int getCount() {
        if (mAppList != null) {
            return mAppList.size();
        }
        return 0;
    }

    @Override
    public AppInfo getItem(int position) {
        if (mAppList != null) {
            return mAppList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(new ContextThemeWrapper(MyApp.getContext(), R.style.AppTheme));
            convertView = inflater.inflate(R.layout.app_item, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mAppNames = (TextView) convertView.findViewById(R.id.appName);
            viewHolder.mAppPackage = (TextView) convertView.findViewById(R.id.appPackage);
            viewHolder.mAppIcon = (ImageView) convertView.findViewById(R.id.appIcon);
            convertView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        AppInfo appInfo = mAppList.get(position);

        holder.mAppNames.setText(appInfo.mAppLabel);
        holder.mAppPackage.setText(appInfo.mPackageName);
        holder.mAppIcon.setImageDrawable(appInfo.mAppIcon);

        return convertView;
    }

    private class ViewHolder {
        public TextView mAppNames;
        public TextView mAppPackage;
        public ImageView mAppIcon;
    }
}
