package com.hiopengl;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class SurfaceViewAndTextureViewActivity extends ActionBarActivity {

    private OpenGLSurfaceView mSurfaceView;
    private OpenGLTextureView mTextureView;

    // tween animation
    private RotateAnimation mRotateAnimation;
    private TranslateAnimation mTranslateAnimation;
    private ScaleAnimation mScaleAnimation;
    private AlphaAnimation mAlphaAnimation;

    // property animation
    private Animator mRotateAnimatorForSurfaceView;
    private Animator mRotateAnimatorForTextureView;

    private Animator mTranslateAnimatorForSurfaceView;
    private Animator mTranslateAnimatorForTextureView;

    private Animator mScaleAnimatorForSurfaceView;
    private Animator mScaleAnimatorForTextureView;

    private Animator mAlphaAnimatorForSurfaceView;
    private Animator mAlphaAnimatorForTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_surface_view_and_texture_view);
        mSurfaceView = findViewById(R.id.surface_view);
        mTextureView = findViewById(R.id.texture_view);

        mRotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setDuration(1000);
        mRotateAnimation.setFillAfter(true);
        mRotateAnimation.setInterpolator(this, android.R.anim.accelerate_decelerate_interpolator);

        mTranslateAnimation = new TranslateAnimation(0, 200, 0, 0);
        mTranslateAnimation.setDuration(1000);
        mTranslateAnimation.setInterpolator(this, android.R.anim.cycle_interpolator);//设置动画插入器
        mTranslateAnimation.setFillAfter(true); // 设置动画结束后保持当前的位置（即不返回到动画开始前的位置）

        mScaleAnimation = new ScaleAnimation(0.0f, 1.5f, 0.0f, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mScaleAnimation.setDuration(1000);//设置动画持续时间为1000毫秒
        mScaleAnimation.setRepeatCount(3);//设置动画循环次数
        mScaleAnimation.setRepeatMode(Animation.REVERSE);
        mScaleAnimation.setStartOffset(0);
        mScaleAnimation.setInterpolator(this, android.R.anim.decelerate_interpolator);//设置动画插入器

        mAlphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        mAlphaAnimation.setDuration(2000);//设置动画持续时间为2000毫秒

        mRotateAnimatorForSurfaceView = AnimatorInflater.loadAnimator(this, R.animator.animator_rotate);
        mRotateAnimatorForTextureView = AnimatorInflater.loadAnimator(this, R.animator.animator_rotate);

        mTranslateAnimatorForSurfaceView = AnimatorInflater.loadAnimator(this, R.animator.animator_translate);
        mTranslateAnimatorForTextureView = AnimatorInflater.loadAnimator(this, R.animator.animator_translate);

        mScaleAnimatorForSurfaceView = AnimatorInflater.loadAnimator(this, R.animator.animator_scale);
        mScaleAnimatorForTextureView = AnimatorInflater.loadAnimator(this, R.animator.animator_scale);

        mAlphaAnimatorForSurfaceView = AnimatorInflater.loadAnimator(this, R.animator.animator_alpha);
        mAlphaAnimatorForTextureView = AnimatorInflater.loadAnimator(this, R.animator.animator_alpha);
    }

    public void onRotateTweenAnimationClick(View view) {
        mSurfaceView.startAnimation(mRotateAnimation);
        mTextureView.startAnimation(mRotateAnimation);
    }

    public void onTranslateTweenAnimationClick(View view) {
        mSurfaceView.startAnimation(mTranslateAnimation);
        mTextureView.startAnimation(mTranslateAnimation);
    }

    public void onScaleTweenAnimationClick(View view) {
        mSurfaceView.startAnimation(mScaleAnimation);
        mTextureView.startAnimation(mScaleAnimation);
    }

    public void onAlphaTweenAnimationClick(View view) {
        mSurfaceView.startAnimation(mAlphaAnimation);
        mTextureView.startAnimation(mAlphaAnimation);
    }

    public void onRotatePropertyAnimationClick(View view) {
        mRotateAnimatorForSurfaceView.setTarget(mSurfaceView);
        mRotateAnimatorForSurfaceView.start();

        mRotateAnimatorForTextureView.setTarget(mTextureView);
        mRotateAnimatorForTextureView.start();
    }

    public void onTranslatePropertyAnimationClick(View view) {
        mTranslateAnimatorForSurfaceView.setTarget(mSurfaceView);
        mTranslateAnimatorForSurfaceView.start();

        mTranslateAnimatorForTextureView.setTarget(mTextureView);
        mTranslateAnimatorForTextureView.start();
    }

    public void onScalePropertyAnimationClick(View view) {
        mScaleAnimatorForSurfaceView.setTarget(mSurfaceView);
        mScaleAnimatorForSurfaceView.start();

        mScaleAnimatorForTextureView.setTarget(mTextureView);
        mScaleAnimatorForTextureView.start();
    }

    public void onAlphaPropertyAnimationClick(View view) {
        mAlphaAnimatorForSurfaceView.setTarget(mSurfaceView);
        mAlphaAnimatorForSurfaceView.start();

        mAlphaAnimatorForTextureView.setTarget(mTextureView);
        mAlphaAnimatorForTextureView.start();
    }
}
