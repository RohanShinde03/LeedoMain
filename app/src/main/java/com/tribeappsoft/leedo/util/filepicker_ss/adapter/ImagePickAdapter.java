package com.tribeappsoft.leedo.util.filepicker_ss.adapter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.util.filepicker_ss.Constant;
import com.tribeappsoft.leedo.util.filepicker_ss.ToastUtil;
import com.tribeappsoft.leedo.util.filepicker_ss.Util;
import com.tribeappsoft.leedo.util.filepicker_ss.activity.ImageBrowserActivity;
import com.tribeappsoft.leedo.util.filepicker_ss.activity.ImagePickActivity;
import com.tribeappsoft.leedo.util.filepicker_ss.filter.entity.ImageFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_DCIM;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.tribeappsoft.leedo.util.filepicker_ss.activity.ImageBrowserActivity.IMAGE_BROWSER_INIT_INDEX;
import static com.tribeappsoft.leedo.util.filepicker_ss.activity.ImageBrowserActivity.IMAGE_BROWSER_SELECTED_LIST;

/**
 * Created by Vincent Woo
 * Date: 2016/10/13
 * Time: 16:07
 */

public class ImagePickAdapter extends BaseAdapter<ImageFile, ImagePickAdapter.ImagePickViewHolder> {
    private boolean isNeedImagePager;
    private boolean isNeedCamera;
    private int mMaxNumber;
    private int mCurrentNumber = 0;
    public String mImagePath;
    public Uri mImageUri;

    public ImagePickAdapter(Context ctx, boolean needCamera, boolean isNeedImagePager, int max) {
        this(ctx, new ArrayList<ImageFile>(), needCamera, isNeedImagePager, max);
    }

    public ImagePickAdapter(Context ctx, ArrayList<ImageFile> list, boolean needCamera, boolean needImagePager, int max) {
        super(ctx, list);
        isNeedCamera = needCamera;
        mMaxNumber = max;
        isNeedImagePager = needImagePager;
    }

    @Override
    public ImagePickViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_layout_item_image_pick, parent, false);
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        if (params != null) {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            params.height = width / ImagePickActivity.COLUMN_NUMBER;
        }
        return new ImagePickViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ImagePickViewHolder holder, int position) {
        if (isNeedCamera && position == 0) {
            holder.mIvCamera.setVisibility(View.VISIBLE);
            holder.mIvThumbnail.setVisibility(View.INVISIBLE);
            holder.rl_layout.setVisibility(View.INVISIBLE);
            holder.rl_layout.setVisibility(View.INVISIBLE);
            holder.mShadow.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
                File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).getAbsolutePath()
                        + "/IMG_" + timeStamp + ".jpg");
                mImagePath = file.getAbsolutePath();

                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, mImagePath);
                mImageUri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                if (Util.detectIntent(mContext, intent)) {
                    ((Activity) mContext).startActivityForResult(intent, Constant.REQUEST_CODE_TAKE_IMAGE);
                } else {
                    ToastUtil.getInstance(mContext).showToast(mContext.getString(R.string.vw_no_photo_app));
                }
            });
        } else {
            holder.mIvCamera.setVisibility(View.INVISIBLE);
            holder.mIvThumbnail.setVisibility(View.VISIBLE);
            holder.rl_layout.setVisibility(View.VISIBLE);
            holder.rl_layout.setVisibility(View.VISIBLE);

            ImageFile file;
            if (isNeedCamera) {
                file = mList.get(position - 1);
            } else {
                file = mList.get(position);
            }

            RequestOptions options = new RequestOptions();
            Glide.with(mContext)
                    .load(file.getPath())
                    .apply(options.centerCrop())
                    .transition(withCrossFade())
//                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.mIvThumbnail);

            if (file.isSelected()) {
                holder.rl_layout.setSelected(true);
                holder.mShadow.setVisibility(View.VISIBLE);
            } else {
                holder.rl_layout.setSelected(false);
                holder.mShadow.setVisibility(View.INVISIBLE);
            }

            holder.rl_layout.setOnClickListener(v -> {
                if (!v.isSelected() && isUpToMax()) {
                    ToastUtil.getInstance(mContext).showToast(R.string.vw_up_to_max);
                    return;
                }

                int index = isNeedCamera ? holder.getAdapterPosition() - 1 : holder.getAdapterPosition();
                if (v.isSelected()) {
                    holder.mShadow.setVisibility(View.INVISIBLE);
                    holder.rl_layout.setSelected(false);
                    mCurrentNumber--;
                    mList.get(index).setSelected(false);
                } else {
                    holder.mShadow.setVisibility(View.VISIBLE);
                    holder.rl_layout.setSelected(true);
                    mCurrentNumber++;
                    mList.get(index).setSelected(true);
                }

                if (mListener != null) {
                    mListener.OnSelectStateChanged(holder.rl_layout.isSelected(), mList.get(index));
                }
            });

           /* if (isNeedImagePager) {
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(mContext, ImageBrowserActivity.class);
                    intent.putExtra(Constant.MAX_NUMBER, mMaxNumber);
                    intent.putExtra(IMAGE_BROWSER_INIT_INDEX,
                            isNeedCamera ? holder.getAdapterPosition() - 1 : holder.getAdapterPosition());
                    intent.putParcelableArrayListExtra(IMAGE_BROWSER_SELECTED_LIST, ((ImagePickActivity) mContext).mSelectedList);
                    ((Activity) mContext).startActivityForResult(intent, Constant.REQUEST_CODE_BROWSER_IMAGE);
                });
            } else {
                holder.mIvThumbnail.setOnClickListener(view -> {
                    if (!holder.rl_layout.isSelected() && isUpToMax()) {
                        ToastUtil.getInstance(mContext).showToast(R.string.vw_up_to_max);
                        return;
                    }

                    int index = isNeedCamera ? holder.getAdapterPosition() - 1 : holder.getAdapterPosition();
                    if (holder.rl_layout.isSelected()) {
                        holder.mShadow.setVisibility(View.INVISIBLE);
                        holder.rl_layout.setSelected(false);
                        mCurrentNumber--;
                        mList.get(index).setSelected(false);
                    } else {
                        holder.mShadow.setVisibility(View.VISIBLE);
                        holder.rl_layout.setSelected(true);
                        mCurrentNumber++;
                        mList.get(index).setSelected(true);
                    }

                    if (mListener != null) {
                        mListener.OnSelectStateChanged(holder.rl_layout.isSelected(), mList.get(index));
                    }
                });
            }*/
        }
    }

    @Override
    public void onBindViewHolder(AudioPickAdapter.AudioPickViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return isNeedCamera ? mList.size() + 1 : mList.size();
    }

    class ImagePickViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIvCamera;
        private ImageView mIvThumbnail;
        private View mShadow;
        private ImageView mCbx;
        private RelativeLayout rl_layout;

        public ImagePickViewHolder(View itemView) {
            super(itemView);
            mIvCamera = (ImageView) itemView.findViewById(R.id.iv_camera);
            mIvThumbnail = (ImageView) itemView.findViewById(R.id.iv_thumbnail);
            mShadow = itemView.findViewById(R.id.shadow);
            mCbx = (ImageView) itemView.findViewById(R.id.cbx);
            rl_layout = (RelativeLayout) itemView.findViewById(R.id.rl_imgPicker_main);
        }
    }

    public boolean isUpToMax() {
        return mCurrentNumber >= mMaxNumber;
    }

    public void setCurrentNumber(int number) {
        mCurrentNumber = number;
    }
}
