package com.aitsuki.swipe;

import android.support.v4.widget.ViewDragHelper;
import android.util.Log;
import android.view.View;

/**
 * Created by harriszhang on 2017/9/22.
 */
public class DragCallBack extends ViewDragHelper.Callback {

    public static final String TAG = "DragCallBack";

    SwipeItemLayout swipeItemLayout;

    public DragCallBack(SwipeItemLayout swipeItemLayout) {

        this.swipeItemLayout = swipeItemLayout;
    }

    @Override
    public boolean tryCaptureView(View child, int pointerId) {
        // menu和content都可以抓取，因为在menu的宽度为MatchParent的时候，是无法点击到content的
        return child == swipeItemLayout.getContentView() || swipeItemLayout.mMenus.containsValue(child);
    }

    @Override
    public int clampViewPositionHorizontal(View child, int left, int dx) {

        // 如果child是内容， 那么可以左划或右划，开启或关闭菜单
        if (child == swipeItemLayout.getContentView()) {
            if (swipeItemLayout.isRightMenu()) {
                return left > 0 ? 0 : left < -swipeItemLayout.mCurrentMenu.getWidth() ?
                        -swipeItemLayout.mCurrentMenu.getWidth() : left;
            } else if (swipeItemLayout.isLeftMenu()) {
                return left > swipeItemLayout.mCurrentMenu.getWidth() ? swipeItemLayout.mCurrentMenu.getWidth() : left < 0 ?
                        0 : left;
            }
        }

        // 如果抓取到的child是菜单，那么不移动child，而是移动contentView
        else if (swipeItemLayout.isRightMenu()) {
            View contentView = swipeItemLayout.getContentView();
            int newLeft = contentView.getLeft() + dx;
            if (newLeft > 0) {
                newLeft = 0;
            } else if (newLeft < -child.getWidth()) {
                newLeft = -child.getWidth();
            }
            contentView.layout(newLeft, contentView.getTop(), newLeft + contentView.getWidth(),
                    contentView.getBottom());
            return child.getLeft();
        } else if (swipeItemLayout.isLeftMenu()) {
            View contentView =swipeItemLayout. getContentView();
            int newLeft = contentView.getLeft() + dx;
            if (newLeft < 0) {
                newLeft = 0;
            } else if (newLeft > child.getWidth()) {
                newLeft = child.getWidth();
            }
            contentView.layout(newLeft, contentView.getTop(), newLeft + contentView.getWidth(),
                    contentView.getBottom());
            return child.getLeft();
        }
        return 0;
    }

    @Override
    public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
        super.onViewPositionChanged(changedView, left, top, dx, dy);
        swipeItemLayout.updateMenu();
    }

    @Override
    public void onViewReleased(View releasedChild, float xvel, float yvel) {
        Log.e(TAG, "onViewReleased: " + xvel + " ,releasedChild = " + releasedChild);
        if (swipeItemLayout.isLeftMenu()) {
            if (xvel > swipeItemLayout.mVelocity) {
                swipeItemLayout.open();
            } else if (xvel < -swipeItemLayout.mVelocity) {
                swipeItemLayout.close();
            } else {
                if (swipeItemLayout.getContentView().getLeft() > swipeItemLayout.mCurrentMenu.getWidth() / 3 * 2) {
                    swipeItemLayout.open();
                } else {
                    swipeItemLayout.close();
                }
            }
        } else if (swipeItemLayout.isRightMenu()) {
            if (xvel < -swipeItemLayout.mVelocity) {
                swipeItemLayout.open();
            } else if (xvel > swipeItemLayout.mVelocity) {
                swipeItemLayout. close();
            } else {
                if (swipeItemLayout.getContentView().getLeft() < -swipeItemLayout.mCurrentMenu.getWidth() / 3 * 2) {
                    swipeItemLayout.open();
                } else {
                    swipeItemLayout.close();
                }
            }
        }
    }

}