package com.opensource.slidemenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;


/**
 * Created by VeyHey on 2016/2/13.
 */
public class SlideMenu extends HorizontalScrollView {

    private int mScreenWidth;
    private int mScreenHeight;
    private int mMenuWidth;
    private View mMenu;

    public View getMenuView() {
        return mMenu;
    }

    public View getContentView() {
        return mContent;
    }

    private View mContent;
    private boolean mHasInit = false;
    private LinearLayout mWrapper;

    public SlideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SlideMenu);
        int menuLayoutId = typedArray.getResourceId(0, -1);
        int contentLayoutId = typedArray.getResourceId(1, -1);
        mMenuWidth = typedArray.getDimensionPixelOffset(2, 0);
        mMenu = View.inflate(getContext(), menuLayoutId, null);
        mContent = View.inflate(getContext(), contentLayoutId, null);

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;

        setOverScrollMode(OVER_SCROLL_NEVER);
        setHorizontalScrollBarEnabled(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(
                mScreenWidth, mScreenHeight);
        if(!mHasInit){
            mHasInit = true;
            mWrapper = new LinearLayout(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(mMenuWidth+mScreenWidth,getMeasuredHeight());
            mWrapper.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(mMenuWidth, mScreenHeight);
            mWrapper.addView(mMenu, menuParams);
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(mScreenWidth, mScreenHeight);
            mWrapper.addView(mContent, contentParams);
            mWrapper.setLayoutParams(layoutParams);
            addView(mWrapper);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(changed){
            scrollTo(mMenuWidth, 0);
        }
    }

    private long mDownTime;
    private float mDownX;
    private float mDownY;

    private boolean mAcceptClick;
    private boolean mAcceptMove;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getScrollX() == 0) {
            return true;
        }
        if (getScrollX() == mMenuWidth) {
            mDownX = ev.getX();
            if (mDownX + getScrollX() > mMenuWidth + 100) {
                clearTemp();
                return false;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownTime = System.currentTimeMillis();
                mDownX = ev.getX();
                mDownY = ev.getY();
                if(getScrollX()==0||getScrollX()!=0&& mDownX + getScrollX() < mMenuWidth + 100) {
                    mAcceptMove = true;
                }
                if (getScrollX()==0&& mDownX + getScrollX() > mMenuWidth) {
                    mAcceptClick = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(!mAcceptMove){
                    if(getScrollX()+ev.getX()<mMenuWidth+100){
                        mAcceptMove = true;
                    }
                    ev.setAction(MotionEvent.ACTION_DOWN);
                }
                break;
            case MotionEvent.ACTION_UP:
                float upX = ev.getX();
                float upY = ev.getY();
                if (isClick(upX, upY)) {
                    smoothScrollTo(mMenuWidth, 0);
                    clearTemp();
                    return true;
                }
                if (getScrollX() > mMenuWidth / 2) {
                    smoothScrollTo(mMenuWidth, 0);
                } else {
                    smoothScrollTo(0, 0);
                }
                clearTemp();
                return true;
        }
        return super.onTouchEvent(ev);
    }

    private void clearTemp() {
        mDownX = 0;
        mDownY = 0;
        mAcceptClick = false;
        mAcceptMove = false;
    }

    private boolean isClick(float upX, float upY) {
        return mAcceptClick && System.currentTimeMillis() - mDownTime < 1000 && Math.pow((mDownX - upX), 2) + Math.pow((mDownY - upY), 2) < 200;
    }

    /**
     * 弹出或隐藏
     */
    public void toggle(){
        if (getScrollX()==mMenuWidth){
            smoothScrollTo(0,0);
        }else{
            smoothScrollTo(mMenuWidth,0);
        }
    }
}
