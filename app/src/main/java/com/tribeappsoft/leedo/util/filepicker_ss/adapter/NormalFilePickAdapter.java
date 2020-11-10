package com.tribeappsoft.leedo.util.filepicker_ss.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.util.filepicker_ss.ToastUtil;
import com.tribeappsoft.leedo.util.filepicker_ss.Util;
import com.tribeappsoft.leedo.util.filepicker_ss.filter.entity.NormalFile;

import java.util.ArrayList;

/**
 * Created by Vincent Woo
 * Date: 2016/10/26
 * Time: 10:23
 */

public class NormalFilePickAdapter extends BaseAdapter<NormalFile, NormalFilePickAdapter.NormalFilePickViewHolder> {
    private int mMaxNumber;
    private int mCurrentNumber = 0;

    public NormalFilePickAdapter(Context ctx, int max) {
        this(ctx, new ArrayList<NormalFile>(), max);
    }

    public NormalFilePickAdapter(Context ctx, ArrayList<NormalFile> list, int max) {
        super(ctx, list);
        mMaxNumber = max;
    }

    @Override
    public NormalFilePickViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_layout_item_normal_file_pick, parent, false);
        return new NormalFilePickViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final NormalFilePickViewHolder holder, final int position) {
        final NormalFile file = mList.get(position);

        holder.mTvTitle.setText(Util.extractFileNameWithSuffix(file.getPath()));
        holder.mTvTitle.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        if (holder.mTvTitle.getMeasuredWidth() >
                Util.getScreenWidth(mContext) - Util.dip2px(mContext, 10 + 32 + 10 + 48 + 10 * 2)) {
            holder.mTvTitle.setLines(2);
        } else {
            holder.mTvTitle.setLines(1);
        }

        if (file.isSelected()) {
            holder.rl_mainLayout.setSelected(true);
        } else {
            holder.rl_mainLayout.setSelected(false);
        }

        if (file.getPath().endsWith("xls") || file.getPath().endsWith("xlsx")) {
            holder.mIvIcon.setImageResource(R.drawable.img_google_sheets);
        } else if (file.getPath().endsWith("doc") || file.getPath().endsWith("docx")){
            holder.mIvIcon.setImageResource(R.drawable.img_google_docs);
        } else if (file.getPath().endsWith("ppt") || file.getPath().endsWith("pptx")){
            holder.mIvIcon.setImageResource(R.drawable.img_google_slides);
        } else if (file.getPath().endsWith("pdf")||file.getPath().endsWith("PDF")){
            holder.mIvIcon.setImageResource(R.drawable.img_pdf);
        } else if (file.getPath().endsWith("txt")){
            holder.mIvIcon.setImageResource(R.drawable.img_google_txt);
        } else {
            holder.mIvIcon.setImageResource(R.drawable.unknown_file);
        }

        holder.rl_mainLayout.setOnClickListener(v -> {
            if (!v.isSelected() && isUpToMax()) {
                ToastUtil.getInstance(mContext).showToast(R.string.vw_up_to_max);
                return;
            }

            if (v.isSelected()) {
                holder.rl_mainLayout.setSelected(false);
                mCurrentNumber--;
            } else {
                holder.rl_mainLayout.setSelected(true);
                mCurrentNumber++;
            }

            mList.get(holder.getAdapterPosition()).setSelected(holder.rl_mainLayout.isSelected());

            if (mListener != null) {
                mListener.OnSelectStateChanged(holder.rl_mainLayout.isSelected(), mList.get(holder.getAdapterPosition()));
            }
        });


     /*   holder.rl_mainLayout.setOnClickListener(v -> {
            if (!v.isSelected() && isUpToMax()) {
                ToastUtil.getInstance(mContext).showToast(R.string.vw_up_to_max);
                return;
            }

            if (v.isSelected()) {
                holder.mCbx.setSelected(false);
                mCurrentNumber--;
            } else {
                holder.mCbx.setSelected(true);
                mCurrentNumber++;
            }

            mList.get(holder.getAdapterPosition()).setSelected(holder.mCbx.isSelected());

            if (mListener != null) {
                mListener.OnSelectStateChanged(holder.mCbx.isSelected(), mList.get(holder.getAdapterPosition()));
            }
        });*/

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri uri = Uri.parse("file://" + file.getPath());
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(uri, "audio/mp3");
//                if (Util.detectIntent(mContext, intent)) {
//                    mContext.startActivity(intent);
//                } else {
//                    Toast.makeText(mContext, "No Application exists for audio!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    @Override
    public void onBindViewHolder(AudioPickAdapter.AudioPickViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class NormalFilePickViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIvIcon;
        private TextView mTvTitle;
        private ImageView mCbx;
        private RelativeLayout rl_mainLayout;

        public NormalFilePickViewHolder(View itemView) {
            super(itemView);
            mIvIcon = (ImageView) itemView.findViewById(R.id.ic_file);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_file_title);
            mCbx = (ImageView) itemView.findViewById(R.id.cbx);
            rl_mainLayout = (RelativeLayout) itemView.findViewById(R.id.rl_mainLayout);
        }
    }

    private boolean isUpToMax() {
        return mCurrentNumber >= mMaxNumber;
    }

}
