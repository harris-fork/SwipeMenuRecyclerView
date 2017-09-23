package com.aitsuki.swipedemo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.aitsuki.swipe.SwipeItemLayout;

/**
 * Created by hc_zhang on 2017/9/22.
 */

public class SimpleViewHolder extends RecyclerView.ViewHolder {

    public final View mLeftMenu;
    public final View mRightMenu;
    public final TextView mContent;
    public final SwipeItemLayout mSwipeItemLayout;

    SimpleViewHolder(View itemView) {

        super(itemView);
        mSwipeItemLayout = (SwipeItemLayout) itemView.findViewById(R.id.swipe_layout);
        mContent = (TextView) itemView.findViewById(R.id.tv_content);
        mLeftMenu = itemView.findViewById(R.id.left_menu);
        mRightMenu = itemView.findViewById(R.id.right_menu);
    }
}