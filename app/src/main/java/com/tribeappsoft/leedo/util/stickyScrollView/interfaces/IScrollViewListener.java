package com.tribeappsoft.leedo.util.stickyScrollView.interfaces;

public interface IScrollViewListener {
    void onScrollChanged(int l, int t, int oldl, int oldt);
    void onScrollStopped(boolean isStoped);
}
