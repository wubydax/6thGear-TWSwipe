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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.TextView;


public class AboutFragment extends DialogFragment {

    AlertDialog.Builder mBuilder;



    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme));
        mBuilder.setTitle(R.string.about_title)
                .setMessage(getString(R.string.about_message))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        Dialog mAbout = mBuilder.create();
        mAbout.show();


        TextView mMessage = (TextView) mAbout.findViewById(android.R.id.message);
        mMessage.setTextSize(14);




        return mAbout;
    }
}
