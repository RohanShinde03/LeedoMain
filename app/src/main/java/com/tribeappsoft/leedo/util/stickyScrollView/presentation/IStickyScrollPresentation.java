package com.tribeappsoft.leedo.util.stickyScrollView.presentation;

/**
 * Created by Rohan on 22/10/19.
 */

public interface IStickyScrollPresentation {
    void freeHeader();
    void freeFooter();
    void stickHeader(int translationY);
    void stickFooter(int translationY);

    void initHeaderView(int id);
    void initFooterView(int id);

}
