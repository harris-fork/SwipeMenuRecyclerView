package com.aitsuki.swipe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by AItsuki on 2017/2/23.
 * 1. 最多同时设置两个菜单
 * 2. 菜单必须设置layoutGravity属性. start left end right
 */
public class SwipeItemLayout extends FrameLayout {

    public static final String TAG = "SwipeItemLayout";

    private ViewDragHelper mDragHelper;
    private int mTouchSlop;
    public int mVelocity;

    private float mDownX;
    private float mDownY;
    private boolean mIsDragged;

    /**
     * 通过判断手势进行赋值 {@link #checkCanDragged(MotionEvent)}
     */
    public View mCurrentMenu;

    /**
     * 某些情况下，不能通过mIsOpen判断当前菜单是否开启或是关闭。
     * 因为在调用 {@link #open()} 或者 {@link #close()} 的时候，mIsOpen的值已经被改变，但是
     * 此时ContentView还没有到达应该的位置。亦或者ContentView已经到拖拽达指定位置，但是此时并没有
     * 松开手指，mIsOpen并不会重新赋值。
     */
    private boolean mIsOpen;

    /**
     * Menu的集合，以{@link android.view.Gravity#LEFT}和{@link android.view.Gravity#LEFT}作为key，
     * 菜单View作为value保存。
     */
    public LinkedHashMap<Integer, View> mMenus = new LinkedHashMap<>();

    /**
     * 菜单是否开始拖动
     */
    public boolean isOpen() {
        return mIsOpen;
    }

    public SwipeItemLayout(Context context) {
        this(context, null);
    }

