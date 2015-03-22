package com.wubydax.geartwswipe;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by anna on 08/03/15.
 */
public class AppImageView extends ImageView {

    public AppImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public AppImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public AppImageView(Context context) {
        super(context);

    }

    @Override
    public void requestLayout() {

    }
}
