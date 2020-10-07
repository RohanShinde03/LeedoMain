package com.tribeappsoft.leedo.salesPerson.salesHead.openSaleBlocks.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.models.project.BlocksModel;
import com.tribeappsoft.leedo.models.project.ProjectModel;
import com.tribeappsoft.leedo.salesPerson.salesHead.openSaleBlocks.BlockForOpenSaleActivity;
import com.tribeappsoft.leedo.util.Animations;
import com.tribeappsoft.leedo.util.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OpenBlocksRecyclerAdapter extends RecyclerView.Adapter<OpenBlocksRecyclerAdapter.AdapterViewHolder> {

    private BlockForOpenSaleActivity context;
    private ArrayList<ProjectModel> itemArrayList;
    private String TAG="OpenBlocksRecyclerAdapter";
    private final Animations anim;
    private int lastPosition = -1;
    private ArrayList<Integer> addedBlockIdsArrayList;
    private ArrayList<Integer> removedBlockIdsArrayList;


    public OpenBlocksRecyclerAdapter(BlockForOpenSaleActivity context, ArrayList<ProjectModel> itemArrayList) {
        this.context = context;
        this.itemArrayList = itemArrayList;
        this.anim = new Animations();
        this.addedBlockIdsArrayList = new ArrayList<>();
        this.removedBlockIdsArrayList = new ArrayList<>();
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_block_for_open_sale, parent, false);
        return new AdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewHolder holder, int position  ){

        //set animation
        setAnimation(holder.cv_itemBlockForSale, position);

        final ProjectModel myModel = itemArrayList.get(position);

        holder.mTv_projectName.setText(myModel.getProject_name() != null && !myModel.getProject_name().trim().isEmpty() ? myModel.getProject_name() : "--");
        holder.mTv_projectBlocks.setText(myModel.getBlocksModelArrayList() != null && myModel.getBlocksModelArrayList().size()>0 ? "("+myModel.getBlocksModelArrayList().size()+ " Blocks)" : "(0 Blocks)");

        //add checkboxes view
        if (myModel.getBlocksModelArrayList()!=null && myModel.getBlocksModelArrayList().size()>0) {

            holder.ll_addBlocks.removeAllViews();
            for (int i =0 ; i< myModel.getBlocksModelArrayList().size(); i++) {
                View rowView_sub = getBlocksView(i, myModel.getBlocksModelArrayList(), false, myModel);
                holder.ll_addBlocks.addView(rowView_sub);
            }

            //visible blocks view
            holder.ll_addBlocks.setVisibility(View.VISIBLE);

            //hide no blocks
            holder.mTv_noBlocks.setVisibility(View.GONE);
        }
        else {
            //hide block view
            holder.ll_addBlocks.setVisibility(View.GONE);

            //visible no blocks
            holder.mTv_noBlocks.setVisibility(View.VISIBLE);
        }



        holder.iv_editBlock.setOnClickListener(view -> {

            if (myModel.getBlocksModelArrayList() != null && myModel.getBlocksModelArrayList().size() > 0)
            {
                if (myModel.isExpandedView())  //expanded
                {
                    // //do collapse View
                    //new Animations().toggleRotate(iv_others_leadDetails_ec, false);

                    //set edit icon enabled
                    holder.iv_editBlock.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_mode_edit_grey_24dp));

                    //do enabled false when click again back on edit icon
                    holder.ll_addBlocks.removeAllViews();
                    for (int i =0 ; i< myModel.getBlocksModelArrayList().size(); i++) {
                        View rowView_sub = getBlocksView(i, myModel.getBlocksModelArrayList(), false, myModel);
                        holder.ll_addBlocks.addView(rowView_sub);
                    }


                    collapse(holder.ll_updateBlockView);
                    myModel.setExpandedView(false);

                }
                else    // collapsed
                {
                    //do expand view
                    //new Animations().toggleRotate(iv_others_leadDetails_ec, true);

                    //set edit icon enabled
                    holder.iv_editBlock.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_mode_edit_black_24dp));

                    //do enabled true when click on edit option
                    holder.ll_addBlocks.removeAllViews();
                    //clear added block ids arrayList
                    this.addedBlockIdsArrayList = new ArrayList<>();
                    this.addedBlockIdsArrayList.clear();
                    //clear removed block ids arrayList
                    this.removedBlockIdsArrayList = new ArrayList<>();
                    this.removedBlockIdsArrayList.clear();

                    for (int i =0 ; i< myModel.getBlocksModelArrayList().size(); i++) {
                        View rowView_sub = getBlocksView(i, myModel.getBlocksModelArrayList(), true, myModel);
                        holder.ll_addBlocks.addView(rowView_sub);
                    }

                    new Animations().scaleEffect(holder.ll_updateBlockView);

                    expandSubView(holder.ll_updateBlockView);
                    myModel.setExpandedView(true);
                }
            }
            else new Helper().showCustomToast(context, "Blocks are empty for this project!");

        });


        //update blocks
        holder.mBtn_update.setOnClickListener(view -> {

            if (myModel.getAddedBlockIdsArrayList()!=null) Log.e(TAG, "checkAddedList: Btn  "+ Arrays.toString(myModel.getAddedBlockIdsArrayList().toArray()));
            if (myModel.getRemovedBlockIdsArrayList()!=null )Log.e(TAG, "checkRemovedList: Btn  "+ Arrays.toString(myModel.getRemovedBlockIdsArrayList().toArray()));

            context.showAlertDialog(myModel.getAddedBlockIdsArrayList(), myModel.getRemovedBlockIdsArrayList(), myModel.getProject_id());
        });


    }

    private View getBlocksView(int i, ArrayList<BlocksModel> blocksModelArrayList, boolean enabled, ProjectModel projectModel)
    {
        @SuppressLint("InflateParams")
        View rowView = LayoutInflater.from(context).inflate(R.layout.layout_item_block_cb, null );

        final LinearLayoutCompat ll_main = rowView.findViewById(R.id.ll_itemBlockName);
        final MaterialCheckBox mCb_itemBlockName = rowView.findViewById(R.id.mCb_itemBlockName);
        final MaterialTextView mTv_flats = rowView.findViewById(R.id.mTv_itemBlockName_flats);

        BlocksModel model = blocksModelArrayList.get(i);
        //set block name
        mCb_itemBlockName.setText(String.format(Locale.getDefault(), "Block %s (%d Units)", model.getBlock_name(), model.getTotal_units_count()));
        mCb_itemBlockName.setEnabled(enabled);
        //check if already open for sale
        mCb_itemBlockName.setChecked(model.getIsOpenForSale() == 1);

        mCb_itemBlockName.setOnCheckedChangeListener((compoundButton, checked) -> {

            if (checked)
            {
                //Already checked -- do unchecked

                //set checkBox checked false
                //mCb_itemBlockName.setChecked(false);

                if (model.getIsOpenForSale()==1)
                {
                    //already open for sale

                    //remove id from removedBlocks arrayList
                    checkInsertRemoveRemovedBlockIds(model.getBlock_id(), false, projectModel);
                }
                else {

                    //add id into added Blocks arrayList

                    //add selected id into an arrayList
                    checkInsertRemoveAddedBlockIds(model.getBlock_id(), true, projectModel);
                }

                //check arrayList
                checkArrayList(projectModel.getAddedBlockIdsArrayList(), projectModel.getRemovedBlockIdsArrayList());
            }
            else
            {
                //already unchecked -- do checked

                //set checkBox checked true
               // mCb_itemBlockName.setChecked(true);

                if (model.getIsOpenForSale()==1)
                {
                    //already open for sale

                    //add id into removedBlocks arrayList
                    checkInsertRemoveRemovedBlockIds(model.getBlock_id(), true, projectModel);

                }
                else {

                    //remove id from addedBlocks arrayList

                    //remove selected id from arrayList
                    checkInsertRemoveAddedBlockIds(model.getBlock_id(), false, projectModel);
                }

                //check arrayList
                checkArrayList(projectModel.getAddedBlockIdsArrayList(), projectModel.getRemovedBlockIdsArrayList());
            }

        });

        return rowView;
    }


    @Override
    public int getItemCount() {
        return (null != itemArrayList ? itemArrayList.size() : 0);
    }

    private void setAnimation(View v, int p) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (p > lastPosition) {
            anim.slideInBottom(v);
            lastPosition = p;
        }
    }

     static class AdapterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv_itemBlockForSale) MaterialCardView cv_itemBlockForSale;
        @BindView(R.id.mTv_itemBlockForSale_projectName) MaterialTextView mTv_projectName;
        @BindView(R.id.iv_itemBlockForSale_editBlock) AppCompatImageView iv_editBlock;
        @BindView(R.id.mTv_itemBlockForSale_projectBlocks) MaterialTextView mTv_projectBlocks;
        @BindView(R.id.ll_itemBlockForSale_addBlocks) LinearLayoutCompat ll_addBlocks;
        @BindView(R.id.mTv_newUser_noBlocks) MaterialTextView mTv_noBlocks;
        @BindView(R.id.ll_itemBlockForSale_updateBlockView) LinearLayoutCompat ll_updateBlockView;
        @BindView(R.id.mBtn_itemBlockForSale_update) MaterialButton mBtn_update;


        AdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    private void checkInsertRemoveAddedBlockIds(int block_id, boolean value, ProjectModel projectModel) {
        if (value) addedBlockIdsArrayList.add(block_id);
            //else catStringArrayList.remove(new String(subcatName));
        else addedBlockIdsArrayList.remove(new Integer(block_id));

        //set ArrayList
        projectModel.setAddedBlockIdsArrayList(addedBlockIdsArrayList);
    }

    public ArrayList<Integer> getAddedBlockIdsArrayList() {
        return addedBlockIdsArrayList;
    }

    private void checkInsertRemoveRemovedBlockIds(int block_id, boolean value, ProjectModel projectModel) {
        if (value) removedBlockIdsArrayList.add(block_id);
            //else catStringArrayList.remove(new String(subcatName));
        else removedBlockIdsArrayList.remove(new Integer(block_id));

        //set ArrayList
        projectModel.setRemovedBlockIdsArrayList(removedBlockIdsArrayList);
    }

    public ArrayList<Integer> getRemovedBlockIdsArrayList() {
        return removedBlockIdsArrayList;
    }

    private void checkArrayList(ArrayList<Integer> blockIdsArrayList, ArrayList<Integer> removedBlockIdsArrayList)
    {

        //check added block id's arrayList
        if (blockIdsArrayList!=null && blockIdsArrayList.size()>0) {
            Log.e(TAG, "Added Blocks Array: "+ Arrays.toString(blockIdsArrayList.toArray()));
        }
        else Log.e(TAG, "Added Blocks Array: null" );

        //check removed block id's arrayList
        if (removedBlockIdsArrayList!=null && removedBlockIdsArrayList.size()>0) {
            Log.e(TAG, "Removed Blocks Array: "+ Arrays.toString(removedBlockIdsArrayList.toArray()));
        }
        else Log.e(TAG, "Removed Blocks Array: null" );
    }



    private void expandSubView(final View v) {

        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                //v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int)(targetHeight * interpolatedTime);
                if (interpolatedTime == 1)
                    v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                v.requestLayout();

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };


        //a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(100);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //iv_arrow.setImageResource(R.drawable.ic_expand_icon_white);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);

    }


    private void collapse(final View v) {

        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);
    }

}
