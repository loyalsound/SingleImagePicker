package com.ls.sip;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @author Gil
 * @since 04/03/2014
 */
public class GalleryFragment extends Fragment {

    public static ImageGalleryAdapter mGalleryAdapter;
    public static ImagePickerActivity mActivity;

    private final String TAG = GalleryFragment.class.getCanonicalName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.sip_fragment_gallery, container, false);
        GridView galleryGridView = (GridView) rootView.findViewById(R.id.gallery_grid);
        mActivity = ((ImagePickerActivity) getActivity());


        List<Uri> images = getImagesFromGallary(getActivity());
        mGalleryAdapter = new ImageGalleryAdapter(getActivity(), images);

        galleryGridView.setAdapter(mGalleryAdapter);
        galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Ensure uri have scheme
                Uri uri = mGalleryAdapter.getItem(i);
                EventBus.getDefault().post(new PictureSelectedEvent(Uri.fromFile(new File(uri.getPath()))));
            }
        });

        return rootView;
    }

    public void refreshGallery(Context context) {

        List<Uri> images = getImagesFromGallary(context);

        if (mGalleryAdapter == null) {
            mGalleryAdapter = new ImageGalleryAdapter(context, images);
        } else {
            mGalleryAdapter.clear();
            mGalleryAdapter.addAll(images);
            mGalleryAdapter.notifyDataSetChanged();
        }
    }

    public List<Uri> getImagesFromGallary(Context context) {

        List<Uri> images = new ArrayList<Uri>();

        Cursor imageCursor = null;

        try {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns.ORIENTATION};
            final String orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";

            imageCursor = context.getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);

            if (imageCursor != null) {
                while (imageCursor.moveToNext()) {
                    Uri uri = Uri.parse(imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                    images.add(uri);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot access Gallery", e);
            // TODO: Show some UI
        } finally {
            if (imageCursor != null && !imageCursor.isClosed()) {
                imageCursor.close();
            }
        }


        return images;

    }

    class ViewHolder {

        ImageView mThumbnail;

        // This is like storing too much data in memory.
        // find a better way to handle this
        Uri uri;

        public ViewHolder(View view) {
            mThumbnail = (ImageView) view.findViewById(R.id.thumbnail_image);
        }

    }

    public class ImageGalleryAdapter extends ArrayAdapter<Uri> {

        Context context;


        public ImageGalleryAdapter(Context context, List<Uri> images) {
            super(context, 0, images);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.sip_grid_item_gallery_thumbnail, parent, false);
                holder = new ViewHolder(convertView);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Uri mUri = getItem(position);

            if (holder.uri == null || !holder.uri.equals(mUri)) {

                Glide.with(context)
                        .load(mUri.toString())
                        .thumbnail(0.1f)
                        .dontAnimate()
                        .centerCrop()
                        .placeholder(R.drawable.sip_place_holder_gallery)
                        .error(R.drawable.sip_no_image)

                        .into(holder.mThumbnail);
                holder.uri = mUri;
            }

            return convertView;
        }
    }

    public static class PictureSelectedEvent {

        private Uri uri;

        public PictureSelectedEvent(Uri uri) {
            this.uri = uri;
        }

        public Uri getUri() {
            return uri;
        }

        public void setUri(Uri uri) {
            this.uri = uri;
        }
    }
}