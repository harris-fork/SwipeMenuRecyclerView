package com.aitsuki.swipedemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.aitsuki.swipedemo.data.Repository;
import com.aitsuki.swipedemo.util.ToastUtil;

public class NativeRecyclerViewAdapterActivity extends AppCompatActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, NativeRecyclerViewAdapterActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_menu_recyclerview);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new DemoAdapter(new Repository().fakeDate(), mItemTouchListener));
    }

    ItemTouchListener mItemTouchListener = new ItemTouchListener() {
        @Override
        public void onItemClick(String str) {
            ToastUtil.show(str);
        }

        @Override
        public void onLeftMenuClick(String str) {
            ToastUtil.show(str);
        }

        @Override
        public void onRightMenuClick(String str) {
            ToastUtil.show(str);
        }
    };
}
