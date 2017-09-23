package com.aitsuki.swipedemo;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aitsuki.swipedemo.entity.Data;
import com.aitsuki.swipedemo.entity.Type;

import java.util.List;

/**
 * Created by hc_zhang on 2017/9/22.
 */
public class DemoAdapter extends RecyclerView.Adapter<SimpleViewHolder> {

    private ItemTouchListener mItemTouchListener;
    private List<Data> mData;

    DemoAdapter(List<Data> data, ItemTouchListener itemTouchListener) {

        this.mData = data;
        this.mItemTouchListener = itemTouchListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).type;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        @LayoutRes
        int layout;

        switch (viewType) {
            case Type.LEFT_MENU:
                layout = R.layout.item_left_menu;
                break;
            case Type.RIGHT_MENU:
                layout = R.layout.item_right_menu;
                break;
            case Type.LEFT_AND_RIGHT_MENU:
                layout = R.layout.item_left_and_right_menu;
                break;
            case Type.LEFT_LONG_MENU:
                layout = R.layout.item_left_long_menu;
                break;
            case Type.RIGHT_LONG_MENU:
                layout = R.layout.item_right_long_menu;
                break;
            case Type.LEFT_AND_RIGHT_LONG_MENU:
                layout = R.layout.item_left_and_right_long_menu;
                break;
            default:
                layout = R.layout.item_left_menu;
                break;
        }

        View rootView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

        return new SimpleViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, int position) {

        holder.mContent.setText(mData.get(position).content.concat(" " + position));
        if (mItemTouchListener != null) {
            holder.itemView.setOnClickListener(v -> mItemTouchListener.onItemClick(holder.mContent.getText().toString()));

            if (holder.mLeftMenu != null) {
                holder.mLeftMenu.setOnClickListener(v -> {
                    mItemTouchListener.onLeftMenuClick("left " + holder.getAdapterPosition());
                    holder.mSwipeItemLayout.close();
                });
            }

            if (holder.mRightMenu != null) {
                holder.mRightMenu.setOnClickListener(v -> {
                    mItemTouchListener.onRightMenuClick("right " + holder.getAdapterPosition());
                    holder.mSwipeItemLayout.close();
                });
            }
        }
    }

}

