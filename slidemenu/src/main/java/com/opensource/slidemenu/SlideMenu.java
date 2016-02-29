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
        //解析自定义参数的值
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SlideMenu);
        int menuLayoutId = typedArray.getResourceId(0, -1);
        int contentLayoutId = typedArray.getResourceId(1, -1);
        mMenuWidth = typedArray.getDimensionPixelOffset(2, 0);

        //加载View
        mMenu = View.inflate(getContext(), menuLayoutId, null);
        mContent = View.inflate(getContext(), contentLayoutId, null);

        //获取屏幕宽高
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;

        //设置为不能滑出边界(滑出边界的效果比较难看)
        setOverScrollMode(OVER_SCROLL_NEVER);

        //设置无滚动条
        setHorizontalScrollBarEnabled(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //设置测量的宽高为屏幕的宽高
        setMeasuredDimension(
                mScreenWidth, mScreenHeight);
        if(!mHasInit){
            //由于会多次测量，所以使用mHasInit保证布局只加载一次
            mHasInit = true;
            //创建子View，并加到ScrollView中
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
            //将ScrollView滚动到mMenuWidth的位置，也就是滚动到跟内容View的左侧对其，让菜单栏刚好完全隐藏在屏幕左侧
            //注意滚动的时机，当view有新的布局和尺寸时调用,也就是在初始化的时候去滚动。这个时候滚动才不会在打开页面时有滚动的效果
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