    public SwipeItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        mDragHelper = ViewDragHelper.create(this, new DragCallBack(this));
    }

    /**
     * 获取ContentView，最上层显示的View即为ContentView
     */
    public View getContentView() {
        return getChildAt(getChildCount() - 1);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        super.onLayout(changed, left, top, right, bottom);

        updateMenu();
    }

    /**
     * 当菜单被ContentView遮住的时候，要设置菜单为Invisible，防止已隐藏的菜单接收到点击事件。
     */
    public void updateMenu() {

        View contentView = getContentView();
        if (contentView != null) {

            int contentLeft = contentView.getLeft();
            if (contentLeft == 0) {

                for (View view : mMenus.values()) {

                    //checkAbsoluteGravity
                    final int gravity = ((LayoutParams) view.getLayoutParams()).gravity;
                    final int absGravity = GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(this));
                    boolean isGravityLeft = (absGravity & Gravity.LEFT) == Gravity.LEFT;

                    if (isGravityLeft) {

                        view.layout(-view.getWidth(), view.getTop(), 0, view.getBottom());
                    } else {

                        view.layout(getMeasuredWidth(), view.getTop(), getMeasuredWidth() + view.getMeasuredWidth(), view.getBottom());
                    }
                }

            } else {

                if (mCurrentMenu != null && mCurrentMenu.getLeft() != 0) {

                    if (isLeftMenu()) {

                        mCurrentMenu.layout(0, mCurrentMenu.getTop(), mCurrentMenu.getMeasuredWidth(), mCurrentMenu.getBottom());
                    } else {

                        mCurrentMenu.layout(getMeasuredWidth() - mCurrentMenu.getMeasuredWidth(), mCurrentMenu.getTop(), getMeasuredWidth(), mCurrentMenu.getBottom());
                    }
                }
            }
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {

            //是否正在做关闭动画
            //关闭菜单过程中禁止接收down事件
            boolean isCloseAnimating = false;
            if (mCurrentMenu != null) {
                int contentLeft = getContentView().getLeft();
                if (!mIsOpen && ((isLeftMenu() && contentLeft > 0) || (isRightMenu() && contentLeft < 0))) {
                    return true;
                }
            }
            if (isCloseAnimating) {
                return false;
            }

            // 菜单打开的时候，按下Content关闭菜单
            if (mIsOpen && isTouchContent(((int) ev.getX()), ((int) ev.getY()))) {
                close();
                return false;
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsDragged = false;
                mDownX = ev.getX();
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                checkCanDragged(ev);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsDragged) {
                    mDragHelper.processTouchEvent(ev);
                    mIsDragged = false;
                }
                break;
            default:
                if (mIsDragged) {
                    mDragHelper.processTouchEvent(ev);
                }
                break;
        }
        return mIsDragged || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsDragged = false;
                mDownX = ev.getX();
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                boolean beforeCheckDrag = mIsDragged;
                checkCanDragged(ev);
                if (mIsDragged) {
                    mDragHelper.processTouchEvent(ev);
                }

                // 开始拖动后，发送一个cancel事件用来取消点击效果
                if (!beforeCheckDrag && mIsDragged) {
                    MotionEvent obtain = MotionEvent.obtain(ev);
                    obtain.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(obtain);
                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsDragged || mIsOpen) {
                    mDragHelper.processTouchEvent(ev);
                    // 拖拽后手指抬起，或者已经开启菜单，不应该响应到点击事件
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    mIsDragged = false;
                }
                break;
            default:
                if (mIsDragged) {
                    mDragHelper.processTouchEvent(ev);
                }
                break;
        }
        return mIsDragged || super.onTouchEvent(ev)
                // 此判断是因为当没有点击事件时，事件会给RecylcerView响应导致无法划开菜单。
                || (!isClickable() && mMenus.size() > 0);
    }

    /**
     * 判断是否可以拖拽View
     */
    @SuppressLint("RtlHardcoded")
    private void checkCanDragged(MotionEvent ev) {
        if (mIsDragged) {
            return;
        }

        float dx = ev.getX() - mDownX;
        float dy = ev.getY() - mDownY;
        boolean isRightDrag = dx > mTouchSlop && dx > Math.abs(dy);
        boolean isLeftDrag = dx < -mTouchSlop && Math.abs(dx) > Math.abs(dy);

        if (mIsOpen) {

            // 开启状态下，点击在content上就捕获事件，点击在菜单上则判断touchSlop
            int downX = (int) mDownX;
            int downY = (int) mDownY;
            if (isTouchContent(downX, downY)) {
                mIsDragged = true;
            } else {

                boolean isTouchMenu = false;
                if (mCurrentMenu != null) {

                    Rect rect = new Rect();
                    mCurrentMenu.getHitRect(rect);
                    isTouchMenu = rect.contains(downX, downY);
                }
                if (isTouchMenu) {
                    mIsDragged = (isLeftMenu() && isLeftDrag) || (isRightMenu() && isRightDrag);
                }
            }

        } else {
            // 关闭状态，获取当前即将要开启的菜单。
            if (isRightDrag) {
                mCurrentMenu = mMenus.get(Gravity.LEFT);
                mIsDragged = mCurrentMenu != null;
            } else if (isLeftDrag) {
                mCurrentMenu = mMenus.get(Gravity.RIGHT);
                mIsDragged = mCurrentMenu != null;
            }
        }

        if (mIsDragged) {
            // 开始拖动后，分发down事件给DragHelper，并且发送一个cancel取消点击事件
            MotionEvent obtain = MotionEvent.obtain(ev);
            obtain.setAction(MotionEvent.ACTION_DOWN);
            mDragHelper.processTouchEvent(obtain);
            if (getParent() != null) {
                // 解决和父控件的滑动冲突。
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
    }

    // 最后一个是内容，倒数第1第2个设置了layout_gravity = right or left的是菜单，其余的忽略
    @SuppressLint("RtlHardcoded")
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int gravity = GravityCompat.getAbsoluteGravity(lp.gravity, ViewCompat.getLayoutDirection(child));
        switch (gravity) {
            case Gravity.RIGHT:
                mMenus.put(Gravity.RIGHT, child);
                break;
            case Gravity.LEFT:
                mMenus.put(Gravity.LEFT, child);
                break;
            default:
                break;
        }
    }

    /**
     * 判断down是否点击在Content上
     */
    public boolean isTouchContent(int x, int y) {
        View contentView = getContentView();
        if (contentView == null) {
            return false;
        }
        Rect rect = new Rect();
        contentView.getHitRect(rect);
        return rect.contains(x, y);
    }

    @SuppressLint("RtlHardcoded")
    public boolean isLeftMenu() {
        return mCurrentMenu != null && mCurrentMenu == mMenus.get(Gravity.LEFT);
    }

    @SuppressLint("RtlHardcoded")
    public boolean isRightMenu() {
        return mCurrentMenu != null && mCurrentMenu == mMenus.get(Gravity.RIGHT);
    }


    /**
     * 关闭菜单
     */
    public void close() {
        if (mCurrentMenu == null) {
            mIsOpen = false;
            return;
        }
        mDragHelper.smoothSlideViewTo(getContentView(), getPaddingLeft(), getPaddingTop());
        mIsOpen = false;
        invalidate();
    }

    /**
     * 开启菜单
     */
    public void open() {
        if (mCurrentMenu == null) {
            mIsOpen = false;
            return;
        }

        if (isLeftMenu()) {

            mDragHelper.smoothSlideViewTo(getContentView(), mCurrentMenu.getWidth(), getPaddingTop());
        } else if (isRightMenu()) {

            mDragHelper.smoothSlideViewTo(getContentView(), -mCurrentMenu.getWidth(), getPaddingTop());
        }
        mIsOpen = true;
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (mDragHelper.continueSettling(true)) {

            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
}
