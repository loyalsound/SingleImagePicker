package com.ls.sip;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * @author jay
 * @author 9you
 * @since 3/5/15
 */
public class Config implements Parcelable {

    public static final Creator<Config> CREATOR = new Creator<Config>() {
        public Config createFromParcel(Parcel in) {
            return new Config(in);
        }

        public Config[] newArray(int size) {
            return new Config[size];
        }
    };

    private int tabBackgroundColor;
    private int tabSelectionIndicatorColor;

    private int selectedBottomColor;

    private int cameraHeight = R.dimen.ted_picker_camera_height;

    private int cameraBtnImage = R.drawable.sip_ic_camera;
    private int cameraBtnBackground = R.drawable.sip_btn_bg;

    private int selectedCloseImage = R.drawable.abc_ic_clear_mtrl_alpha;
    private int selectedBottomHeight = R.dimen.ted_picker_selected_image_height;

    private int savedDirectoryName = R.string.default_directory;

    private boolean flashOn = false;

    public Config() {
        super();
    }

    public Config(Parcel in) {
        super();
        this.tabBackgroundColor = in.readInt();
        this.tabSelectionIndicatorColor = in.readInt();

        this.selectedBottomColor = in.readInt();

        this.cameraHeight = in.readInt();

        this.cameraBtnImage = in.readInt();
        this.cameraBtnBackground = in.readInt();

        this.selectedCloseImage = in.readInt();
        this.selectedBottomHeight = in.readInt();

        this.savedDirectoryName = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.tabBackgroundColor);
        dest.writeInt(this.tabSelectionIndicatorColor);

        dest.writeInt(this.selectedBottomColor);

        dest.writeInt(this.cameraHeight);

        dest.writeInt(this.cameraBtnImage);
        dest.writeInt(this.cameraBtnBackground);

        dest.writeInt(this.selectedCloseImage);
        dest.writeInt(this.selectedBottomHeight);

        dest.writeInt(this.savedDirectoryName);
    }

    public int getCameraHeight() {
        return cameraHeight;
    }

    public void setCameraHeight(@DimenRes int dimenRes) {
        if (dimenRes <= 0)
            throw new IllegalArgumentException("Invalid value for cameraHeight");

        this.cameraHeight = dimenRes;
    }

    public int getSavedDirectoryName() {
        return savedDirectoryName;
    }

    public void setSavedDirectoryName(@StringRes int stringRes) {
        if (stringRes <= 0)
            throw new IllegalArgumentException("Invalid value for savedDirectoryName");

        this.savedDirectoryName = stringRes;
    }

    public int getSelectedBottomHeight() {
        return selectedBottomHeight;
    }

    public void setSelectedBottomHeight(@DimenRes int dimenRes) {
        if (dimenRes <= 0)
            throw new IllegalArgumentException("Invalid value for selectedBottomHeight");

        this.selectedBottomHeight = dimenRes;
    }

    public int getSelectedBottomColor() {
        return selectedBottomColor;
    }

    public void setSelectedBottomColor(@ColorRes int colorRes) {
        if (colorRes <= 0)
            throw new IllegalArgumentException("Invalid value for selectedBottomColor");

        this.selectedBottomColor = colorRes;
    }

    public int getTabBackgroundColor() {
        return tabBackgroundColor;
    }

    public void setTabBackgroundColor(@ColorRes int colorRes) {
        if (colorRes <= 0)
            throw new IllegalArgumentException("Invalid value for tabBackgroundColor");


        this.tabBackgroundColor = colorRes;

    }

    public int getTabSelectionIndicatorColor() {
        return tabSelectionIndicatorColor;
    }

    /**
     * Sets selected tab indicator color.
     */
    public void setTabSelectionIndicatorColor(@ColorRes int colorRes) {
        if (colorRes <= 0) {
            throw new IllegalArgumentException("Invalid value for tabSelectionIndicatorColor");
        }

        this.tabSelectionIndicatorColor = colorRes;
    }

    public int getCameraBtnImage() {
        return cameraBtnImage;
    }

    public void setCameraBtnImage(@DrawableRes int drawableRes) {
        if (drawableRes <= 0) {
            throw new IllegalArgumentException("Invalid value for cameraBtnImage");
        }
        this.cameraBtnImage = drawableRes;
    }

    public int getCameraBtnBackground() {
        return cameraBtnBackground;
    }

    public void setCameraBtnBackground(@DrawableRes int drawableRes) {
        if (drawableRes <= 0) {
            throw new IllegalArgumentException("Invalid value for cameraBtnBackground");
        }
        this.cameraBtnBackground = drawableRes;
    }

    public int getSelectedCloseImage() {
        return selectedCloseImage;
    }

    public void setSelectedCloseImage(@DrawableRes int drawableRes) {
        if (drawableRes <= 0) {
            throw new IllegalArgumentException("Invalid value for selectedCloseImage");
        }
        this.selectedCloseImage = drawableRes;
    }

    public boolean isFlashOn(){
        return flashOn;
    }

    public void setFlashOn(boolean flashOn){
        this.flashOn = flashOn;
    }
}
