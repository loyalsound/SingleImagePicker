package com.ls.sip;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.yalantis.ucrop.view.OverlayView;
import com.yalantis.ucrop.view.UCropView;

/**
 * @author Thanh
 * @since 2/29/16
 */
public class WrappedUCropView extends UCropView {

    public WrappedUCropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WrappedUCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SipCropView,
                defStyleAttr, R.style.Sip_Theme_CropViewStyle);

        OverlayView overlayView = getOverlayView();

        // Overlay view options
        overlayView.setDimmedColor(a.getColor(R.styleable.SipCropView_sipDimmedColor, ContextCompat.getColor(context, R.color.sip_crop_dimmed_color)));
        overlayView.setOvalDimmedLayer(a.getBoolean(R.styleable.SipCropView_sipOvalDimmedLayer, OverlayView.DEFAULT_OVAL_DIMMED_LAYER));

        overlayView.setShowCropFrame(a.getBoolean(R.styleable.SipCropView_sipShowCropFrame, OverlayView.DEFAULT_SHOW_CROP_FRAME));
        overlayView.setCropFrameColor(a.getColor(R.styleable.SipCropView_sipCropFrameColor, ContextCompat.getColor(context, R.color.sip_crop_frame_color)));
        overlayView.setCropFrameStrokeWidth(a.getDimensionPixelSize(R.styleable.SipCropView_sipCropFrameStrokeWidth, getResources().getDimensionPixelSize(R.dimen.sip_crop_frame_stroke_width)));

        overlayView.setShowCropGrid(a.getBoolean(R.styleable.SipCropView_sipShowCropGrid, OverlayView.DEFAULT_SHOW_CROP_GRID));
        overlayView.setCropGridRowCount(a.getInt(R.styleable.SipCropView_sipCropGridRowCount, OverlayView.DEFAULT_CROP_GRID_ROW_COUNT));
        overlayView.setCropGridColumnCount(a.getInt(R.styleable.SipCropView_sipCropGridColumnCount, OverlayView.DEFAULT_CROP_GRID_COLUMN_COUNT));
        overlayView.setCropGridColor(a.getColor(R.styleable.SipCropView_sipCropGridColor, ContextCompat.getColor(context, R.color.sip_crop_grid_color)));
        overlayView.setCropGridStrokeWidth(a.getDimensionPixelSize(R.styleable.SipCropView_sipCropGridStrokeWidth, getResources().getDimensionPixelSize(R.dimen.sip_crop_grid_stroke_width)));

        a.recycle();
    }

}
