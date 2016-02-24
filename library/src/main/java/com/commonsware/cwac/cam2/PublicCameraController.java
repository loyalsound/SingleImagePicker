package com.commonsware.cwac.cam2;

/**
 * @author Thanh
 * @since 2/24/16
 */
public class PublicCameraController extends CameraController {

    public PublicCameraController(FocusMode focusMode, boolean allowChangeFlashMode, boolean isVideo) {
        super(focusMode, allowChangeFlashMode, isVideo);
    }

    @Override
    public boolean changeZoom(int delta) {
        return super.changeZoom(delta);
    }

    @Override
    public boolean setZoom(int zoomLevel) {
        return super.setZoom(zoomLevel);
    }

}
