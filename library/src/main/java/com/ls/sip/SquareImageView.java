package com.ls.sip;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author Gil
 * @since 09/06/2014
 */
public class SquareImageView extends ImageView {

    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //Squares the thumbnail
    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
    }

}
