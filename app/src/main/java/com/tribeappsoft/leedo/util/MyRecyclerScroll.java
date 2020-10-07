package com.tribeappsoft.leedo.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/*
 * Created by ${ROHAN} on 27/5/19.
 */
public abstract class MyRecyclerScroll extends RecyclerView.OnScrollListener
{

    private static final int HIDE_THRESHOLD = 25;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;


    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
    {
        super.onScrolled(recyclerView, dx, dy);

        if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
            onHide();
            controlsVisible = false;
            scrolledDistance = 0;
        } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
            onShow();
            controlsVisible = true;
            scrolledDistance = 0;
        }

        if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
            scrolledDistance += dy;
        }


    }


    public abstract void onHide();
    public abstract void onShow();

}
