package com.tribeappsoft.leedo.util;

import android.os.Build;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by prk on 28/7/16.
 */

public class Animations {

    private RotateAnimation ra1;
    private AlphaAnimation aa1;
    private TranslateAnimation ta1;
    private ScaleAnimation sa1;
    private AnimationSet as1;
    // --Commented out by Inspection (17/9/16 5:29 PM):private ObjectAnimator objectAnimator;

    private final Interpolator linear;
    private final Interpolator bounce;
    private final Interpolator overShoot;
    private final Interpolator accelerate;
    private final Interpolator decelerate;

    public Animations() {

        linear = new LinearInterpolator();
        bounce = new BounceInterpolator();
        overShoot = new OvershootInterpolator();
        accelerate = new AccelerateDecelerateInterpolator();
        decelerate = new DecelerateInterpolator();

    }

    public void wiggleEffect(final View view) {

        ta1 = new TranslateAnimation(-10, 10, 0, 0);
        ta1.setDuration(70);
        ta1.setRepeatCount(5);
        ta1.setRepeatMode(Animation.REVERSE);
        ta1.setInterpolator(linear);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            as1 = new AnimationSet(true);
            as1.addAnimation(ta1);
            view.startAnimation(as1);

        } else view.startAnimation(ta1);

    }

    public void clickEffect(final View view) {

                sa1 = new ScaleAnimation(0.7f, 1, 0.7f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                sa1.setFillAfter(true);
                sa1.setDuration(100);
                view.startAnimation(sa1);

    }


    public void clickEffectL(final View view) {

                sa1 = new ScaleAnimation(2, 1, 2, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                sa1.setFillAfter(true);
                sa1.setDuration(100);
                view.startAnimation(sa1);

    }

    public void shrinkEffect(final View view) {

                sa1 = new ScaleAnimation(1, 0.3f, 1, 0.3f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                sa1.setFillAfter(true);
                sa1.setDuration(100);
                view.startAnimation(sa1);

    }

    public void scaleEffect(final View view) {
                sa1 = new ScaleAnimation(3, 1, 3, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                sa1.setFillAfter(true);
                sa1.setDuration(100);
                sa1.setInterpolator(bounce);
                view.startAnimation(sa1);
    }

    public void fabClose(View view){
        ra1 = new RotateAnimation(0,135, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra1.setFillAfter(true);
        ra1.setDuration(200);
        ra1.setInterpolator(overShoot);
        view.startAnimation(ra1);
    }

    public void fabPlus(View view){

        ra1 = new RotateAnimation(135,0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra1.setFillAfter(true);
        ra1.setDuration(200);
        ra1.setStartOffset((long) 0);
        ra1.setInterpolator(overShoot);
        view.startAnimation(ra1);

    }

    public void fabScaleUp(View view, long offset){

                sa1 = new ScaleAnimation(0, 1, 0, 1,  Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                sa1.setFillAfter(true);
                sa1.setDuration(200);
                sa1.setStartOffset(offset);
                sa1.setInterpolator(overShoot);
                view.startAnimation(sa1);
    }

    public void fabScaleDown(View view, long offset){
                sa1 = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                sa1.setFillAfter(true);
                sa1.setDuration(200);
                sa1.setStartOffset(offset);
                sa1.setInterpolator(accelerate);
                view.startAnimation(sa1);
    }


    public Animation toMiddle(){
        sa1 = new ScaleAnimation(1, 0, 1, 1, 50, 50);
        sa1.setFillAfter(true);
        sa1.setDuration(100);
        sa1.setInterpolator(decelerate);
        return sa1;
    }

    public Animation fromMiddle(){
        ScaleAnimation sa1 = new ScaleAnimation(0, 1, 1, 1, 50, 50);
        sa1.setFillAfter(true);
        sa1.setDuration(100);
        sa1.setInterpolator(decelerate);
        return sa1;
    }

    public void toggleRotate(View view, boolean rotate)
    {
        if(rotate) ra1 = new RotateAnimation(0,180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        else ra1 = new RotateAnimation(180,0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra1.setFillAfter(true);
        ra1.setDuration(250);
        ra1.setInterpolator(linear);
        view.startAnimation(ra1);
    }

    public void slideInLeft(final View view) {

        ta1 = new TranslateAnimation(-50, 0, 0, 0);
        ta1.setFillAfter(true);
        ta1.setDuration(400);

        aa1 = new AlphaAnimation(0, 1);
        aa1.setFillAfter(true);
        aa1.setDuration(400);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            as1 = new AnimationSet(true);
            as1.addAnimation(ta1);
            as1.addAnimation(aa1);
            view.startAnimation(as1);
        } else {

            new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(ta1);
                }
            };
            new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(aa1);
                }
            };
        }
    }

    public void slideInTop(final View view){
        ta1 = new TranslateAnimation(0, 0, -100, 0);
        ta1.setFillAfter(true);
        ta1.setDuration(400);
        ta1.setStartOffset((long) 400);
        ta1.setInterpolator(bounce);

        aa1 = new AlphaAnimation(0, 1);
        aa1.setFillAfter(true);
        aa1.setDuration(400);
        aa1.setStartOffset((long) 400);
        aa1.setInterpolator(bounce);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            as1 = new AnimationSet(true);
            as1.addAnimation(ta1);
            as1.addAnimation(aa1);
            view.startAnimation(as1);
        } else {

            new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(ta1);
                }
            };
            new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(aa1);
                }
            };
        }
    }

    public void slideInBottom(final View view){
        ta1 = new TranslateAnimation(0, 0, 75, 0);
        ta1.setFillAfter(true);
        ta1.setDuration(400);
        ta1.setInterpolator(bounce);

        aa1 = new AlphaAnimation(0, 1);
        aa1.setFillAfter(true);
        aa1.setDuration(400);
        aa1.setInterpolator(bounce);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            as1 = new AnimationSet(true);
            as1.addAnimation(ta1);
            as1.addAnimation(aa1);
            view.startAnimation(as1);
        } else {

            new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(ta1);
                }
            };
            new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(aa1);
                }
            };
        }
    }

    public void slideOutBottom(final View view){
        ta1 = new TranslateAnimation(0, 0, 0, 100);
        ta1.setFillAfter(true);
        ta1.setDuration(200);
        ta1.setInterpolator(accelerate);

        aa1 = new AlphaAnimation(1, 0);
        aa1.setFillAfter(true);
        aa1.setDuration(200);
        aa1.setInterpolator(accelerate);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            as1 = new AnimationSet(true);
            as1.addAnimation(ta1);
            as1.addAnimation(aa1);
            view.startAnimation(as1);
        } else {

            new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(ta1);
                }
            };
            new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(aa1);
                }
            };
        }
    }

    public void slideInBottom(final View view, long offset){
        ta1 = new TranslateAnimation(0, 0, 75, 0);
        ta1.setFillAfter(true);
        ta1.setDuration(400);
        ta1.setStartOffset(offset);
        ta1.setInterpolator(bounce);

        aa1 = new AlphaAnimation(0, 1);
        aa1.setFillAfter(true);
        aa1.setDuration(400);
        aa1.setStartOffset(offset);
        aa1.setInterpolator(bounce);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            as1 = new AnimationSet(true);
            as1.addAnimation(ta1);
            as1.addAnimation(aa1);
            view.startAnimation(as1);
        } else {

            new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(ta1);
                }
            };
            new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(aa1);
                }
            };
        }
    }


    public void slideInBottomFab(final View view){
        ta1 = new TranslateAnimation(0, 0, 75, 0);
        ta1.setFillAfter(true);
        ta1.setDuration(200);
        ta1.setInterpolator(bounce);

        aa1 = new AlphaAnimation(0, 1);
        aa1.setFillAfter(true);
        aa1.setDuration(200);
        aa1.setInterpolator(bounce);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            as1 = new AnimationSet(true);
            as1.addAnimation(ta1);
            as1.addAnimation(aa1);
            view.startAnimation(as1);
        } else {

            new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(ta1);
                }
            };
            new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(aa1);
                }
            };
        }
    }
}
